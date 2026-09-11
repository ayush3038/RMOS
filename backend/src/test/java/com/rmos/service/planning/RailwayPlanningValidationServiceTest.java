package com.rmos.service.planning;

import com.rmos.domain.planning.BlockWindowStatus;
import com.rmos.domain.planning.OperationalConstraintStatus;
import com.rmos.domain.planning.PlanningConstraintType;
import com.rmos.dto.planning.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RailwayPlanningValidationServiceTest {

    private RailwayPlanningValidationService validationService;

    @BeforeEach
    void setUp() {
        validationService = new RailwayPlanningValidationService();
    }

    @Test
    void testValidContext() {
        PlanningRequest req = new PlanningRequest();
        RailwayPlanningContext context = new RailwayPlanningContext();
        context.setPlanningWindowStart(LocalDateTime.now());
        context.setPlanningWindowEnd(LocalDateTime.now().plusHours(5));

        RailwaySection section = new RailwaySection();
        section.setSectionId("S1");
        context.setSections(Collections.singletonList(section));

        MaintenanceBlockWindow bw = new MaintenanceBlockWindow();
        bw.setBlockId("B1");
        bw.setSectionId("S1");
        bw.setWindowStart(LocalDateTime.now().plusHours(1));
        bw.setWindowEnd(LocalDateTime.now().plusHours(4));
        bw.setStatus(BlockWindowStatus.AVAILABLE);
        context.setBlockWindows(Collections.singletonList(bw));

        RailwayPlanningTask task = new RailwayPlanningTask();
        task.setTaskId(1L);
        task.setSectionId("S1");

        WorkforceProfile wp = new WorkforceProfile();
        wp.setAvailableTeams(5);
        wp.setRequiredTeams(2);
        task.setWorkforceProfile(wp);

        req.setPlanningContext(context);
        req.setTasks(Collections.singletonList(task));

        List<OperationalConstraintEvaluation> results = validationService.validateContext(req);
        assertTrue(results.isEmpty(), "Expected no validation errors");
    }

    @Test
    void testUnknownSection() {
        PlanningRequest req = new PlanningRequest();
        RailwayPlanningContext context = new RailwayPlanningContext();
        context.setPlanningWindowStart(LocalDateTime.now());
        context.setPlanningWindowEnd(LocalDateTime.now().plusHours(5));

        RailwayPlanningTask task = new RailwayPlanningTask();
        task.setTaskId(1L);
        task.setSectionId("UNKNOWN_S1");

        req.setPlanningContext(context);
        req.setTasks(Collections.singletonList(task));

        List<OperationalConstraintEvaluation> results = validationService.validateContext(req);
        assertEquals(1, results.size());
        assertEquals(PlanningConstraintType.SECTION_AVAILABILITY, results.get(0).getConstraintType());
    }

    @Test
    void testBlockWindowOutsidePlanningWindow() {
        PlanningRequest req = new PlanningRequest();
        RailwayPlanningContext context = new RailwayPlanningContext();
        LocalDateTime pStart = LocalDateTime.now();
        context.setPlanningWindowStart(pStart);
        context.setPlanningWindowEnd(pStart.plusHours(5));

        MaintenanceBlockWindow bw = new MaintenanceBlockWindow();
        bw.setBlockId("B1");
        bw.setWindowStart(pStart.minusHours(1));
        bw.setWindowEnd(pStart.plusHours(4));
        context.setBlockWindows(Collections.singletonList(bw));
        req.setPlanningContext(context);

        List<OperationalConstraintEvaluation> results = validationService.validateContext(req);
        assertEquals(1, results.size());
        assertTrue(results.get(0).getMessage().contains("starts before planning window"));
    }

    @Test
    void testInvalidWorkforceValues() {
        PlanningRequest req = new PlanningRequest();
        RailwayPlanningContext context = new RailwayPlanningContext();
        context.setPlanningWindowStart(LocalDateTime.now());
        context.setPlanningWindowEnd(LocalDateTime.now().plusHours(5));

        RailwayPlanningTask task = new RailwayPlanningTask();
        task.setTaskId(1L);
        WorkforceProfile wp = new WorkforceProfile();
        wp.setAvailableTeams(2);
        wp.setRequiredTeams(5);
        task.setWorkforceProfile(wp);

        req.setPlanningContext(context);
        req.setTasks(Collections.singletonList(task));

        List<OperationalConstraintEvaluation> results = validationService.validateContext(req);
        assertEquals(1, results.size());
        assertEquals(PlanningConstraintType.WORKFORCE_CAPACITY, results.get(0).getConstraintType());
    }
}
