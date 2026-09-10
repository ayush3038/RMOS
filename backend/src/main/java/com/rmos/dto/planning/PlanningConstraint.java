package com.rmos.dto.planning;

import com.rmos.domain.planning.PlanningConstraintType;
import jakarta.validation.constraints.NotNull;

public class PlanningConstraint {

    @NotNull
    private PlanningConstraintType type;

    private boolean hard;

    private String description;

    public PlanningConstraintType getType() {
        return type;
    }

    public void setType(PlanningConstraintType type) {
        this.type = type;
    }

    public boolean isHard() {
        return hard;
    }

    public void setHard(boolean hard) {
        this.hard = hard;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
