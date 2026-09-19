package com.rmos.simulator.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@EnableScheduling
public class SimulationScheduler {

    private final SimulationEngine engine;

    @Autowired
    public SimulationScheduler(SimulationEngine engine) {
        this.engine = engine;
    }

    // Tick configured exactly between Simulator boundaries (5s evaluation)
    @Scheduled(fixedDelay = 5000)
    public void executeSimulationTick() {
        engine.processTick();
    }
}
