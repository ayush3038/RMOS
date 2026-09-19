package com.rmos.controller.scenario;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rmos.domain.scenario.ScenarioStatus;
import com.rmos.dto.planning.PlanningRequest;
import com.rmos.dto.scenario.ScenarioComparisonRequest;
import com.rmos.dto.scenario.ScenarioComparisonResult;
import com.rmos.dto.scenario.ScenarioObjectiveProfile;
import com.rmos.dto.scenario.ScenarioRequest;
import com.rmos.dto.scenario.ScenarioResult;
import com.rmos.service.scenario.ScenarioComparisonService;
import com.rmos.service.scenario.ScenarioSimulationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@org.springframework.context.annotation.Import({com.rmos.security.SecurityConfig.class, com.rmos.security.JwtAuthFilter.class, com.rmos.security.CustomAuthenticationEntryPoint.class, com.rmos.security.CustomAccessDeniedHandler.class})
@WebMvcTest(ScenarioController.class)
@org.springframework.security.test.context.support.WithMockUser(roles = "ADMIN")
class ScenarioControllerTest {
    @org.springframework.boot.test.mock.mockito.MockBean
    private com.rmos.security.JwtUtils jwtUtils;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ScenarioSimulationService simulationService;

    @MockBean
    private ScenarioComparisonService comparisonService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void simulateScenario_ValidRequest_Success() throws Exception {
        ScenarioRequest req = new ScenarioRequest();
        req.setScenarioId("SCEN_01");
        req.setScenarioName("Scen 1");
        req.setObjectiveProfile(new ScenarioObjectiveProfile());

        PlanningRequest pr = new PlanningRequest();
        pr.setPlanningId("PLAN_1");
        pr.setPlanningWindowStart(java.time.LocalDateTime.now());
        pr.setPlanningWindowEnd(java.time.LocalDateTime.now().plusHours(10));

        com.rmos.dto.planning.PlanningTask tk = new com.rmos.dto.planning.PlanningTask();
        tk.setTaskId(1L);
        tk.setAssetId(2L);
        tk.setDepartment("OHE");
        tk.setDurationMinutes(60);
        tk.setPriorityScore(0.5);
        pr.setTasks(java.util.Collections.singletonList(tk));

        req.setPlanningRequest(pr);

        ScenarioResult res = new ScenarioResult();
        res.setScenarioId("SCEN_01");
        res.setStatus(ScenarioStatus.COMPLETED);

        when(simulationService.simulate(any(ScenarioRequest.class))).thenReturn(res);

        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

        mockMvc.perform(post("/api/scenarios/simulate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scenarioId").value("SCEN_01"));
    }

    @Test
    void compareScenarios_DuplicateIds_Rejects() throws Exception {
        ScenarioComparisonRequest req = new ScenarioComparisonRequest();
        ScenarioResult r1 = new ScenarioResult();
        r1.setScenarioId("S1");
        ScenarioResult r2 = new ScenarioResult();
        r2.setScenarioId("S1"); // Duplicate
        req.setScenarios(java.util.Arrays.asList(r1, r2));

        mockMvc.perform(post("/api/scenarios/compare")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void compareScenarios_ValidRequest_Success() throws Exception {
        ScenarioComparisonRequest req = new ScenarioComparisonRequest();
        ScenarioResult r1 = new ScenarioResult();
        r1.setScenarioId("S1");
        ScenarioResult r2 = new ScenarioResult();
        r2.setScenarioId("S2");
        req.setScenarios(java.util.Arrays.asList(r1, r2));

        ScenarioComparisonResult cres = new ScenarioComparisonResult();
        cres.setBestScenarioId("S2");

        when(comparisonService.compare(any(ScenarioComparisonRequest.class))).thenReturn(cres);

        mockMvc.perform(post("/api/scenarios/compare")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bestScenarioId").value("S2"));
    }
}





