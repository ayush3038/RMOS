package com.rmos.config;

import com.rmos.simulator.domain.SimulationScenario;
import com.rmos.simulator.domain.SimulationStateEntity;
import com.rmos.simulator.repository.SimulationStateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

/**
 * Baseline simulation-data initializer.
 *
 * <p><b>What was actually broken.</b> {@code SimulationEngine.processTick()} reads
 * the simulator state with {@code findById("SINGLETON_STATE").ifPresent(...)}.
 * Nothing in the codebase ever created that row - {@code SimulationController}
 * only persisted it lazily when an administrator explicitly called
 * {@code POST /api/v1/simulation/start}. On a fresh database the scheduled tick
 * therefore ran every 5 seconds and silently did nothing: the simulator looked
 * "alive" but produced no events, and {@code GET /simulation/status} answered
 * with an unsaved transient entity. The demo appeared broken with no error at all.
 *
 * <p><b>Fix.</b> Seed the singleton row exactly once on startup so the simulator
 * has a real, deterministic baseline to run from.
 *
 * <p>Safety and idempotency:
 * <ul>
 *   <li>Idempotent - an existing row is never overwritten, so repeated startups
 *       can neither duplicate nor reset simulator state.</li>
 *   <li>Seeded with {@code isActive=false}; the simulation is still started
 *       deliberately through the existing simulator controls.</li>
 *   <li>Disabled under the {@code prod}/{@code production} profiles, so a hosted
 *       deployment never gains synthetic data.</li>
 *   <li>Writes only to {@code simulation_state}; no operational table is touched,
 *       which keeps simulation data isolated from real railway data.</li>
 * </ul>
 *
 * <p>Runs as an {@link ApplicationRunner} rather than {@code @PostConstruct} so it
 * executes after Flyway and JPA are fully initialised.
 */
@Component
public class SimulationDataInitializer implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(SimulationDataInitializer.class);

    /** Fixed key used by the engine, the controller and the scheduler. */
    public static final String SINGLETON_STATE_ID = "SINGLETON_STATE";

    /** Deterministic default so repeated runs produce the same event trajectory. */
    public static final long DEFAULT_SEED = 42L;

    private final SimulationStateRepository stateRepository;
    private final Environment environment;
    private final boolean enabled;
    private final int intervalSeconds;

    public SimulationDataInitializer(SimulationStateRepository stateRepository,
            Environment environment,
            @Value("${rmos.demo.simulation.enabled:true}") boolean enabled,
            @Value("${rmos.demo.simulation.interval-seconds:${SIMULATION_INTERVAL_SEC:8}}") int intervalSeconds) {
        this.stateRepository = stateRepository;
        this.environment = environment;
        this.enabled = enabled;
        this.intervalSeconds = intervalSeconds;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        initSimulationState();
    }

    /**
     * Ensures the simulator baseline exists. Safe to call on every startup.
     *
     * @return {@code true} when a baseline row was created by this call,
     *         {@code false} when one already existed or seeding was skipped.
     */
    @Transactional
    public boolean initSimulationState() {
        if (!enabled) {
            logger.info("Baseline simulation seeding is disabled (rmos.demo.simulation.enabled=false)");
            return false;
        }

        if (isProductionProfileActive()) {
            logger.info("Skipping baseline simulation seeding: a prod profile is active, so synthetic "
                    + "simulation data must not be created on a hosted deployment");
            return false;
        }

        if (stateRepository.findById(SINGLETON_STATE_ID).isPresent()) {
            logger.info("Baseline simulation state '{}' already exists - left unchanged (idempotent)",
                    SINGLETON_STATE_ID);
            return false;
        }

        SimulationStateEntity state = new SimulationStateEntity();
        state.setId(SINGLETON_STATE_ID);
        state.setActive(false);
        state.setCurrentScenario(SimulationScenario.NORMAL_OPERATIONS);
        state.setSeed(DEFAULT_SEED);
        state.setIntervalSeconds(Math.max(1, intervalSeconds));
        state.setCurrentSequence(0L);
        state.setStartedAt(null);
        state.setLastEventAt(null);

        stateRepository.save(state);
        logger.info("Created baseline simulation state '{}' (scenario=NORMAL_OPERATIONS, seed={}, interval={}s, "
                        + "active=false). Start it from the simulator controls when required.",
                SINGLETON_STATE_ID, DEFAULT_SEED, Math.max(1, intervalSeconds));
        return true;
    }

    private boolean isProductionProfileActive() {
        if (environment == null) {
            return false;
        }
        return Arrays.stream(environment.getActiveProfiles())
                .anyMatch(SimulationDataInitializer::isProductionProfile)
                || Arrays.stream(environment.getProperty("spring.profiles.active", "").split(","))
                        .map(String::trim)
                        .anyMatch(SimulationDataInitializer::isProductionProfile);
    }

    private static boolean isProductionProfile(String profile) {
        return "prod".equalsIgnoreCase(profile) || "production".equalsIgnoreCase(profile);
    }
}