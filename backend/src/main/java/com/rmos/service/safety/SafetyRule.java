package com.rmos.service.safety;

import com.rmos.domain.safety.RuleSeverity;
import com.rmos.domain.safety.SafetyRuleType;
import com.rmos.dto.safety.RuleEvaluation;
import com.rmos.dto.safety.SafetyValidationRequest;

public interface SafetyRule {

    SafetyRuleType getRuleType();

    RuleSeverity getSeverity();

    RuleEvaluation evaluate(SafetyValidationRequest request);
}
