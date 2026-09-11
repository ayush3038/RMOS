package com.rmos.service.scenario;

import com.rmos.domain.planning.PlanningDecisionStatus;
import com.rmos.domain.planning.PlanningTaskStatus;
import com.rmos.domain.scenario.ScenarioStatus;
import com.rmos.dto.planning.PlanAssignment;
import com.rmos.dto.planning.PlanningRequest;
import com.rmos.dto.planning.PlanningResult;
import com.rmos.dto.planning.PlanningTask;
import com.rmos.dto.planning.RailwayPlanningTask;
import com.rmos.dto.scenario.ScenarioMetrics;
import com.rmos.dto.scenario.ScenarioRequest;
import com.rmos.dto.scenario.ScenarioResult;
import com.rmos.service.planning.PlanningService;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ScenarioSimulationService {

    private final PlanningService planningService;

    public ScenarioSimulationService(PlanningService planningService) {
        this.planningService = planningService;
    }

    public ScenarioResult simulate(ScenarioRequest scenarioRequest) {
        ScenarioResult result = new ScenarioResult();
        result.setScenarioId(scenarioRequest.getScenarioId());
        result.setScenarioName(scenarioRequest.getScenarioName());

        try {
            PlanningResult planningResult = planningService.plan(scenarioRequest.getPlanningRequest());
            result.setPlanningResult(planningResult);

            if (planningResult.getDecisionStatus() == PlanningDecisionStatus.INFEASIBLE) {
                result.setStatus(ScenarioStatus.INFEASIBLE);
                result.setExplanation("Underlying planning request was structurally infeasible.");
            } else {
                result.setStatus(ScenarioStatus.COMPLETED);
                result.setExplanation("Scenario simulated successfully.");
                result.setMetrics(calculateMetrics(scenarioRequest.getPlanningRequest(), planningResult));
            }
        } catch (Exception e) {
            result.setStatus(ScenarioStatus.FAILED);
            result.setExplanation("Simulation failed due to internal error: " + e.getMessage());
        }

        return result;
    }

    private ScenarioMetrics calculateMetrics(PlanningRequest request, PlanningResult result) {
        ScenarioMetrics metrics = new ScenarioMetrics();
        if (request.getTasks() == null || result.getAssignments() == null) {
            return metrics;
        }

        Map<Long, PlanningTask> taskMap = request.getTasks().stream()
                .collect(Collectors.toMap(PlanningTask::getTaskId, t -> t));

        int scheduledTasks = 0;
        int unscheduledTasks = 0;
        double prioritySum = 0.0;
        int scheduledMins = 0;
        int unscheduledMins = 0;
        int trainImpact = 0;
        int workforceLoad = 0;

        for (PlanAssignment assignment : result.getAssignments()) {
            PlanningTask task = taskMap.get(assignment.getTaskId());
            if (task == null)
                continue;

            if (assignment.getStatus() == PlanningTaskStatus.SCHEDULED) {
                scheduledTasks++;
                if (task.getPriorityScore() != null) {
                    prioritySum += task.getPriorityScore();
                }

                if (assignment.getAssignedStart() != null && assignment.getAssignedEnd() != null) {
                    scheduledMins += (int) Duration.between(assignment.getAssignedStart(), assignment.getAssignedEnd())
                            .toMinutes();
                }

                if (task instanceof RailwayPlanningTask rTask) {
                    if (rTask.getTrainImpactProfile() != null
                            && rTask.getTrainImpactProfile().getAffectedTrainCount() != null) {
                        trainImpact += rTask.getTrainImpactProfile().getAffectedTrainCount();
                    }
                    if (rTask.getWorkforceProfile() != null && rTask.getWorkforceProfile().getRequiredTeams() != null) {
                        workforceLoad += rTask.getWorkforceProfile().getRequiredTeams();
                    }
                }
            } else {
                unscheduledTasks++;
                if (task.getDurationMinutes() != null) {
                    unscheduledMins += task.getDurationMinutes();
                }
            }
        }

        metrics.setTotalTasks(request.getTasks().size());
        metrics.setScheduledTasks(scheduledTasks);
        metrics.setUnscheduledTasks(unscheduledTasks);
        metrics.setScheduledPrioritySum(prioritySum);
        metrics.setAveragePriorityOfScheduledTasks(scheduledTasks > 0 ? prioritySum / scheduledTasks : null);
        metrics.setTotalScheduledMinutes(scheduledMins);
        metrics.setTotalUnscheduledMinutes(unscheduledMins);
        metrics.setTrainImpactTotal(trainImpact);
        metrics.setWorkforceRequirementTotal(workforceLoad);

        // Block Window Utilization
        if (request.getPlanningContext() != null && request.getPlanningContext().getBlockWindows() != null) {
            int totalBlockMins = request.getPlanningContext().getBlockWindows().stream()
                    .filter(w -> w.getStatus() == com.rmos.domain.planning.BlockWindowStatus.AVAILABLE)
                    .mapToInt(w -> (int) Duration.between(w.getWindowStart(), w.getWindowEnd()).toMinutes())
                    .sum();

            if (totalBlockMins > 0) {
                metrics.setBlockWindowUtilization((double) scheduledMins / totalBlockMins);
            }
        }

        return metrics;
    }
}
