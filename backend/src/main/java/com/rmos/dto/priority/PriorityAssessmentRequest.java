package com.rmos.dto.priority;

import com.rmos.domain.priority.ImpactLevel;
import com.rmos.domain.priority.SafetyLevel;
import com.rmos.domain.priority.WorkforceLevel;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class PriorityAssessmentRequest {

    @NotNull(message = "Task ID must not be null")
    private Long taskId;

    @NotNull(message = "Safety Level must not be null")
    private SafetyLevel safetyLevel;

    @NotNull(message = "Train Impact Level must not be null")
    private ImpactLevel trainImpactLevel;

    @NotNull(message = "Criticality must not be null")
    private ImpactLevel criticality;

    @NotNull(message = "Urgency must not be null")
    private ImpactLevel urgency;

    @NotNull(message = "Duration must not be null")
    @Positive(message = "Duration must be positive")
    private Integer durationMinutes;

    @NotNull(message = "Workforce level must not be null")
    private WorkforceLevel workforceRequired;

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public SafetyLevel getSafetyLevel() {
        return safetyLevel;
    }

    public void setSafetyLevel(SafetyLevel safetyLevel) {
        this.safetyLevel = safetyLevel;
    }

    public ImpactLevel getTrainImpactLevel() {
        return trainImpactLevel;
    }

    public void setTrainImpactLevel(ImpactLevel trainImpactLevel) {
        this.trainImpactLevel = trainImpactLevel;
    }

    public ImpactLevel getCriticality() {
        return criticality;
    }

    public void setCriticality(ImpactLevel criticality) {
        this.criticality = criticality;
    }

    public ImpactLevel getUrgency() {
        return urgency;
    }

    public void setUrgency(ImpactLevel urgency) {
        this.urgency = urgency;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public WorkforceLevel getWorkforceRequired() {
        return workforceRequired;
    }

    public void setWorkforceRequired(WorkforceLevel workforceRequired) {
        this.workforceRequired = workforceRequired;
    }
}
