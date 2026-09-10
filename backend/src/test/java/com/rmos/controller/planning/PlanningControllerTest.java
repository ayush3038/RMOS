package com.rmos.controller.planning;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rmos.domain.planning.PlanningDecisionStatus;
import com.rmos.dto.planning.PlanningRequest;
import com.rmos.dto.planning.PlanningResult;
import com.rmos.dto.planning.PlanningTask;
import com.rmos.service.planning.PlanningService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PlanningController.class)
class PlanningControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PlanningService planningService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // required to serialize LocalDateTime
    }

    private PlanningRequest createValidRequest() {
        PlanningRequest request = new PlanningRequest();
        request.setPlanningId("PLAN-999");
        request.setPlanningWindowStart(LocalDateTime.now().plusHours(1));
        request.setPlanningWindowEnd(LocalDateTime.now().plusHours(10));

        PlanningTask tk = new PlanningTask();
        tk.setTaskId(1L);
        tk.setAssetId(1L);
        tk.setDepartment("OHE");
        tk.setDurationMinutes(60);
        tk.setPriorityScore(0.5);

        request.setTasks(Collections.singletonList(tk));
        return request;
    }

    @Test
    void plan_ValidRequest_ReturnsResult() throws Exception {
        PlanningRequest request = createValidRequest();

        PlanningResult result = new PlanningResult();
        result.setPlanningId("PLAN-999");
        result.setDecisionStatus(PlanningDecisionStatus.FEASIBLE);
        result.setAssignments(Collections.emptyList());

        when(planningService.plan(any(PlanningRequest.class))).thenReturn(result);

        mockMvc.perform(post("/api/planning/plan")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.planningId").value("PLAN-999"))
                .andExpect(jsonPath("$.decisionStatus").value("FEASIBLE"));
    }

    @Test
    void plan_EmptyTaskCollection_ReturnsBadRequest() throws Exception {
        PlanningRequest request = createValidRequest();
        request.setTasks(Collections.emptyList());

        mockMvc.perform(post("/api/planning/plan")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
