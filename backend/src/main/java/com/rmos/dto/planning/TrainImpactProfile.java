package com.rmos.dto.planning;

import com.rmos.domain.priority.ImpactLevel;
import jakarta.validation.constraints.Min;

public class TrainImpactProfile {

    private ImpactLevel impactLevel;

    @Min(value = 0, message = "Affected train count cannot be negative")
    private Integer affectedTrainCount;

    @Min(value = 0, message = "Affected service minutes cannot be negative")
    private Integer affectedServiceMinutes;

    public ImpactLevel getImpactLevel() {
        return impactLevel;
    }

    public void setImpactLevel(ImpactLevel impactLevel) {
        this.impactLevel = impactLevel;
    }

    public Integer getAffectedTrainCount() {
        return affectedTrainCount;
    }

    public void setAffectedTrainCount(Integer affectedTrainCount) {
        this.affectedTrainCount = affectedTrainCount;
    }

    public Integer getAffectedServiceMinutes() {
        return affectedServiceMinutes;
    }

    public void setAffectedServiceMinutes(Integer affectedServiceMinutes) {
        this.affectedServiceMinutes = affectedServiceMinutes;
    }
}
