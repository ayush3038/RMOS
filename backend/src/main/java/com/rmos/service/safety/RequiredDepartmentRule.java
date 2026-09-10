package com.rmos.service.safety;

import com.rmos.domain.safety.RuleEvaluationStatus;
import com.rmos.domain.safety.RuleSeverity;
import com.rmos.domain.safety.SafetyRuleType;
import com.rmos.dto.safety.RuleEvaluation;
import com.rmos.dto.safety.SafetyValidationRequest;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(30)
public class RequiredDepartmentRule implements SafetyRule {

    @Override
    public SafetyRuleType getRuleType() {
        return SafetyRuleType.REQUIRED_DEPARTMENT;
    }

    @Override
    public RuleSeverity getSeverity() {
        return RuleSeverity.HARD;
    }

    @Override
    public RuleEvaluation evaluate(SafetyValidationRequest request) {
        if (request.getDepartment() == null || request.getDepartment().trim().isEmpty()) {
            return new RuleEvaluation(getRuleType(), getSeverity(), RuleEvaluationStatus.FAILED,
                    "Department is required and cannot be null or empty.");
        }
        return new RuleEvaluation(getRuleType(), getSeverity(), RuleEvaluationStatus.PASSED, "Department is valid.");
    }
}
