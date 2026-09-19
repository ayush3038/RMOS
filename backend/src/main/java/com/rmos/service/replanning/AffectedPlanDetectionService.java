package com.rmos.service.replanning;

import com.rmos.domain.event.RmosRealtimeEvent;
import com.rmos.domain.planning.PlanningDecisionStatus;
import com.rmos.domain.planning.PlanningRunEntity;
import com.rmos.repository.planning.PlanningRunRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AffectedPlanDetectionService {

    private final PlanningRunRepository planningRunRepository;

    public AffectedPlanDetectionService(PlanningRunRepository planningRunRepository) {
        this.planningRunRepository = planningRunRepository;
    }

    /**
     * Determines which currently active, scheduled, or pending plans intersect
     * spatially or temporally with the provided realtime event payload.
     */
    public List<PlanningRunEntity> detectAffectedPlans(RmosRealtimeEvent event) {
        // Find all currently active plans.
        // In a real railway scenario, we would use spatial indexing (e.g., PostGIS)
        // bounding box intersections against scheduled task locations.
        // For our M3.16 scope, we search for currently ACCEPTED or PENDING_REVIEW plans
        // generated within reasonable lookback horizons.

        List<PlanningRunEntity> allActiveRuns = planningRunRepository.findAll().stream()
                .filter(run -> run.getStatus() == com.rmos.domain.planning.PlanningRunStatus.CURRENT
                        || run.getStatus() == com.rmos.domain.planning.PlanningRunStatus.PENDING_REVIEW)
                .collect(Collectors.toList());

        return allActiveRuns.stream()
                .filter(run -> isAffected(run, event))
                .collect(Collectors.toList());
    }

    private boolean isAffected(PlanningRunEntity run, RmosRealtimeEvent event) {
        // If the event lacks a target or coordinates, we safely assume it overlaps the
        // active corridor
        if (event.getPayload() == null || event.getPayload().isEmpty()) {
            return true;
        }

        // Fast filtering: If plan is totally bounded outside the event context (e.g.
        // event time > plan horizon)
        // Here we rely on assigned_start of the tasks in the plan
        boolean hasAssignments = run.getAssignments() != null && !run.getAssignments().isEmpty();
        if (!hasAssignments)
            return false;

        LocalDateTime firstStart = run.getAssignments().stream().map(a -> a.getAssignedStart())
                .min(LocalDateTime::compareTo).orElse(null);
        LocalDateTime lastEnd = run.getAssignments().stream().map(a -> a.getAssignedEnd()).max(LocalDateTime::compareTo)
                .orElse(null);

        if (firstStart != null && lastEnd != null && event.getSourceUpdatedAt() != null) {
            // If the event happened strictly after the plan ended entirely, it's not
            // affected
            if (event.getSourceUpdatedAt().isAfter(lastEnd)) {
                return false;
            }
        }

        return true;
    }
}
