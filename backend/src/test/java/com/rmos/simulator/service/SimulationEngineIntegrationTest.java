package com.rmos.simulator.service;

import com.rmos.dto.integration.IncidentContext;
import com.rmos.dto.integration.IncidentMappingResult;
import com.rmos.dto.integration.IncidentMappingStatus;
import com.rmos.service.integration.IncidentMappingService;
import com.rmos.simulator.domain.SimulationScenario;
import com.rmos.simulator.domain.SimulationStateEntity;
import com.rmos.simulator.repository.SimulationStateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SimulationEngineIntegrationTest {

    private SimulationStateRepository stateRepository;
    private SimulationEventGenerator eventGenerator;
    private IncidentMappingService mappingService;
    private SimulationEngine engine;

    @BeforeEach
    void setUp() {
        stateRepository = mock(SimulationStateRepository.class);
        eventGenerator = new SimulationEventGenerator();
        mappingService = mock(IncidentMappingService.class);

        when(mappingService.mapIncidentToPlanningContext(any())).thenReturn(
                new IncidentMappingResult(null, IncidentMappingStatus.MAPPED, null, "Mocked"));

        engine = new SimulationEngine(stateRepository, eventGenerator, mappingService);
    }

    @Test
    void testDeterministicSequenceIncrementsReliably() {
        SimulationStateEntity state = new SimulationStateEntity();
        state.setActive(true);
        state.setSeed(42L);
        state.setCurrentSequence(100L);
        state.setCurrentScenario(SimulationScenario.NORMAL_OPERATIONS);

        when(stateRepository.findById("SINGLETON_STATE")).thenReturn(Optional.of(state));

        engine.processTick();

        assertThat(state.getCurrentSequence()).isEqualTo(101L);
        verify(stateRepository, times(1)).save(state);
    }

    @Test
    void testTrainDelayScenarioTriggersIntegrationMapping() {
        SimulationStateEntity state = new SimulationStateEntity();
        state.setActive(true);
        state.setSeed(42L); // Fixed bounds yield deterministic generator execution limits
        state.setCurrentSequence(1L);
        state.setCurrentScenario(SimulationScenario.TRAIN_DELAY);

        when(stateRepository.findById("SINGLETON_STATE")).thenReturn(Optional.of(state));

        // Advance ticks rapidly to force seeded occurrence hit safely
        for (int i = 0; i < 20; i++) {
            engine.processTick();
        }

        verify(mappingService, atLeastOnce()).mapIncidentToPlanningContext(any(IncidentContext.class));
    }
}
