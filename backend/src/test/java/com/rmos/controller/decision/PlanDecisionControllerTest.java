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

@org.springframework.context.annotation.Import({ com.rmos.security.SecurityConfig.class,
        com.rmos.security.JwtAuthFilter.class, com.rmos.security.CustomAuthenticationEntryPoint.class,
        com.rmos.security.CustomAccessDeniedHandler.class })
@WebMvcTest(PlanDecisionController.class)
class PlanDecisionControllerTest {
    @org.springframework.boot.test.mock.mockito.MockBean
    private com.rmos.security.JwtUtils jwtUtils;

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

    private PlanReviewRequest buildValidRequest() {
        PlanReviewRequest req = new PlanReviewRequest();
        req.setReviewerId("USER_123");
        req.setReviewerRole(ReviewerRole.OPERATOR);
        req.setAction(PlanDecision.APPROVE);
        req.setPlanningId("PLAN_999");
        req.setPlanReference(new PlanReference("PLAN_999", 1));
        return req;
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(roles = "ADMIN")
    void createDecision_Admin_ReturnsCreated() throws Exception {
        PlanDecisionResult expected = new PlanDecisionResult();
        expected.setDecisionId("UUID-1234");
        expected.setPlanningId("PLAN_999");
        when(service.processDecision(any())).thenReturn(expected);

        mockMvc.perform(post("/api/plans/decisions")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(buildValidRequest())))
                .andExpect(status().isCreated());
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(roles = "REVIEWER")
    void createDecision_Reviewer_ReturnsCreated() throws Exception {
        PlanDecisionResult expected = new PlanDecisionResult();
        expected.setDecisionId("UUID-1234");
        expected.setPlanningId("PLAN_999");
        when(service.processDecision(any())).thenReturn(expected);

        mockMvc.perform(post("/api/plans/decisions")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(buildValidRequest())))
                .andExpect(status().isCreated());
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(roles = "PLANNER")
    void createDecision_Planner_ReturnsForbidden() throws Exception {
        mockMvc.perform(post("/api/plans/decisions")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(buildValidRequest())))
                .andExpect(status().isForbidden());
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(roles = "VIEWER")
    void createDecision_Viewer_ReturnsForbidden() throws Exception {
        mockMvc.perform(post("/api/plans/decisions")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(buildValidRequest())))
                .andExpect(status().isForbidden());
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(roles = "ADMIN")
    void createDecision_FailsBadRules() throws Exception {
        PlanReviewRequest req = buildValidRequest();
        req.setAction(PlanDecision.REJECT);
        when(service.processDecision(any())).thenThrow(new IllegalArgumentException("Missing comment"));

        mockMvc.perform(post("/api/plans/decisions")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }
}
