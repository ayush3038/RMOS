package com.rmos.simulator.controller;

import com.rmos.simulator.domain.SimulationScenario;
import com.rmos.simulator.domain.SimulationStateEntity;
import com.rmos.simulator.dto.SimulationRequestDTO;
import com.rmos.simulator.repository.SimulationStateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/v1/simulation")
public class SimulationController {

    private final SimulationStateRepository stateRepository;

    @Autowired
    public SimulationController(SimulationStateRepository stateRepository) {
        this.stateRepository = stateRepository;
    }

    @PostMapping("/start")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<SimulationStateEntity> startSimulation() {
        SimulationStateEntity state = stateRepository.findById("SINGLETON_STATE").orElse(new SimulationStateEntity());
        state.setActive(true);
        if (state.getStartedAt() == null) {
            state.setStartedAt(LocalDateTime.now());
            state.setCurrentSequence(0L); // Ensure monotonically increasing
            if (state.getSeed() == null) {
                state.setSeed(42L);
            }
        }
        return ResponseEntity.ok(stateRepository.save(state));
    }

    @PostMapping("/stop")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<SimulationStateEntity> stopSimulation() {
        SimulationStateEntity state = stateRepository.findById("SINGLETON_STATE").orElse(new SimulationStateEntity());
        state.setActive(false);
        return ResponseEntity.ok(stateRepository.save(state));
    }

    @PostMapping("/reset")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<SimulationStateEntity> resetSimulation() {
        SimulationStateEntity state = stateRepository.findById("SINGLETON_STATE").orElse(new SimulationStateEntity());
        state.setActive(false);
        state.setCurrentSequence(0L);
        state.setStartedAt(null);
        state.setLastEventAt(null);
        state.setCurrentScenario(SimulationScenario.NORMAL_OPERATIONS);
        return ResponseEntity.ok(stateRepository.save(state));
    }

    @GetMapping("/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PLANNER')")
    public ResponseEntity<SimulationStateEntity> getStatus() {
        SimulationStateEntity state = stateRepository.findById("SINGLETON_STATE").orElse(new SimulationStateEntity());
        return ResponseEntity.ok(state);
    }

    @GetMapping("/scenarios")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PLANNER')")
    public ResponseEntity<List<SimulationScenario>> getScenarios() {
        return ResponseEntity.ok(Arrays.asList(SimulationScenario.values()));
    }

    @PostMapping("/scenario")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<SimulationStateEntity> configureScenario(@RequestBody SimulationRequestDTO request) {
        SimulationStateEntity state = stateRepository.findById("SINGLETON_STATE").orElse(new SimulationStateEntity());
        if (request.getScenario() != null)
            state.setCurrentScenario(request.getScenario());
        if (request.getSeed() != null) {
            state.setSeed(request.getSeed());
            // Seed modification safely restarts deterministic sequence trajectory
            // seamlessly
            state.setCurrentSequence(0L);
        }
        if (request.getIntervalSeconds() != null)
            state.setIntervalSeconds(request.getIntervalSeconds());

        return ResponseEntity.ok(stateRepository.save(state));
    }
}
