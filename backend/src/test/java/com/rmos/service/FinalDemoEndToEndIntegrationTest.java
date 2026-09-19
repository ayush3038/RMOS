package com.rmos.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rmos.simulator.controller.SimulationController;
import com.rmos.simulator.domain.SimulationScenario;
import com.rmos.simulator.domain.SimulationStateEntity;
import com.rmos.simulator.dto.SimulationRequestDTO;
import com.rmos.simulator.repository.SimulationStateRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.rmos.security.JwtUtils;
import com.rmos.security.JwtAuthFilter;
import com.rmos.security.CustomAuthenticationEntryPoint;
import com.rmos.security.CustomAccessDeniedHandler;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SimulationController.class)
@AutoConfigureMockMvc(addFilters = false)
class FinalDemoEndToEndIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SimulationStateRepository simulationStateRepository;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    @MockBean
    private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @MockBean
    private CustomAccessDeniedHandler customAccessDeniedHandler;

    @Test
    @WithMockUser(roles = "ADMIN")
    void testEndToEndDeterministicSimulationFlow() throws Exception {
        SimulationStateEntity state = new SimulationStateEntity();
        state.setSeed(42L);
        state.setCurrentScenario(SimulationScenario.NORMAL_OPERATIONS);

        when(simulationStateRepository.findById("SINGLETON_STATE")).thenReturn(Optional.of(state));
        when(simulationStateRepository.save(any())).thenReturn(state);

        SimulationRequestDTO startReq = new SimulationRequestDTO();
        startReq.setScenario(SimulationScenario.NORMAL_OPERATIONS);
        startReq.setSeed(42L);
        startReq.setIntervalSeconds(5);

        mockMvc.perform(post("/api/v1/simulation/scenario")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(startReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentScenario").value("NORMAL_OPERATIONS"));

        mockMvc.perform(post("/api/v1/simulation/start").with(csrf()))
                .andExpect(status().isOk());
    }
}
