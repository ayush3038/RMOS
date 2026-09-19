package com.rmos.simulator.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "simulation_state")
public class SimulationStateEntity {

    @Id
    private String id = "SINGLETON_STATE";

    @Column(name = "is_active", nullable = false)
    private boolean active = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_scenario")
    private SimulationScenario currentScenario;

    @Column(name = "seed")
    private Long seed;

    @Column(name = "interval_seconds")
    private Integer intervalSeconds;

    @Column(name = "current_sequence")
    private Long currentSequence;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "last_event_at")
    private LocalDateTime lastEventAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public SimulationScenario getCurrentScenario() {
        return currentScenario;
    }

    public void setCurrentScenario(SimulationScenario currentScenario) {
        this.currentScenario = currentScenario;
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

    public Long getCurrentSequence() {
        return currentSequence;
    }

    public void setCurrentSequence(Long currentSequence) {
        this.currentSequence = currentSequence;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getLastEventAt() {
        return lastEventAt;
    }

    public void setLastEventAt(LocalDateTime lastEventAt) {
        this.lastEventAt = lastEventAt;
    }
}
