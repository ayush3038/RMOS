package com.rmos.dto.scenario;

import java.util.List;

public class ScenarioComparisonResult {

    private List<ScenarioResult> scenarios;
    private String bestScenarioId;
    private List<ScenarioComparisonMetric> comparisonMetrics;
    private String explanation;

    public List<ScenarioResult> getScenarios() {
        return scenarios;
    }

    public void setScenarios(List<ScenarioResult> scenarios) {
        this.scenarios = scenarios;
    }

    public String getBestScenarioId() {
        return bestScenarioId;
    }

    public void setBestScenarioId(String bestScenarioId) {
        this.bestScenarioId = bestScenarioId;
    }

    public List<ScenarioComparisonMetric> getComparisonMetrics() {
        return comparisonMetrics;
    }

    public void setComparisonMetrics(List<ScenarioComparisonMetric> comparisonMetrics) {
        this.comparisonMetrics = comparisonMetrics;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }
}
