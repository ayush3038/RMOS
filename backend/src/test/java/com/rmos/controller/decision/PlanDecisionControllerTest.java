package com.rmos.controller.decision;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rmos.domain.decision.PlanDecision;
import com.rmos.domain.decision.ReviewerRole;
import com.rmos.dto.decision.PlanReference;
import com.rmos.dto.decision.PlanReviewRequest;
import com.rmos.dto.decision.PlanDecisionResult;
import com.rmos.service.decision.PlanDecisionAuditService;
import com.rmos.service.decision.PlanDecisionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PlanDecisionController.class)
class PlanDecisionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PlanDecisionService service;

    @MockBean
    private PlanDecisionAuditService auditService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void createDecision_ValidApprove_ReturnsCreated() throws Exception {
        PlanReviewRequest req = new PlanReviewRequest();
        req.setReviewerId("USER_123");
        req.setReviewerRole(ReviewerRole.OPERATOR);
        req.setAction(PlanDecision.APPROVE);
        req.setPlanningId("PLAN_999");
        req.setPlanReference(new PlanReference("PLAN_999", 1));

        PlanDecisionResult expected = new PlanDecisionResult();
        expected.setDecisionId("UUID-1234");
        expected.setPlanningId("PLAN_999");

        when(service.processDecision(any())).thenReturn(expected);

        mockMvc.perform(post("/api/plans/decisions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.decisionId").value("UUID-1234"))
                .andExpect(jsonPath("$.planningId").value("PLAN_999"));
    }

    @Test
    void createDecision_FailsBadRules() throws Exception {
        PlanReviewRequest req = new PlanReviewRequest();
        req.setReviewerId("USER_123");
        req.setReviewerRole(ReviewerRole.OPERATOR);
        req.setAction(PlanDecision.REJECT);
        req.setPlanningId("PLAN_999");
        req.setPlanReference(new PlanReference("PLAN_999", 1));

        when(service.processDecision(any())).thenThrow(new IllegalArgumentException("Missing comment"));

        mockMvc.perform(post("/api/plans/decisions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }
}
