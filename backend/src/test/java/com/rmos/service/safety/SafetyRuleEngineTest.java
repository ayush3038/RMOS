package com.rmos.service.safety;

import com.rmos.domain.safety.RuleEvaluationStatus;
import com.rmos.domain.safety.RuleSeverity;
import com.rmos.domain.safety.SafetyRuleType;
import com.rmos.dto.safety.RuleEvaluation;
import com.rmos.dto.safety.SafetyValidationRequest;
import com.rmos.dto.safety.SafetyValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SafetyRuleEngineTest {

    private SafetyRuleEngine engine;
    private SafetyRule hardRule1;
    private SafetyRule hardRule2;
    private SafetyRule warningRule;

    @BeforeEach
    void setUp() {
        hardRule1 = mock(SafetyRule.class);
        when(hardRule1.getSeverity()).thenReturn(RuleSeverity.HARD);

        hardRule2 = mock(SafetyRule.class);
        when(hardRule2.getSeverity()).thenReturn(RuleSeverity.HARD);

        warningRule = mock(SafetyRule.class);
        when(warningRule.getSeverity()).thenReturn(RuleSeverity.WARNING);

        engine = new SafetyRuleEngine(Arrays.asList(hardRule1, hardRule2, warningRule));
    }

    @Test
    void validate_AllRulesPass_ReturnsValidTrue() {
        when(hardRule1.evaluate(any())).thenReturn(new RuleEvaluation(SafetyRuleType.REQUIRED_ASSET, RuleSeverity.HARD,
                RuleEvaluationStatus.PASSED, "Passed"));
        when(hardRule2.evaluate(any())).thenReturn(new RuleEvaluation(SafetyRuleType.REQUIRED_DEPARTMENT,
                RuleSeverity.HARD, RuleEvaluationStatus.PASSED, "Passed"));
        when(warningRule.evaluate(any())).thenReturn(new RuleEvaluation(SafetyRuleType.DATA_FRESHNESS,
                RuleSeverity.WARNING, RuleEvaluationStatus.PASSED, "Passed"));

        SafetyValidationResult result = engine.validate(new SafetyValidationRequest());

        assertTrue(result.isValid());
        assertEquals(3, result.getEvaluations().size());
    }

    @Test
    void validate_OneHardRuleFails_ReturnsValidFalse() {
        when(hardRule1.evaluate(any())).thenReturn(new RuleEvaluation(SafetyRuleType.REQUIRED_ASSET, RuleSeverity.HARD,
                RuleEvaluationStatus.PASSED, "Passed"));
        when(hardRule2.evaluate(any())).thenReturn(new RuleEvaluation(SafetyRuleType.REQUIRED_DEPARTMENT,
                RuleSeverity.HARD, RuleEvaluationStatus.FAILED, "Failed"));
        when(warningRule.evaluate(any())).thenReturn(new RuleEvaluation(SafetyRuleType.DATA_FRESHNESS,
                RuleSeverity.WARNING, RuleEvaluationStatus.PASSED, "Passed"));

        SafetyValidationResult result = engine.validate(new SafetyValidationRequest());

        assertFalse(result.isValid());
    }

    @Test
    void validate_WarningExistsButHardRulesPass_ReturnsValidTrue() {
        when(hardRule1.evaluate(any())).thenReturn(new RuleEvaluation(SafetyRuleType.REQUIRED_ASSET, RuleSeverity.HARD,
                RuleEvaluationStatus.PASSED, "Passed"));
        when(hardRule2.evaluate(any())).thenReturn(new RuleEvaluation(SafetyRuleType.REQUIRED_DEPARTMENT,
                RuleSeverity.HARD, RuleEvaluationStatus.PASSED, "Passed"));
        when(warningRule.evaluate(any())).thenReturn(new RuleEvaluation(SafetyRuleType.DATA_FRESHNESS,
                RuleSeverity.WARNING, RuleEvaluationStatus.WARNING, "Warning"));

        SafetyValidationResult result = engine.validate(new SafetyValidationRequest());

        assertTrue(result.isValid());
    }

    @Test
    void validate_UnknownResultPreserved() {
        when(hardRule1.evaluate(any())).thenReturn(new RuleEvaluation(SafetyRuleType.REQUIRED_ASSET, RuleSeverity.HARD,
                RuleEvaluationStatus.PASSED, "Passed"));
        when(hardRule2.evaluate(any())).thenReturn(new RuleEvaluation(SafetyRuleType.REQUIRED_DEPARTMENT,
                RuleSeverity.HARD, RuleEvaluationStatus.PASSED, "Passed"));
        when(warningRule.evaluate(any())).thenReturn(new RuleEvaluation(SafetyRuleType.DATA_FRESHNESS,
                RuleSeverity.WARNING, RuleEvaluationStatus.UNKNOWN, "Unknown"));

        SafetyValidationResult result = engine.validate(new SafetyValidationRequest());

        assertTrue(result.isValid());
        assertEquals(RuleEvaluationStatus.UNKNOWN, result.getEvaluations().get(2).getStatus());
    }
}
