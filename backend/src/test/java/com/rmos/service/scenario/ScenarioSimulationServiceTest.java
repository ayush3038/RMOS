package com.rmos.service.scenario;

import com.rmos.domain.planning.PlanningDecisionStatus;
import com.rmos.domain.planning.PlanningTaskStatus;
import com.rmos.domain.scenario.ScenarioStatus;
import com.rmos.dto.planning.PlanAssignment;
import com.rmos.dto.planning.PlanningRequest;
import com.rmos.dto.planning.PlanningResult;
import com.rmos.dto.planning.PlanningTask;
import com.rmos.dto.planning.RailwayPlanningTask;
import com.rmos.dto.planning.TrainImpactProfile;
import com.rmos.dto.planning.WorkforceProfile;
import com.rmos.dto.scenario.ScenarioRequest;
import com.rmos.dto.scenario.ScenarioResult;
import com.rmos.service.planning.PlanningService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ScenarioSimulationServiceTest {

    private PlanningService planningService;
    private ScenarioSimulationService simulationService;

    @BeforeEach
    void setUp() {
        planningService = mock(PlanningService.class);
        simulationService = new ScenarioSimulationService(planningService);
    }

    @Test
    void testValidSimulationCalculatesMetrics() {
        ScenarioRequest req = new ScenarioRequest();
        req.setScenarioId("SCEN_01");

        PlanningRequest pr = new PlanningRequest();

        RailwayPlanningTask task1 = new RailwayPlanningTask();
        task1.setTaskId(1L);
        task1.setPriorityScore(0.8);
        TrainImpactProfile impact = new TrainImpactProfile();
        impact.setAffectedTrainCount(2);
        task1.setTrainImpactProfile(impact);
        WorkforceProfile wp = new WorkforceProfile();
        wp.setRequiredTeams(1);
        task1.setWorkforceProfile(wp);

        pr.setTasks(Collections.singletonList(task1));
        req.setPlanningRequest(pr);

        PlanningResult pRes = new PlanningResult();
        pRes.setDecisionStatus(PlanningDecisionStatus.FEASIBLE);

        PlanAssignment a1 = new PlanAssignment();
        a1.setTaskId(1L);
        a1.setStatus(PlanningTaskStatus.SCHEDULED);
        a1.setAssignedStart(LocalDateTime.now());
        a1.setAssignedEnd(LocalDateTime.now().plusHours(1));
        pRes.setAssignments(Collections.singletonList(a1));

        when(planningService.plan(any())).thenReturn(pRes);

        ScenarioResult result = simulationService.simulate(req);

        assertEquals(ScenarioStatus.COMPLETED, result.getStatus());
        assertNotNull(result.getMetrics());
        assertEquals(1, result.getMetrics().getScheduledTasks());
        assertEquals(0, result.getMetrics().getUnscheduledTasks());
        assertEquals(0.8, result.getMetrics().getScheduledPrioritySum());
        assertEquals(2, result.getMetrics().getTrainImpactTotal());
        assertEquals(1, result.getMetrics().getWorkforceRequirementTotal());
    }

    @Test
    void testInfeasibleSimulation() {
        ScenarioRequest req = new ScenarioRequest();
        req.setScenarioId("SCEN_02");

        PlanningResult pRes = new PlanningResult();
        pRes.setDecisionStatus(PlanningDecisionStatus.INFEASIBLE);

        when(planningService.plan(any())).thenReturn(pRes);

        ScenarioResult result = simulationService.simulate(req);
        assertEquals(ScenarioStatus.INFEASIBLE, result.getStatus());
    }
}
