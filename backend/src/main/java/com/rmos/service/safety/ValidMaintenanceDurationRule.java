package com.rmos.service.safety;

import com.rmos.domain.safety.RuleEvaluationStatus;
import com.rmos.domain.safety.RuleSeverity;
import com.rmos.domain.safety.SafetyRuleType;
import com.rmos.dto.safety.RuleEvaluation;
import com.rmos.dto.safety.SafetyValidationRequest;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(20)
public class ValidMaintenanceDurationRule implements SafetyRule {

    @Override
    public SafetyRuleType getRuleType() {
        return SafetyRuleType.VALID_MAINTENANCE_DURATION;
    }

    @Override
    public RuleSeverity getSeverity() {
        return RuleSeverity.HARD;
    }

    @Override
    public RuleEvaluation evaluate(SafetyValidationRequest request) {
        if (request.getRequestedDurationMinutes() == null) {
            return new RuleEvaluation(getRuleType(), getSeverity(), RuleEvaluationStatus.FAILED,
                    "Duration is required but was null.");
        }
        if (request.getRequestedDurationMinutes() <= 0) {
            return new RuleEvaluation(getRuleType(), getSeverity(), RuleEvaluationStatus.FAILED,
                    "Duration must be strictly positive.");
        }
        return new RuleEvaluation(getRuleType(), getSeverity(), RuleEvaluationStatus.PASSED, "Duration is valid.");
    }
}
