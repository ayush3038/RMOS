package com.rmos.config;

import com.rmos.simulator.domain.SimulationScenario;
import com.rmos.simulator.domain.SimulationStateEntity;
import com.rmos.simulator.repository.SimulationStateRepository;
import com.rmos.simulator.service.SimulationEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Simulation baseline seeding tests.
 *
 * <p>Regression target: {@code SimulationEngine.processTick()} used
 * {@code findById("SINGLETON_STATE").ifPresent(...)} while nothing created that
 * row, so the 5-second scheduler silently did nothing on a fresh database.
 */
@SpringBootTest(properties = {
        "rmos.ai.chat.provider=none",
        "rmos.ai.nim.enabled=false"
})
@ActiveProfiles("test")
@Transactional
class SimulationDataInitializerTest {

    @Autowired
    private SimulationStateRepository stateRepository;

    @BeforeEach
    void cleanState() {
        stateRepository.deleteAll();
        stateRepository.flush();
    }

    private SimulationDataInitializer initializer(String activeProfile, boolean enabled) {
        MockEnvironment environment = new MockEnvironment();
        if (activeProfile != null) {
            environment.setActiveProfiles(activeProfile);
        }
        return new SimulationDataInitializer(stateRepository, environment, enabled, 8);
    }

    @Test
    void createsTheSingletonStateRowTheEngineReads() {
        assertThat(stateRepository.findById("SINGLETON_STATE")).isEmpty();

        assertThat(initializer("dev", true).initSimulationState()).isTrue();

        SimulationStateEntity state = stateRepository.findById("SINGLETON_STATE").orElseThrow();
        assertThat(state.getId()).isEqualTo("SINGLETON_STATE");
        assertThat(state.getCurrentScenario()).isEqualTo(SimulationScenario.NORMAL_OPERATIONS);
        assertThat(state.getSeed()).isEqualTo(SimulationDataInitializer.DEFAULT_SEED);
        assertThat(state.getIntervalSeconds()).isEqualTo(8);
        // Sequence must be non-null or the engine would NPE on first tick.
        assertThat(state.getCurrentSequence()).isZero();
        // Seeded idle: an administrator still starts it deliberately.
        assertThat(state.isActive()).isFalse();
    }

    @Test
    void repeatedInitializationIsIdempotentAndDoesNotDuplicate() {
        SimulationDataInitializer initializer = initializer("dev", true);

        assertThat(initializer.initSimulationState()).isTrue();
        assertThat(initializer.initSimulationState()).isFalse();
        assertThat(initializer.initSimulationState()).isFalse();

        assertThat(stateRepository.count()).isEqualTo(1);
    }

    @Test
    void existingStateIsNeverOverwrittenOnRestart() {
        SimulationDataInitializer initializer = initializer("dev", true);
        initializer.initSimulationState();

        // Simulate an operator having started and advanced the simulation.
        SimulationStateEntity running = stateRepository.findById("SINGLETON_STATE").orElseThrow();
        running.setActive(true);
        running.setCurrentScenario(SimulationScenario.TRAIN_DELAY);
        running.setCurrentSequence(17L);
        stateRepository.save(running);
        stateRepository.flush();

        assertThat(initializer.initSimulationState()).isFalse();

        SimulationStateEntity after = stateRepository.findById("SINGLETON_STATE").orElseThrow();
        assertThat(after.isActive()).isTrue();
        assertThat(after.getCurrentScenario()).isEqualTo(SimulationScenario.TRAIN_DELAY);
        assertThat(after.getCurrentSequence()).isEqualTo(17L);
    }

    @Test
    void seedingIsSkippedUnderProdProfile() {
        assertThat(initializer("prod", true).initSimulationState()).isFalse();
        assertThat(stateRepository.findById("SINGLETON_STATE")).isEmpty();
    }

    @Test
    void seedingIsSkippedUnderProductionProfile() {
        assertThat(initializer("production", true).initSimulationState()).isFalse();
        assertThat(stateRepository.findById("SINGLETON_STATE")).isEmpty();
    }

    @Test
    void seedingCanBeDisabledExplicitly() {
        assertThat(initializer("dev", false).initSimulationState()).isFalse();
        assertThat(stateRepository.findById("SINGLETON_STATE")).isEmpty();
    }

    private SimulationEngine engine() {
        return new SimulationEngine(stateRepository,
                org.mockito.Mockito.mock(com.rmos.simulator.service.SimulationEventGenerator.class),
                org.mockito.Mockito.mock(com.rmos.service.integration.IncidentMappingService.class));
    }

    @Test
    void seededBaselineMakesTheEngineTickRunWithoutError() {
        initializer("dev", true).initSimulationState();

        SimulationEngine engine = engine();

        // Idle state: the tick must return quietly without throwing or NPE-ing.
        engine.processTick();
        SimulationStateEntity after = stateRepository.findById("SINGLETON_STATE").orElseThrow();
        assertThat(after.getCurrentSequence()).isZero();

        // Active state: the tick advances the sequence.
        after.setActive(true);
        stateRepository.save(after);
        engine.processTick();
        assertThat(stateRepository.findById("SINGLETON_STATE").orElseThrow().getCurrentSequence())
                .isEqualTo(1L);
    }

    @Test
    void engineTickWithoutSeededRowIsANoOpRatherThanAnError() {
        SimulationEngine engine = engine();

        // The engine stays callable even when the row is absent (prod, or
        // seeding disabled) - it must not blow up the scheduler thread.
        engine.processTick();

        Optional<SimulationStateEntity> state = stateRepository.findById("SINGLETON_STATE");
        assertThat(state).isEmpty();
    }
}