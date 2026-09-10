package com.rmos.service.safety;

import com.rmos.domain.safety.RuleEvaluationStatus;
import com.rmos.domain.safety.RuleSeverity;
import com.rmos.domain.safety.SafetyRuleType;
import com.rmos.dto.safety.RuleEvaluation;
import com.rmos.dto.safety.SafetyValidationRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ValidMaintenanceDurationRuleTest {

    private ValidMaintenanceDurationRule rule;

    @BeforeEach
    void setUp() {
        rule = new ValidMaintenanceDurationRule();
    }

    @Test
    void testGetters() {
        assertEquals(SafetyRuleType.VALID_MAINTENANCE_DURATION, rule.getRuleType());
        assertEquals(RuleSeverity.HARD, rule.getSeverity());
    }

    @Test
    void evaluate_WithPositiveDuration_ReturnsPassed() {
        SafetyValidationRequest request = new SafetyValidationRequest();
        request.setRequestedDurationMinutes(60);

        RuleEvaluation eval = rule.evaluate(request);

        assertEquals(RuleEvaluationStatus.PASSED, eval.getStatus());
    }

    @Test
    void evaluate_WithZeroDuration_ReturnsFailed() {
        SafetyValidationRequest request = new SafetyValidationRequest();
        request.setRequestedDurationMinutes(0);

        RuleEvaluation eval = rule.evaluate(request);

        assertEquals(RuleEvaluationStatus.FAILED, eval.getStatus());
    }

    @Test
    void evaluate_WithNegativeDuration_ReturnsFailed() {
        SafetyValidationRequest request = new SafetyValidationRequest();
        request.setRequestedDurationMinutes(-10);

        RuleEvaluation eval = rule.evaluate(request);

        assertEquals(RuleEvaluationStatus.FAILED, eval.getStatus());
    }

    @Test
    void evaluate_WithoutDuration_ReturnsFailed() {
        SafetyValidationRequest request = new SafetyValidationRequest();

        RuleEvaluation eval = rule.evaluate(request);

        assertEquals(RuleEvaluationStatus.FAILED, eval.getStatus());
    }
}
