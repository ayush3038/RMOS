package com.rmos.dto.safety;

import java.util.ArrayList;
import java.util.List;

public class SafetyValidationResult {
    private boolean valid;
    private List<RuleEvaluation> evaluations = new ArrayList<>();

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public List<RuleEvaluation> getEvaluations() {
        return evaluations;
    }

    public void setEvaluations(List<RuleEvaluation> evaluations) {
        this.evaluations = evaluations;
    }

    public void addEvaluation(RuleEvaluation evaluation) {
        if (this.evaluations == null) {
            this.evaluations = new ArrayList<>();
        }
        this.evaluations.add(evaluation);
    }
}
