package com.rmos.dto.scenario;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public class ScenarioComparisonRequest {

    @NotEmpty(message = "Scenarios list cannot be empty")
    @Size(min = 2, message = "At least 2 scenarios required for comparison")
    @Valid
    private List<ScenarioResult> scenarios;

    public List<ScenarioResult> getScenarios() {
        return scenarios;
    }

    public void setScenarios(List<ScenarioResult> scenarios) {
        this.scenarios = scenarios;
    }
}
