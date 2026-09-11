package com.rmos.dto.planning;

import com.rmos.domain.planning.OperationalConstraintStatus;
import com.rmos.domain.planning.PlanningConstraintType;

public class OperationalConstraintEvaluation {

    private PlanningConstraintType constraintType;
    private OperationalConstraintStatus status;
    private Boolean hard;
    private String message;

    public PlanningConstraintType getConstraintType() {
        return constraintType;
    }

    public void setConstraintType(PlanningConstraintType constraintType) {
        this.constraintType = constraintType;
    }

    public OperationalConstraintStatus getStatus() {
        return status;
    }

    public void setStatus(OperationalConstraintStatus status) {
        this.status = status;
    }

    public Boolean getHard() {
        return hard;
    }

    public void setHard(Boolean hard) {
        this.hard = hard;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
