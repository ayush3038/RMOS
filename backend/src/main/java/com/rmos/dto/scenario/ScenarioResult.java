package com.rmos.dto.scenario;

import com.rmos.domain.scenario.ScenarioStatus;
import com.rmos.dto.planning.PlanningResult;

public class ScenarioResult {

    private String scenarioId;
    private String scenarioName;
    private PlanningResult planningResult;
    private ScenarioMetrics metrics;
    private ScenarioStatus status;
    private String explanation;

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

    public PlanningResult getPlanningResult() {
        return planningResult;
    }

    public void setPlanningResult(PlanningResult planningResult) {
        this.planningResult = planningResult;
    }

    public ScenarioMetrics getMetrics() {
        return metrics;
    }

    public void setMetrics(ScenarioMetrics metrics) {
        this.metrics = metrics;
    }

    public ScenarioStatus getStatus() {
        return status;
    }

    public void setStatus(ScenarioStatus status) {
        this.status = status;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }
}
