package com.rmos.service.safety;

import com.rmos.domain.safety.RuleEvaluationStatus;
import com.rmos.domain.safety.RuleSeverity;
import com.rmos.domain.safety.SafetyRuleType;
import com.rmos.dto.safety.RuleEvaluation;
import com.rmos.dto.safety.SafetyValidationRequest;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(10)
public class RequiredAssetRule implements SafetyRule {

    @Override
    public SafetyRuleType getRuleType() {
        return SafetyRuleType.REQUIRED_ASSET;
    }

    @Override
    public RuleSeverity getSeverity() {
        return RuleSeverity.HARD;
    }

    @Override
    public RuleEvaluation evaluate(SafetyValidationRequest request) {
        if (request.getAssetId() == null) {
            return new RuleEvaluation(getRuleType(), getSeverity(), RuleEvaluationStatus.FAILED,
                    "Asset ID is required but was null.");
        }
        return new RuleEvaluation(getRuleType(), getSeverity(), RuleEvaluationStatus.PASSED, "Asset ID is present.");
    }
}
