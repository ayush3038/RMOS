package com.rmos.service.planning;

import com.rmos.domain.planning.PlanningDecisionStatus;
import com.rmos.domain.planning.PlanningTaskStatus;
import com.rmos.dto.planning.PlanAssignment;
import com.rmos.dto.planning.PlanningRequest;
import com.rmos.dto.planning.PlanningResult;
import com.rmos.dto.planning.PlanningTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ORToolsCpSatOptimizerTest {

    private ORToolsCpSatOptimizer optimizer;

    private final LocalDateTime WINDOW_START = LocalDateTime.of(2025, 1, 1, 0, 0);
    private final LocalDateTime WINDOW_END = LocalDateTime.of(2025, 1, 1, 23, 59);

    @BeforeEach
    void setUp() {
        optimizer = new ORToolsCpSatOptimizer(5, 4);
    }

    private PlanningRequest createEmptyRequest() {
        PlanningRequest req = new PlanningRequest();
        req.setPlanningId("OPT-001");
        req.setPlanningWindowStart(WINDOW_START);
        req.setPlanningWindowEnd(WINDOW_END);
        return req;
    }

    @Test
    void optimize_EmptyRequest_ReturnsInfeasible() {
        PlanningRequest req = createEmptyRequest();
        req.setTasks(Collections.emptyList());

        PlanningResult res = optimizer.optimize(req);

        assertEquals(PlanningDecisionStatus.INFEASIBLE, res.getDecisionStatus());
        assertTrue(res.getExplanation().contains("No tasks provided"));
    }

    @Test
    void optimize_SingleTaskFits_IsScheduled() {
        PlanningRequest req = createEmptyRequest();

        PlanningTask task = new PlanningTask();
        task.setTaskId(1L);
        task.setAssetId(99L);
        task.setDurationMinutes(120);
        task.setPriorityScore(0.5);

        req.setTasks(Collections.singletonList(task));

        PlanningResult res = optimizer.optimize(req);

        assertEquals(PlanningDecisionStatus.FEASIBLE, res.getDecisionStatus());
        assertEquals(1, res.getAssignments().size());

        PlanAssignment assignment = res.getAssignments().get(0);
        assertEquals(PlanningTaskStatus.SCHEDULED, assignment.getStatus());
        assertEquals(WINDOW_START, assignment.getAssignedStart()); // Opt prefers earlier
        assertEquals(WINDOW_START.plusMinutes(120), assignment.getAssignedEnd());
    }

    @Test
    void optimize_SingleTaskOutsideWindow_IsUnscheduled() {
        PlanningRequest req = createEmptyRequest();

        PlanningTask task = new PlanningTask();
        task.setTaskId(1L);
        task.setAssetId(99L);
        task.setDurationMinutes(48 * 60); // 48 hours, doesn't fit in 24 hr window
        task.setPriorityScore(0.5);

        req.setTasks(Collections.singletonList(task));

        PlanningResult res = optimizer.optimize(req);

        assertEquals(1, res.getAssignments().size());
        assertEquals(PlanningTaskStatus.UNSCHEDULED, res.getAssignments().get(0).getStatus());
    }

    @Test
    void optimize_TwoTasksSameAsset_DoNotOverlap() {
        PlanningRequest req = createEmptyRequest();

        PlanningTask t1 = new PlanningTask();
        t1.setTaskId(1L);
        t1.setAssetId(100L); // SAME ASSET
        t1.setDurationMinutes(60);
        t1.setPriorityScore(0.8);

        PlanningTask t2 = new PlanningTask();
        t2.setTaskId(2L);
        t2.setAssetId(100L); // SAME ASSET
        t2.setDurationMinutes(120);
        t2.setPriorityScore(0.6);

        req.setTasks(Arrays.asList(t1, t2));

        PlanningResult res = optimizer.optimize(req);

        assertEquals(PlanningDecisionStatus.FEASIBLE, res.getDecisionStatus());

        PlanAssignment a1 = res.getAssignments().stream().filter(a -> a.getTaskId().equals(1L)).findFirst().get();
        PlanAssignment a2 = res.getAssignments().stream().filter(a -> a.getTaskId().equals(2L)).findFirst().get();

        // Ensure non-overlap
        boolean nonOverlapping = a1.getAssignedEnd().isBefore(a2.getAssignedStart())
                || a1.getAssignedEnd().equals(a2.getAssignedStart()) ||
                a2.getAssignedEnd().isBefore(a1.getAssignedStart())
                || a2.getAssignedEnd().equals(a1.getAssignedStart());

        assertTrue(nonOverlapping, "Tasks targeting the identical asset must never overlap chronologically.");
    }

    @Test
    void optimize_TwoTasksDistinctAssets_CanOverlap() {
        PlanningRequest req = createEmptyRequest();

        PlanningTask t1 = new PlanningTask();
        t1.setTaskId(1L);
        t1.setAssetId(100L); // DISTINCT ASSET
        t1.setDurationMinutes(60);
        t1.setPriorityScore(0.8);

        PlanningTask t2 = new PlanningTask();
        t2.setTaskId(2L);
        t2.setAssetId(200L); // DISTINCT ASSET
        t2.setDurationMinutes(120);
        t2.setPriorityScore(0.6);

        req.setTasks(Arrays.asList(t1, t2));

        PlanningResult res = optimizer.optimize(req);

        PlanAssignment a1 = res.getAssignments().stream().filter(a -> a.getTaskId().equals(1L)).findFirst().get();
        PlanAssignment a2 = res.getAssignments().stream().filter(a -> a.getTaskId().equals(2L)).findFirst().get();

        // Both should trigger optimally near the beginning of the window since they map
        // uniquely
        assertEquals(WINDOW_START, a1.getAssignedStart());
        assertEquals(WINDOW_START, a2.getAssignedStart());
    }

    @Test
    void optimize_HigherPriorityPreferredWhenWindowRestricted() {
        PlanningRequest req = new PlanningRequest();
        req.setPlanningId("OPT-RESTRICTED");
        req.setPlanningWindowStart(WINDOW_START);
        req.setPlanningWindowEnd(WINDOW_START.plusHours(2)); // ONLY 2 hours available

        PlanningTask t1 = new PlanningTask();
        t1.setTaskId(1L);
        t1.setAssetId(100L);
        t1.setDurationMinutes(120); // Takes 2 hours
        t1.setPriorityScore(0.3); // Low Priority

        PlanningTask t2 = new PlanningTask();
        t2.setTaskId(2L);
        t2.setAssetId(100L); // Takes 2 hours on same asset, so only 1 can be chosen
        t2.setDurationMinutes(120);
        t2.setPriorityScore(0.9); // High Priority

        req.setTasks(Arrays.asList(t1, t2));

        PlanningResult res = optimizer.optimize(req);

        PlanAssignment a1 = res.getAssignments().stream().filter(a -> a.getTaskId().equals(1L)).findFirst().get();
        PlanAssignment a2 = res.getAssignments().stream().filter(a -> a.getTaskId().equals(2L)).findFirst().get();

        assertEquals(PlanningTaskStatus.UNSCHEDULED, a1.getStatus());
        assertEquals(PlanningTaskStatus.SCHEDULED, a2.getStatus());
    }

    @Test
    void optimize_EarliestStartRespected() {
        PlanningRequest req = createEmptyRequest();

        PlanningTask task = new PlanningTask();
        task.setTaskId(1L);
        task.setAssetId(99L);
        task.setDurationMinutes(60);
        task.setPriorityScore(0.5);
        task.setEarliestStart(WINDOW_START.plusHours(5)); // Cannot start before T+5 hours

        req.setTasks(Collections.singletonList(task));

        PlanningResult res = optimizer.optimize(req);

        PlanAssignment assignment = res.getAssignments().get(0);
        assertEquals(PlanningTaskStatus.SCHEDULED, assignment.getStatus());
        assertEquals(WINDOW_START.plusHours(5), assignment.getAssignedStart());
    }

    @Test
    void optimize_ExplanationOutputsDeterministicTelemetry() {
        PlanningRequest req = createEmptyRequest();

        PlanningTask task = new PlanningTask();
        task.setTaskId(1L);
        task.setAssetId(99L);
        task.setDurationMinutes(60);
        task.setPriorityScore(0.5);

        req.setTasks(Collections.singletonList(task));

        PlanningResult res = optimizer.optimize(req);

        assertNotNull(res.getExplanation());
        assertTrue(res.getExplanation().contains("CP-SAT optimization executed"));
    }

    @Test
    void optimize_TaskInAvailableBlockWindow_IsScheduled() {
        PlanningRequest req = createEmptyRequest();
        com.rmos.dto.planning.RailwayPlanningContext ctx = new com.rmos.dto.planning.RailwayPlanningContext();
        com.rmos.dto.planning.MaintenanceBlockWindow w = new com.rmos.dto.planning.MaintenanceBlockWindow();
        w.setSectionId("S1");
        w.setStatus(com.rmos.domain.planning.BlockWindowStatus.AVAILABLE);
        w.setWindowStart(WINDOW_START.plusHours(1));
        w.setWindowEnd(WINDOW_START.plusHours(3));
        ctx.setBlockWindows(Collections.singletonList(w));
        req.setPlanningContext(ctx);

        com.rmos.dto.planning.RailwayPlanningTask task = new com.rmos.dto.planning.RailwayPlanningTask();
        task.setTaskId(1L);
        task.setAssetId(99L);
        task.setDurationMinutes(60);
        task.setSectionId("S1");
        req.setTasks(Collections.singletonList(task));

        PlanningResult res = optimizer.optimize(req);
        assertEquals(PlanningTaskStatus.SCHEDULED, res.getAssignments().get(0).getStatus());
        assertTrue(!res.getAssignments().get(0).getAssignedStart().isBefore(WINDOW_START.plusHours(1)));
    }

    @Test
    void optimize_TaskOutsideAvailableWindows_IsUnscheduled() {
        PlanningRequest req = createEmptyRequest();
        com.rmos.dto.planning.RailwayPlanningContext ctx = new com.rmos.dto.planning.RailwayPlanningContext();
        com.rmos.dto.planning.MaintenanceBlockWindow w = new com.rmos.dto.planning.MaintenanceBlockWindow();
        w.setSectionId("S1");
        // Reserved block should not be used
        w.setStatus(com.rmos.domain.planning.BlockWindowStatus.RESERVED);
        w.setWindowStart(WINDOW_START.plusHours(1));
        w.setWindowEnd(WINDOW_START.plusHours(3));
        ctx.setBlockWindows(Collections.singletonList(w));
        req.setPlanningContext(ctx);

        com.rmos.dto.planning.RailwayPlanningTask task = new com.rmos.dto.planning.RailwayPlanningTask();
        task.setTaskId(1L);
        task.setAssetId(99L);
        task.setDurationMinutes(60);
        task.setSectionId("S1");
        req.setTasks(Collections.singletonList(task));

        PlanningResult res = optimizer.optimize(req);
        assertEquals(PlanningTaskStatus.UNSCHEDULED, res.getAssignments().get(0).getStatus());
    }

    @Test
    void optimize_TaskMismatchedSection_IsUnscheduled() {
        PlanningRequest req = createEmptyRequest();
        com.rmos.dto.planning.RailwayPlanningContext ctx = new com.rmos.dto.planning.RailwayPlanningContext();
        com.rmos.dto.planning.MaintenanceBlockWindow w = new com.rmos.dto.planning.MaintenanceBlockWindow();
        w.setSectionId("S2"); // Different section
        w.setStatus(com.rmos.domain.planning.BlockWindowStatus.AVAILABLE);
        w.setWindowStart(WINDOW_START.plusHours(1));
        w.setWindowEnd(WINDOW_START.plusHours(3));
        ctx.setBlockWindows(Collections.singletonList(w));
        req.setPlanningContext(ctx);

        com.rmos.dto.planning.RailwayPlanningTask task = new com.rmos.dto.planning.RailwayPlanningTask();
        task.setTaskId(1L);
        task.setAssetId(99L);
        task.setDurationMinutes(60);
        task.setSectionId("S1");
        req.setTasks(Collections.singletonList(task));

        PlanningResult res = optimizer.optimize(req);
        assertEquals(PlanningTaskStatus.UNSCHEDULED, res.getAssignments().get(0).getStatus());
    }
}
