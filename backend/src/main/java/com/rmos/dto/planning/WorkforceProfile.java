package com.rmos.dto.planning;

import com.rmos.domain.priority.WorkforceLevel;
import jakarta.validation.constraints.Min;

public class WorkforceProfile {

    private WorkforceLevel workforceLevel;

    @Min(value = 0, message = "Available teams cannot be negative")
    private Integer availableTeams;

    @Min(value = 0, message = "Required teams cannot be negative")
    private Integer requiredTeams;

    public WorkforceLevel getWorkforceLevel() {
        return workforceLevel;
    }

    public void setWorkforceLevel(WorkforceLevel workforceLevel) {
        this.workforceLevel = workforceLevel;
    }

    public Integer getAvailableTeams() {
        return availableTeams;
    }

    public void setAvailableTeams(Integer availableTeams) {
        this.availableTeams = availableTeams;
    }

    public Integer getRequiredTeams() {
        return requiredTeams;
    }

    public void setRequiredTeams(Integer requiredTeams) {
        this.requiredTeams = requiredTeams;
    }
}
