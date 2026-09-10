package com.rmos.dto.safety;

import com.rmos.domain.safety.RuleEvaluationStatus;
import com.rmos.domain.safety.RuleSeverity;
import com.rmos.domain.safety.SafetyRuleType;

public class RuleEvaluation {
    private SafetyRuleType ruleType;
    private RuleSeverity severity;
    private RuleEvaluationStatus status;
    private String message;

    public RuleEvaluation() {
    }

    public RuleEvaluation(SafetyRuleType ruleType, RuleSeverity severity, RuleEvaluationStatus status, String message) {
        this.ruleType = ruleType;
        this.severity = severity;
        this.status = status;
        this.message = message;
    }

    public SafetyRuleType getRuleType() {
        return ruleType;
    }

    public void setRuleType(SafetyRuleType ruleType) {
        this.ruleType = ruleType;
    }

    public RuleSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(RuleSeverity severity) {
        this.severity = severity;
    }

    public RuleEvaluationStatus getStatus() {
        return status;
    }

    public void setStatus(RuleEvaluationStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
