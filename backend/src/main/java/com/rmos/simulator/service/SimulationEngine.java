package com.rmos.simulator.service;

import com.rmos.dto.integration.IncidentContext;
import com.rmos.simulator.domain.SimulationScenario;
import com.rmos.simulator.domain.SimulationStateEntity;
import com.rmos.simulator.repository.SimulationStateRepository;
import com.rmos.service.integration.IncidentMappingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class SimulationEngine {

    private static final Logger logger = LoggerFactory.getLogger(SimulationEngine.class);

    /** Deterministic fallback seed, matching SimulationDataInitializer.DEFAULT_SEED. */
    private static final long DEFAULT_SEED = 42L;

    /** Key of the singleton simulator state row seeded by SimulationDataInitializer. */
    public static final String SINGLETON_STATE_ID = "SINGLETON_STATE";

    private final SimulationStateRepository stateRepository;
    private final SimulationEventGenerator eventGenerator;
    private final IncidentMappingService mappingService;

    private Random deterministicRandom;
    /** Warn only once per process so a missing row cannot spam the log every 5s. */
    private volatile boolean missingStateWarned;

    @Autowired
    public SimulationEngine(SimulationStateRepository stateRepository,
            SimulationEventGenerator eventGenerator,
            IncidentMappingService mappingService) {
        this.stateRepository = stateRepository;
        this.eventGenerator = eventGenerator;
        this.mappingService = mappingService;

        initializeRandomState();
    }

    private void initializeRandomState() {
        stateRepository.findById(SINGLETON_STATE_ID).ifPresent(state -> {
            if (state.getSeed() != null) {
                this.deterministicRandom = new Random(state.getSeed());
            } else {
                this.deterministicRandom = new Random(DEFAULT_SEED);
            }
        });
        if (this.deterministicRandom == null) {
            this.deterministicRandom = new Random(DEFAULT_SEED);
        }
    }

    @Transactional
    public void processTick() {
        SimulationStateEntity state = stateRepository.findById(SINGLETON_STATE_ID).orElse(null);

        if (state == null) {
            // Previously a silent no-op on every tick, which made a fresh
            // simulation database look broken with no diagnostic at all.
            if (!missingStateWarned) {
                missingStateWarned = true;
                logger.warn("Simulation state '{}' is missing - the simulator is idle. "
                        + "Start the backend once so SimulationDataInitializer can seed the baseline row.",
                        SINGLETON_STATE_ID);
            }
            return;
        }

        if (!state.isActive())
            return;

        if (this.deterministicRandom == null) {
            initializeRandomState();
        }

        Long currentSequence = state.getCurrentSequence() == null ? 0L : state.getCurrentSequence();
        Long nextSeq = currentSequence + 1;
        state.setCurrentSequence(nextSeq);
        state.setLastEventAt(LocalDateTime.now());

        IncidentContext event = generateScenarioEvent(state, nextSeq);
        if (event != null) {
            // Post into integration bounds directly
            logger.debug("[SIMULATOR][{}] Emitting event {}", state.getCurrentScenario(), nextSeq);
            mappingService.mapIncidentToPlanningContext(event);
        }

        stateRepository.save(state);
    }

    private IncidentContext generateScenarioEvent(SimulationStateEntity state, Long sequence) {
        if (state.getCurrentScenario() == null)
            return null;

        switch (state.getCurrentScenario()) {
            case TRAIN_DELAY:
                return executeTrainDelayScenario(sequence);
            case URGENT_MAINTENANCE:
                return executeUrgentMaintenanceScenario(sequence);
            case BLOCK_UNAVAILABLE:
                return executeBlockUnavailableScenario(sequence);
            case MAINTENANCE_OVERRUN:
                return executeMaintenanceOverrunScenario(sequence);
            case MULTI_DEPARTMENT_COORDINATION:
                return executeMultiDepartmentScenario(sequence);
            case NORMAL_OPERATIONS:
            default:
                // Nominal execution without explicit disruptions
                return null;
        }
    }

    private IncidentContext executeTrainDelayScenario(Long sequence) {
        // Deterministic condition checks (Example: triggers at modulo bounds or random
        // thresholds!)
        if (deterministicRandom.nextInt(100) > 80) { // 20% event trigger per tick
            IncidentContext ctx = eventGenerator.generateEventContext(sequence, "TRAIN_DELAY", "TRAIN_DELAY", 101L);
            ctx.setImpactWindowStart(LocalDateTime.now().plusMinutes(10));
            ctx.setImpactWindowEnd(LocalDateTime.now().plusMinutes(120));
            ctx.setSeverity("HIGH");
            return ctx;
        }
        return null;
    }

    private IncidentContext executeUrgentMaintenanceScenario(Long sequence) {
        if (deterministicRandom.nextInt(100) > 85) {
            IncidentContext ctx = eventGenerator.generateEventContext(sequence, "URGENT_MAINTENANCE",
                    "MAINTENANCE_UPDATE", 205L);
            ctx.setImpactWindowStart(LocalDateTime.now().minusMinutes(5));
            ctx.setImpactWindowEnd(LocalDateTime.now().plusMinutes(240));
            ctx.setSeverity("CRITICAL");
            return ctx;
        }
        return null;
    }

    private IncidentContext executeBlockUnavailableScenario(Long sequence) {
        if (deterministicRandom.nextInt(100) > 90) {
            IncidentContext ctx = eventGenerator.generateEventContext(sequence, "BLOCK_UNAVAILABLE",
                    "UNAVAILABLE_WINDOW", 301L);
            ctx.setImpactWindowStart(LocalDateTime.now());
            ctx.setImpactWindowEnd(LocalDateTime.now().plusHours(48));
            ctx.setSeverity("HIGH");
            return ctx;
        }
        return null;
    }

    private IncidentContext executeMaintenanceOverrunScenario(Long sequence) {
        if (deterministicRandom.nextInt(100) > 95) {
            IncidentContext ctx = eventGenerator.generateEventContext(sequence, "MAINTENANCE_OVERRUN",
                    "OVERRUN_TRACKING", 401L);
            ctx.setImpactWindowStart(LocalDateTime.now().minusHours(1));
            ctx.setImpactWindowEnd(LocalDateTime.now().plusHours(2));
            ctx.setSeverity("MEDIUM");
            return ctx;
        }
        return null;
    }

    private IncidentContext executeMultiDepartmentScenario(Long sequence) {
        if (deterministicRandom.nextInt(100) > 90) {
            IncidentContext ctx = eventGenerator.generateEventContext(sequence, "MULTI_DEPARTMENT_COORDINATION",
                    "MULTI_DEPT_ALERT", 501L);
            ctx.setImpactWindowStart(LocalDateTime.now());
            ctx.setImpactWindowEnd(LocalDateTime.now().plusHours(8));
            ctx.setSeverity("LOW");
            return ctx;
        }
        return null;
    }
}
