package com.rmos.service.planning;

import com.rmos.domain.planning.PlanningTaskStatus;
import com.rmos.dto.planning.PlanningRequest;
import com.rmos.dto.planning.PlanningResult;
import com.rmos.dto.planning.PlanningTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeterministicPlanningOptimizerTest {

    private DeterministicPlanningOptimizer optimizer;

    @BeforeEach
    void setUp() {
        optimizer = new DeterministicPlanningOptimizer();
    }

    @Test
    void optimize_HigherPriorityAllocatedFirst() {
        PlanningRequest req = new PlanningRequest();
        req.setPlanningId("P1");
        req.setPlanningWindowStart(LocalDateTime.of(2025, 1, 1, 0, 0));
        req.setPlanningWindowEnd(LocalDateTime.of(2025, 1, 1, 4, 0));

        // Low priority task (but inserted first)
        PlanningTask t1 = new PlanningTask();
        t1.setTaskId(1L);
        t1.setDurationMinutes(60);
        t1.setPriorityScore(0.3);

        // High priority task
        PlanningTask t2 = new PlanningTask();
        t2.setTaskId(2L);
        t2.setDurationMinutes(120);
        t2.setPriorityScore(0.9);

        req.setTasks(Arrays.asList(t1, t2));

        PlanningResult result = optimizer.optimize(req);

        // T2 assigned first (00:00 to 02:00)
        assertEquals(2L, result.getAssignments().get(0).getTaskId());
        assertEquals(LocalDateTime.of(2025, 1, 1, 0, 0), result.getAssignments().get(0).getAssignedStart());
        assertEquals(LocalDateTime.of(2025, 1, 1, 2, 0), result.getAssignments().get(0).getAssignedEnd());

        // T1 assigned second (02:00 to 03:00)
        assertEquals(1L, result.getAssignments().get(1).getTaskId());
        assertEquals(LocalDateTime.of(2025, 1, 1, 2, 0), result.getAssignments().get(1).getAssignedStart());
    }

    @Test
    void optimize_InsufficientWindowCausesUnscheduled() {
        PlanningRequest req = new PlanningRequest();
        req.setPlanningId("P2");
        req.setPlanningWindowStart(LocalDateTime.of(2025, 1, 1, 0, 0));
        req.setPlanningWindowEnd(LocalDateTime.of(2025, 1, 1, 1, 0)); // Only 1 hr window

        PlanningTask t1 = new PlanningTask();
        t1.setTaskId(1L);
        t1.setDurationMinutes(120); // 2 hr task
        t1.setPriorityScore(0.5);

        req.setTasks(Arrays.asList(t1));

        PlanningResult result = optimizer.optimize(req);

        assertEquals(1, result.getAssignments().size());
        assertEquals(PlanningTaskStatus.UNSCHEDULED, result.getAssignments().get(0).getStatus());
    }

    @Test
    void optimize_DeterministicExplanationIdentifiesBase() {
        PlanningRequest req = new PlanningRequest();
        req.setPlanningId("P3");
        req.setPlanningWindowStart(LocalDateTime.of(2025, 1, 1, 0, 0));
        req.setPlanningWindowEnd(LocalDateTime.of(2025, 1, 1, 10, 0));

        PlanningTask t1 = new PlanningTask();
        t1.setTaskId(1L);
        t1.setDurationMinutes(60);
        t1.setPriorityScore(0.5);
        req.setTasks(Arrays.asList(t1));

        PlanningResult result = optimizer.optimize(req);

        assertTrue(result.getExplanation().contains("BASELINE OPTIMIZER"));
        assertTrue(result.getExplanation().contains("No true constraint optimization performed"));
    }
}
