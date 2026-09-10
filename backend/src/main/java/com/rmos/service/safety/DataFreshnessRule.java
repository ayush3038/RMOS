package com.rmos.service.safety;

import com.rmos.domain.DataFreshness;
import com.rmos.domain.safety.RuleEvaluationStatus;
import com.rmos.domain.safety.RuleSeverity;
import com.rmos.domain.safety.SafetyRuleType;
import com.rmos.dto.safety.RuleEvaluation;
import com.rmos.dto.safety.SafetyValidationRequest;
import com.rmos.service.DataFreshnessService;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(40)
public class DataFreshnessRule implements SafetyRule {

    private final DataFreshnessService freshnessService;

    public DataFreshnessRule(DataFreshnessService freshnessService) {
        this.freshnessService = freshnessService;
    }

    @Override
    public SafetyRuleType getRuleType() {
        return SafetyRuleType.DATA_FRESHNESS;
    }

    @Override
    public RuleSeverity getSeverity() {
        return RuleSeverity.WARNING;
    }

    @Override
    public RuleEvaluation evaluate(SafetyValidationRequest request) {
        if (request.getDataTimestamp() == null) {
            return new RuleEvaluation(getRuleType(), getSeverity(), RuleEvaluationStatus.UNKNOWN,
                    "Data timestamp is null, freshness cannot be determined.");
        }

        DataFreshness freshness = freshnessService.determineFreshness(request.getDataTimestamp());

        if (freshness == DataFreshness.STALE) {
            return new RuleEvaluation(getRuleType(), getSeverity(), RuleEvaluationStatus.WARNING,
                    "Data is stale according to engine freshness constraints.");
        } else if (freshness == DataFreshness.FRESH) {
            return new RuleEvaluation(getRuleType(), getSeverity(), RuleEvaluationStatus.PASSED, "Data is fresh.");
        }

        return new RuleEvaluation(getRuleType(), getSeverity(), RuleEvaluationStatus.UNKNOWN,
                "Data freshness evaluates to unknown.");
    }
}
