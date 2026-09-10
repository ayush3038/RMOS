package com.rmos.service.safety;

import com.rmos.domain.safety.RuleEvaluationStatus;
import com.rmos.domain.safety.RuleSeverity;
import com.rmos.domain.safety.SafetyRuleType;
import com.rmos.dto.safety.RuleEvaluation;
import com.rmos.dto.safety.SafetyValidationRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RequiredAssetRuleTest {

    private RequiredAssetRule rule;

    @BeforeEach
    void setUp() {
        rule = new RequiredAssetRule();
    }

    @Test
    void testGetters() {
        assertEquals(SafetyRuleType.REQUIRED_ASSET, rule.getRuleType());
        assertEquals(RuleSeverity.HARD, rule.getSeverity());
    }

    @Test
    void evaluate_WithAssetId_ReturnsPassed() {
        SafetyValidationRequest request = new SafetyValidationRequest();
        request.setAssetId(10L);

        RuleEvaluation eval = rule.evaluate(request);

        assertEquals(RuleEvaluationStatus.PASSED, eval.getStatus());
    }

    @Test
    void evaluate_WithoutAssetId_ReturnsFailed() {
        SafetyValidationRequest request = new SafetyValidationRequest();

        RuleEvaluation eval = rule.evaluate(request);

        assertEquals(RuleEvaluationStatus.FAILED, eval.getStatus());
    }
}
