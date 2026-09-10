package com.rmos.service.planning;

import com.rmos.domain.planning.PlanningDecisionStatus;
import com.rmos.domain.planning.PlanningTaskStatus;
import com.rmos.dto.planning.PlanAssignment;
import com.rmos.dto.planning.PlanningRequest;
import com.rmos.dto.planning.PlanningResult;
import com.rmos.dto.planning.PlanningTask;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Component
public class DeterministicPlanningOptimizer implements PlanningOptimizer {

    @Override
    public PlanningResult optimize(PlanningRequest request) {
        PlanningResult result = new PlanningResult();
        result.setPlanningId(request.getPlanningId());
        result.setExplanation(
                "BASELINE OPTIMIZER: Deterministic fallback matching sequential priorities only. No true constraint optimization performed.");

        List<PlanAssignment> assignments = new ArrayList<>();

        // Strategy: Sort descending by priority score
        List<PlanningTask> sortedTasks = new ArrayList<>(request.getTasks());
        sortedTasks.sort(Comparator.comparing(PlanningTask::getPriorityScore).reversed()
                .thenComparing(PlanningTask::getTaskId)); // deterministic tie break

        LocalDateTime currentCursor = request.getPlanningWindowStart();

        for (PlanningTask task : sortedTasks) {
            PlanAssignment assignment = new PlanAssignment();
            assignment.setTaskId(task.getTaskId());

            // If we lack information or hit window capacity, mark unscheduled.
            if (task.getDurationMinutes() == null || task.getDurationMinutes() <= 0) {
                assignment.setStatus(PlanningTaskStatus.UNSCHEDULED);
                assignments.add(assignment);
                continue;
            }

            LocalDateTime proposedEnd = currentCursor.plusMinutes(task.getDurationMinutes());

            // Validate against absolute window bounds
            if (proposedEnd.isAfter(request.getPlanningWindowEnd())) {
                assignment.setStatus(PlanningTaskStatus.UNSCHEDULED);
                assignments.add(assignment);
                continue;
            }

            // Validate against individual task bounds if present
            if (task.getLatestFinish() != null && proposedEnd.isAfter(task.getLatestFinish())) {
                assignment.setStatus(PlanningTaskStatus.UNSCHEDULED);
                assignments.add(assignment);
                continue;
            }

            if (task.getEarliestStart() != null && currentCursor.isBefore(task.getEarliestStart())) {
                currentCursor = task.getEarliestStart();
                proposedEnd = currentCursor.plusMinutes(task.getDurationMinutes());
                if (proposedEnd.isAfter(request.getPlanningWindowEnd()) ||
                        (task.getLatestFinish() != null && proposedEnd.isAfter(task.getLatestFinish()))) {
                    assignment.setStatus(PlanningTaskStatus.UNSCHEDULED);
                    assignments.add(assignment);
                    continue;
                }
            }

            // Assign slot
            assignment.setAssignedStart(currentCursor);
            assignment.setAssignedEnd(proposedEnd);
            assignment.setAssignedAssetId(task.getAssetId());
            assignment.setStatus(PlanningTaskStatus.SCHEDULED);
            assignments.add(assignment);

            // advance cursor for next
            currentCursor = proposedEnd;
        }

        result.setAssignments(assignments);
        result.setViolatedConstraints(Collections.emptyList());
        result.setDecisionStatus(PlanningDecisionStatus.FEASIBLE);
        result.setObjectiveScore(null);

        return result;
    }
}
