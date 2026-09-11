package com.rmos.dto.decision;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PlanReference {

    @NotBlank(message = "planningId is required")
    private String planningId;

    @NotNull(message = "planVersion is required")
    @Min(value = 1, message = "planVersion must be >= 1")
    private Integer planVersion;

    public PlanReference() {
    }

    public PlanReference(String planningId, Integer planVersion) {
        this.planningId = planningId;
        this.planVersion = planVersion;
    }

    public String getPlanningId() {
        return planningId;
    }

    public void setPlanningId(String planningId) {
        this.planningId = planningId;
    }

    public Integer getPlanVersion() {
        return planVersion;
    }

    public void setPlanVersion(Integer planVersion) {
        this.planVersion = planVersion;
    }
}
