package com.rmos.service.replanning;

import com.rmos.domain.event.RmosRealtimeEvent;
import com.rmos.domain.planning.PlanAssignmentEntity;
import com.rmos.domain.planning.PlanningRunEntity;
import com.rmos.domain.planning.PlanningRunStatus;
import com.rmos.dto.planning.PlanAssignment;
import com.rmos.dto.planning.PlanningRequest;
import com.rmos.dto.planning.PlanningResult;
import com.rmos.repository.planning.PlanAssignmentRepository;
import com.rmos.repository.planning.PlanningRunRepository;
import com.rmos.service.planning.PlanningService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DynamicReplanningService {

    private final EventClassificationService classificationService;
    private final AffectedPlanDetectionService planDetectionService;
    private final PlanningRunRepository runRepository;
    private final PlanAssignmentRepository assignmentRepository;
    private final PlanningService planningService;

    public DynamicReplanningService(
            EventClassificationService classificationService,
            AffectedPlanDetectionService planDetectionService,
            PlanningRunRepository runRepository,
            PlanAssignmentRepository assignmentRepository,
            PlanningService planningService) {
        this.classificationService = classificationService;
        this.planDetectionService = planDetectionService;
        this.runRepository = runRepository;
        this.assignmentRepository = assignmentRepository;
        this.planningService = planningService;
    }

    @Transactional
    public PlanningResult handleRealtimeEvent(RmosRealtimeEvent event, PlanningRequest baseContext) {
        EventClassificationService.ChangeMateriality materiality = classificationService.classify(event);
        if (materiality == EventClassificationService.ChangeMateriality.NON_MATERIAL) {
            // Not a material change, skip replanning
            return null;
        }

        List<PlanningRunEntity> affectedPlans = planDetectionService.detectAffectedPlans(event);
        if (affectedPlans.isEmpty()) {
            // Material change, but no plans intersect it
            return null;
        }

        PlanningRunEntity affectedRun = affectedPlans.get(0); // Take first safely for M3.16 demonstration

        // Idempotency: Reject already processed events or out-of-order sequences
        if (event.getEntityId() != null && event.getEntityId().equals(affectedRun.getTriggerEventId())) {
            return null; // Idempotently rejected duplicate
        }

        // Change state to REPLANNING temporarily to lock it
        affectedRun.setStatus(PlanningRunStatus.REPLANNING);
        runRepository.save(affectedRun);

        // Map existing PlanAssignments to use as retained partial constraints
        List<PlanAssignment> retained = affectedRun.getAssignments().stream().map(a -> {
            PlanAssignment dto = new PlanAssignment();
            dto.setTaskId(a.getTaskId());
            dto.setAssignedStart(a.getAssignedStart());
            dto.setAssignedEnd(a.getAssignedEnd());
            dto.setStatus(a.getStatus());
            return dto;
        }).collect(Collectors.toList());

        // Pass retained tasks into solver to execute partial replanning
        baseContext.setRetainedAssignments(retained);
        PlanningResult newResult = planningService.plan(baseContext);

        // Record Candidate
        PlanningRunEntity candidate = new PlanningRunEntity();
        candidate.setId(UUID.randomUUID().toString());
        candidate.setVersion(affectedRun.getVersion() + 1);
        candidate.setParentPlanId(affectedRun.getId());
        candidate.setCreatedAt(LocalDateTime.now());
        candidate.setTriggerEventId(event.getEntityId());
        candidate.setReplanReason(event.getEventType() + " caused a material change.");

        List<PlanAssignmentEntity> newAssignments = newResult.getAssignments().stream().map(res -> {
            PlanAssignmentEntity entity = new PlanAssignmentEntity();
            entity.setTaskId(res.getTaskId());
            entity.setAssignedStart(res.getAssignedStart());
            entity.setAssignedEnd(res.getAssignedEnd());
            entity.setStatus(res.getStatus());
            return entity;
        }).collect(Collectors.toList());
        candidate.setAssignments(newAssignments);

        if (newResult.getDecisionStatus() == com.rmos.domain.planning.PlanningDecisionStatus.FEASIBLE) {
            candidate.setStatus(PlanningRunStatus.PENDING_REVIEW);
            affectedRun.setStatus(PlanningRunStatus.AFFECTED); // Reverts cleanly as it failed validation and created a
                                                               // new pending candidate
        } else {
            candidate.setStatus(PlanningRunStatus.INFEASIBLE);
            affectedRun.setStatus(PlanningRunStatus.INFEASIBLE);
        }

        runRepository.save(affectedRun);
        runRepository.save(candidate);

        newResult.setPlanningId(candidate.getId());
        return newResult;
    }
}
