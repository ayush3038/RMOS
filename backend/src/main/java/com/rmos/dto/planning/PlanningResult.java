package com.rmos.dto.planning;

import com.rmos.domain.planning.PlanningDecisionStatus;

import java.util.List;

public class PlanningResult {

    private String planningId;
    private PlanningDecisionStatus decisionStatus;
    private List<PlanAssignment> assignments;
    private List<PlanningConstraint> violatedConstraints;
    private Double objectiveScore;
    private String explanation;

    public String getPlanningId() {
        return planningId;
    }

    public void setPlanningId(String planningId) {
        this.planningId = planningId;
    }

    public PlanningDecisionStatus getDecisionStatus() {
        return decisionStatus;
    }

    public void setDecisionStatus(PlanningDecisionStatus decisionStatus) {
        this.decisionStatus = decisionStatus;
    }

    public List<PlanAssignment> getAssignments() {
        return assignments;
    }

    public void setAssignments(List<PlanAssignment> assignments) {
        this.assignments = assignments;
    }

    public List<PlanningConstraint> getViolatedConstraints() {
        return violatedConstraints;
    }

    public void setViolatedConstraints(List<PlanningConstraint> violatedConstraints) {
        this.violatedConstraints = violatedConstraints;
    }

    public Double getObjectiveScore() {
        return objectiveScore;
    }

    public void setObjectiveScore(Double objectiveScore) {
        this.objectiveScore = objectiveScore;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }
}
