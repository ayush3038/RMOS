package com.rmos.dto.scenario;

import com.rmos.dto.planning.PlanningRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ScenarioRequest {

    @NotBlank(message = "Scenario ID required")
    private String scenarioId;

    @NotBlank(message = "Scenario name required")
    private String scenarioName;

    @NotNull(message = "Planning request required")
    @Valid
    private PlanningRequest planningRequest;

    @NotNull(message = "Objective profile required")
    @Valid
    private ScenarioObjectiveProfile objectiveProfile;

    public String getScenarioId() {
        return scenarioId;
    }

    public void setScenarioId(String scenarioId) {
        this.scenarioId = scenarioId;
    }

    public String getScenarioName() {
        return scenarioName;
    }

    public void setScenarioName(String scenarioName) {
        this.scenarioName = scenarioName;
    }

    public PlanningRequest getPlanningRequest() {
        return planningRequest;
    }

    public void setPlanningRequest(PlanningRequest planningRequest) {
        this.planningRequest = planningRequest;
    }

    public ScenarioObjectiveProfile getObjectiveProfile() {
        return objectiveProfile;
    }

    public void setObjectiveProfile(ScenarioObjectiveProfile objectiveProfile) {
        this.objectiveProfile = objectiveProfile;
    }
}
