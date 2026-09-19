package com.rmos.service.planning;

import com.google.ortools.Loader;
import com.google.ortools.sat.BoolVar;
import com.google.ortools.sat.CpModel;
import com.google.ortools.sat.CpSolver;
import com.google.ortools.sat.CpSolverStatus;
import com.google.ortools.sat.IntervalVar;
import com.google.ortools.sat.IntVar;
import com.google.ortools.sat.LinearExpr;
import com.google.ortools.sat.LinearExprBuilder;
import com.rmos.domain.planning.PlanningDecisionStatus;
import com.rmos.domain.planning.PlanningTaskStatus;
import com.rmos.dto.planning.PlanAssignment;
import com.rmos.dto.planning.PlanningRequest;
import com.rmos.dto.planning.PlanningResult;
import com.rmos.dto.planning.PlanningTask;
import com.rmos.dto.planning.RailwayPlanningTask;
import com.rmos.dto.planning.RailwayPlanningContext;
import com.rmos.dto.planning.MaintenanceBlockWindow;
import com.rmos.domain.planning.BlockWindowStatus;
import com.rmos.dto.planning.WorkforceProfile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Primary
public class ORToolsCpSatOptimizer implements PlanningOptimizer {

    private final int maxSeconds;
    private final int workers;

    public ORToolsCpSatOptimizer(
            @Value("${rmos.optimization.max-seconds:5}") int maxSeconds,
            @Value("${rmos.optimization.workers:4}") int workers) {
        this.maxSeconds = maxSeconds;
        this.workers = workers;

        // Ensure native C++ binaries are loaded into the JVM seamlessly
        Loader.loadNativeLibraries();
    }

    @Override
    public PlanningResult optimize(PlanningRequest request) {
        PlanningResult result = new PlanningResult();
        result.setPlanningId(request.getPlanningId());
        result.setViolatedConstraints(Collections.emptyList()); // Soft constraints not evaluated yet

        // Validate basic bounds
        if (request.getTasks() == null || request.getTasks().isEmpty()) {
            return fallbackResult(request, "No tasks provided to solver", PlanningDecisionStatus.INFEASIBLE);
        }

        LocalDateTime windowStart = request.getPlanningWindowStart();
        LocalDateTime windowEnd = request.getPlanningWindowEnd();

        if (windowStart == null || windowEnd == null || windowStart.isAfter(windowEnd)) {
            return fallbackResult(request, "Invalid planning window bound", PlanningDecisionStatus.INFEASIBLE);
        }

        int maxWindowMinutes = (int) Duration.between(windowStart, windowEnd).toMinutes();

        CpModel model = new CpModel();

        // Context map to store variables mapped by Task ID
        Map<Long, TaskVariables> taskVarsMap = new HashMap<>();
        Map<Long, List<IntervalVar>> assetIntervalsMap = new HashMap<>();

        // 1. Build variables and individual constraints
        for (PlanningTask task : request.getTasks()) {
            if (task.getDurationMinutes() == null || task.getDurationMinutes() <= 0) {
                // Skips structurally invalid task mapping inside solver
                continue;
            }

            long duration = task.getDurationMinutes();

            // Limit starts to [0, maxBound]
            IntVar startVar = model.newIntVar(0, maxWindowMinutes, "start_" + task.getTaskId());
            IntVar endVar = model.newIntVar(0, maxWindowMinutes, "end_" + task.getTaskId());
            BoolVar scheduledVar = model.newBoolVar("scheduled_" + task.getTaskId());

            // If scheduled => end = start + duration
            model.addEquality(endVar, LinearExpr.newBuilder().add(startVar).add(duration).build())
                    .onlyEnforceIf(scheduledVar);

            // If NOT scheduled => start = 0, end = 0 (to keep domains clean)
            model.addEquality(startVar, 0).onlyEnforceIf(scheduledVar.not());
            model.addEquality(endVar, 0).onlyEnforceIf(scheduledVar.not());

            // Add optional IntervalVar for scheduling engines
            IntervalVar interval = model.newOptionalIntervalVar(startVar, model.newConstant(duration), endVar,
                    scheduledVar, "interval_" + task.getTaskId());

            // Bind task-specific windows if structurally safe
            if (task instanceof RailwayPlanningTask rTask) {
                WorkforceProfile wp = rTask.getWorkforceProfile();
                if (wp != null && wp.getRequiredTeams() != null && wp.getAvailableTeams() != null) {
                    if (wp.getRequiredTeams() > wp.getAvailableTeams()) {
                        model.addEquality(scheduledVar, 0); // Force UNSCHEDULED
                    }
                }

                if (rTask.getSectionId() != null && request.getPlanningContext() != null
                        && request.getPlanningContext().getBlockWindows() != null) {
                    List<MaintenanceBlockWindow> validWindows = request.getPlanningContext().getBlockWindows().stream()
                            .filter(w -> rTask.getSectionId().equals(w.getSectionId()))
                            .filter(w -> w.getStatus() == BlockWindowStatus.AVAILABLE)
                            .toList();

                    if (validWindows.isEmpty()) {
                        model.addEquality(scheduledVar, 0);
                    } else if (validWindows.size() == 1) {
                        MaintenanceBlockWindow w = validWindows.get(0);
                        int wStart = toMinuteOffset(windowStart, w.getWindowStart());
                        int wEnd = toMinuteOffset(windowStart, w.getWindowEnd());
                        model.addGreaterOrEqual(startVar, wStart).onlyEnforceIf(scheduledVar);
                        model.addLessOrEqual(endVar, wEnd).onlyEnforceIf(scheduledVar);
                    } else {
                        List<BoolVar> windowVars = new ArrayList<>();
                        for (int i = 0; i < validWindows.size(); i++) {
                            MaintenanceBlockWindow w = validWindows.get(i);
                            BoolVar wVar = model.newBoolVar("window_" + i + "_" + task.getTaskId());
                            int wStart = toMinuteOffset(windowStart, w.getWindowStart());
                            int wEnd = toMinuteOffset(windowStart, w.getWindowEnd());

                            model.addGreaterOrEqual(startVar, wStart).onlyEnforceIf(wVar);
                            model.addLessOrEqual(endVar, wEnd).onlyEnforceIf(wVar);
                            model.addImplication(wVar, scheduledVar);
                            windowVars.add(wVar);
                        }
                        com.google.ortools.sat.Literal[] arr = windowVars
                                .toArray(new com.google.ortools.sat.Literal[0]);
                        model.addBoolOr(arr).onlyEnforceIf(scheduledVar);
                    }
                }
            }

            if (task.getEarliestStart() != null) {
                int earliestOffset = toMinuteOffset(windowStart, task.getEarliestStart());
                if (earliestOffset > 0) {
                    model.addGreaterOrEqual(startVar, earliestOffset).onlyEnforceIf(scheduledVar);
                }
            }

            if (task.getLatestFinish() != null) {
                int latestOffset = toMinuteOffset(windowStart, task.getLatestFinish());
                if (latestOffset < maxWindowMinutes) {
                    model.addLessOrEqual(endVar, latestOffset).onlyEnforceIf(scheduledVar);
                }
            }

            // M3.16 Partial Replanning: Freeze retained assignments securely
            if (request.getRetainedAssignments() != null) {
                request.getRetainedAssignments().stream()
                        .filter(a -> a.getTaskId().equals(task.getTaskId())
                                && a.getStatus() == PlanningTaskStatus.SCHEDULED)
                        .findFirst()
                        .ifPresent(retained -> {
                            int rStart = toMinuteOffset(windowStart, retained.getAssignedStart());
                            int rEnd = toMinuteOffset(windowStart, retained.getAssignedEnd());

                            model.addEquality(startVar, rStart);
                            model.addEquality(endVar, rEnd);
                            model.addEquality(scheduledVar, 1); // Force assignment
                        });
            }

            taskVarsMap.put(task.getTaskId(), new TaskVariables(task, startVar, endVar, scheduledVar));

            // Group intervals targeting the specific asset
            assetIntervalsMap.computeIfAbsent(task.getAssetId(), k -> new ArrayList<>()).add(interval);
        }

        // 2. Add Non-Overlap constraints for identically targeted assets
        for (Map.Entry<Long, List<IntervalVar>> entry : assetIntervalsMap.entrySet()) {
            List<IntervalVar> intervals = entry.getValue();
            if (intervals.size() > 1) {
                model.addNoOverlap(intervals);
            }
        }

        // 3. Build Object Function
        buildObjective(model, taskVarsMap);

        // 4. Solve Configuration
        CpSolver solver = new CpSolver();
        solver.getParameters().setMaxTimeInSeconds(maxSeconds);
        solver.getParameters().setNumSearchWorkers(workers);

        CpSolverStatus status = solver.solve(model);

        // 5. Build Result Map
        return mapResult(request, solver, status, taskVarsMap, windowStart);
    }

    private void buildObjective(CpModel model, Map<Long, TaskVariables> taskVarsMap) {
        LinearExprBuilder objBuilder = LinearExpr.newBuilder();

        for (TaskVariables vars : taskVarsMap.values()) {
            PlanningTask task = vars.task;

            // Priority 1: Maximize Total Tasks (Extremely High Scaling)
            long presenceWeight = 1000000L;

            // Priority 2: Optimize Higher Value Priorities
            double priority = task.getPriorityScore() != null ? task.getPriorityScore() : 0.0;
            long priorityWeight = Math.round(priority * 1000);

            long scheduledValue = presenceWeight + priorityWeight;

            // Add scheduled reward
            objBuilder.addTerm(vars.scheduledVar, scheduledValue);

            // Priority 3: Minus small offset valuing earlier allocations
            // objBuilder.addTerm(vars.startVar, -1);
        }

        // Apply secondary timing optimizations by maximizing objective where Start
        // Variables act as negative penalty reductions
        for (TaskVariables vars : taskVarsMap.values()) {
            objBuilder.addTerm(vars.startVar, -1);
        }

        model.maximize(objBuilder.build());
    }

    private PlanningResult mapResult(PlanningRequest request, CpSolver solver, CpSolverStatus status,
            Map<Long, TaskVariables> taskVarsMap, LocalDateTime windowStart) {
        PlanningResult result = new PlanningResult();
        result.setPlanningId(request.getPlanningId());
        result.setDecisionStatus(mapStatus(status));

        boolean hasSolution = (status == CpSolverStatus.OPTIMAL || status == CpSolverStatus.FEASIBLE);

        List<PlanAssignment> assignments = new ArrayList<>();

        for (PlanningTask task : request.getTasks()) {
            PlanAssignment assignment = new PlanAssignment();
            assignment.setTaskId(task.getTaskId());
            assignment.setAssignedAssetId(task.getAssetId());

            if (hasSolution && taskVarsMap.containsKey(task.getTaskId())) {
                TaskVariables vars = taskVarsMap.get(task.getTaskId());
                boolean isScheduled = solver.booleanValue(vars.scheduledVar);

                if (isScheduled) {
                    long startOffset = solver.value(vars.startVar);
                    long endOffset = solver.value(vars.endVar);

                    assignment.setStatus(PlanningTaskStatus.SCHEDULED);
                    assignment.setAssignedStart(windowStart.plusMinutes(startOffset));
                    assignment.setAssignedEnd(windowStart.plusMinutes(endOffset));
                } else {
                    assignment.setStatus(PlanningTaskStatus.UNSCHEDULED);
                }
            } else {
                assignment.setStatus(PlanningTaskStatus.UNSCHEDULED);
            }

            assignments.add(assignment);
        }

        result.setAssignments(assignments);
        result.setViolatedConstraints(Collections.emptyList());

        if (hasSolution) {
            result.setObjectiveScore(solver.objectiveValue());
        }

        result.setExplanation(buildExplanation(status));

        return result;
    }

    private int toMinuteOffset(LocalDateTime startBase, LocalDateTime targetTime) {
        return (int) Math.max(0, Duration.between(startBase, targetTime).toMinutes());
    }

    private PlanningDecisionStatus mapStatus(CpSolverStatus status) {
        return switch (status) {
            case OPTIMAL, FEASIBLE -> PlanningDecisionStatus.FEASIBLE;
            case INFEASIBLE -> PlanningDecisionStatus.INFEASIBLE;
            default -> PlanningDecisionStatus.UNKNOWN;
        };
    }

    private PlanningResult fallbackResult(PlanningRequest request, String reason, PlanningDecisionStatus status) {
        PlanningResult r = new PlanningResult();
        r.setPlanningId(request.getPlanningId());
        r.setDecisionStatus(status);
        r.setExplanation(reason);
        r.setAssignments(Collections.emptyList());
        r.setViolatedConstraints(Collections.emptyList());
        return r;
    }

    private String buildExplanation(CpSolverStatus status) {
        return "CP-SAT optimization executed. Status: " + status.name() + ". " +
                "This is the initial RMOS optimization objective (Milestone 3.8 prototype) maximizing bounds safely targeting strictly deterministic integer paths. "
                +
                "It is NOT autonomous railway control and does NOT replace final approval pipelines.";
    }

    private static class TaskVariables {
        final PlanningTask task;
        final IntVar startVar;
        final IntVar endVar;
        final BoolVar scheduledVar;

        TaskVariables(PlanningTask task, IntVar startVar, IntVar endVar, BoolVar scheduledVar) {
            this.task = task;
            this.startVar = startVar;
            this.endVar = endVar;
            this.scheduledVar = scheduledVar;
        }
    }
}
