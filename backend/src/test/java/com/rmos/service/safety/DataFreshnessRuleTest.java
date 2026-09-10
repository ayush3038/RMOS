package com.rmos.service.safety;

import com.rmos.domain.DataFreshness;
import com.rmos.domain.safety.RuleEvaluationStatus;
import com.rmos.domain.safety.RuleSeverity;
import com.rmos.domain.safety.SafetyRuleType;
import com.rmos.dto.safety.RuleEvaluation;
import com.rmos.dto.safety.SafetyValidationRequest;
import com.rmos.service.DataFreshnessService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataFreshnessRuleTest {

    @Mock
    private DataFreshnessService freshnessService;

    @InjectMocks
    private DataFreshnessRule rule;

    @Test
    void testGetters() {
        assertEquals(SafetyRuleType.DATA_FRESHNESS, rule.getRuleType());
        assertEquals(RuleSeverity.WARNING, rule.getSeverity());
    }

    @Test
    void evaluate_WithNullTimestamp_ReturnsUnknown() {
        SafetyValidationRequest request = new SafetyValidationRequest();

        RuleEvaluation eval = rule.evaluate(request);

        assertEquals(RuleEvaluationStatus.UNKNOWN, eval.getStatus());
    }

    @Test
    void evaluate_WithStaleTimestamp_ReturnsWarning() {
        SafetyValidationRequest request = new SafetyValidationRequest();
        request.setDataTimestamp(LocalDateTime.now().minusHours(2));

        when(freshnessService.determineFreshness(any())).thenReturn(DataFreshness.STALE);

        RuleEvaluation eval = rule.evaluate(request);

        assertEquals(RuleEvaluationStatus.WARNING, eval.getStatus());
    }

    @Test
    void evaluate_WithFreshTimestamp_ReturnsPassed() {
        SafetyValidationRequest request = new SafetyValidationRequest();
        request.setDataTimestamp(LocalDateTime.now().minusMinutes(5));

        when(freshnessService.determineFreshness(any())).thenReturn(DataFreshness.FRESH);

        RuleEvaluation eval = rule.evaluate(request);

        assertEquals(RuleEvaluationStatus.PASSED, eval.getStatus());
    }
}
