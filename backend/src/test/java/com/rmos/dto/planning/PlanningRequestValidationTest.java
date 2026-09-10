package com.rmos.dto.planning;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlanningRequestValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private PlanningRequest createValidRequest() {
        PlanningRequest request = new PlanningRequest();
        request.setPlanningId("PLAN-001");
        request.setPlanningWindowStart(LocalDateTime.now().plusDays(1));
        request.setPlanningWindowEnd(LocalDateTime.now().plusDays(2));

        PlanningTask task = new PlanningTask();
        task.setTaskId(100L);
        task.setAssetId(50L);
        task.setDepartment("TRACK");
        task.setPriorityScore(0.8);
        task.setDurationMinutes(120);

        request.setTasks(Collections.singletonList(task));

        return request;
    }

    @Test
    void testValidRequest() {
        PlanningRequest request = createValidRequest();
        Set<ConstraintViolation<PlanningRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testMissingPlanningId() {
        PlanningRequest request = createValidRequest();
        request.setPlanningId("");
        Set<ConstraintViolation<PlanningRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    @Test
    void testEmptyTaskCollection() {
        PlanningRequest request = createValidRequest();
        request.setTasks(new ArrayList<>());
        Set<ConstraintViolation<PlanningRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    @Test
    void testInvalidTaskDuration() {
        PlanningRequest request = createValidRequest();
        request.getTasks().get(0).setDurationMinutes(-10);
        Set<ConstraintViolation<PlanningRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    @Test
    void testPriorityScoreOutsideBounds() {
        PlanningRequest request = createValidRequest();
        request.getTasks().get(0).setPriorityScore(1.5);
        Set<ConstraintViolation<PlanningRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());

        request.getTasks().get(0).setPriorityScore(-0.5);
        violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }
}
