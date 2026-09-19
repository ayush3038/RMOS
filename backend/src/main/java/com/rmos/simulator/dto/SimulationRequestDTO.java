package com.rmos.simulator.dto;

import com.rmos.simulator.domain.SimulationScenario;

public class SimulationRequestDTO {
    private SimulationScenario scenario;
    private Long seed;
    private Integer intervalSeconds;

    public SimulationScenario getScenario() {
        return scenario;
    }

    public void setScenario(SimulationScenario scenario) {
        this.scenario = scenario;
    }

    public Long getSeed() {
        return seed;
    }

    public void setSeed(Long seed) {
        this.seed = seed;
    }

    public Integer getIntervalSeconds() {
        return intervalSeconds;
    }

    public void setIntervalSeconds(Integer intervalSeconds) {
        this.intervalSeconds = intervalSeconds;
    }
}
