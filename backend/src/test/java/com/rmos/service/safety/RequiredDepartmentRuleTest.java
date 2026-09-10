package com.rmos.service.safety;

import com.rmos.domain.safety.RuleEvaluationStatus;
import com.rmos.domain.safety.RuleSeverity;
import com.rmos.domain.safety.SafetyRuleType;
import com.rmos.dto.safety.RuleEvaluation;
import com.rmos.dto.safety.SafetyValidationRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RequiredDepartmentRuleTest {

    private RequiredDepartmentRule rule;

    @BeforeEach
    void setUp() {
        rule = new RequiredDepartmentRule();
    }

    @Test
    void testGetters() {
        assertEquals(SafetyRuleType.REQUIRED_DEPARTMENT, rule.getRuleType());
        assertEquals(RuleSeverity.HARD, rule.getSeverity());
    }

    @Test
    void evaluate_WithDepartment_ReturnsPassed() {
        SafetyValidationRequest request = new SafetyValidationRequest();
        request.setDepartment("OHE");

        RuleEvaluation eval = rule.evaluate(request);

        assertEquals(RuleEvaluationStatus.PASSED, eval.getStatus());
    }

    @Test
    void evaluate_WithEmptyDepartment_ReturnsFailed() {
        SafetyValidationRequest request = new SafetyValidationRequest();
        request.setDepartment("  ");

        RuleEvaluation eval = rule.evaluate(request);

        assertEquals(RuleEvaluationStatus.FAILED, eval.getStatus());
    }

    @Test
    void evaluate_WithoutDepartment_ReturnsFailed() {
        SafetyValidationRequest request = new SafetyValidationRequest();

        RuleEvaluation eval = rule.evaluate(request);

        assertEquals(RuleEvaluationStatus.FAILED, eval.getStatus());
    }
}
