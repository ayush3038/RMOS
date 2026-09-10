package com.rmos.service.safety;

import com.rmos.domain.safety.RuleEvaluationStatus;
import com.rmos.domain.safety.RuleSeverity;
import com.rmos.dto.safety.RuleEvaluation;
import com.rmos.dto.safety.SafetyValidationRequest;
import com.rmos.dto.safety.SafetyValidationResult;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SafetyRuleEngine {

    private final List<SafetyRule> rules;

    public SafetyRuleEngine(List<SafetyRule> rules) {
        // rules will be injected in order assuming @Order is used on implementations
        this.rules = rules;
    }

    public SafetyValidationResult validate(SafetyValidationRequest request) {
        SafetyValidationResult result = new SafetyValidationResult();
        boolean isValid = true;

        for (SafetyRule rule : rules) {
            RuleEvaluation evaluation = rule.evaluate(request);
            result.addEvaluation(evaluation);

            if (rule.getSeverity() == RuleSeverity.HARD && evaluation.getStatus() == RuleEvaluationStatus.FAILED) {
                isValid = false;
            }
        }

        result.setValid(isValid);
        return result;
    }
}
