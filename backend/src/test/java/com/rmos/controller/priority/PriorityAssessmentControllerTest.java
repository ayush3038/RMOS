package com.rmos.controller.priority;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rmos.domain.priority.ImpactLevel;
import com.rmos.domain.priority.PriorityBand;
import com.rmos.domain.priority.SafetyLevel;
import com.rmos.domain.priority.WorkforceLevel;
import com.rmos.dto.priority.PriorityAssessment;
import com.rmos.dto.priority.PriorityAssessmentRequest;
import com.rmos.dto.priority.PriorityFactorContribution;
import com.rmos.service.priority.PriorityAssessmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@org.springframework.context.annotation.Import({com.rmos.security.SecurityConfig.class, com.rmos.security.JwtAuthFilter.class, com.rmos.security.CustomAuthenticationEntryPoint.class, com.rmos.security.CustomAccessDeniedHandler.class})
@WebMvcTest(PriorityAssessmentController.class)
@org.springframework.security.test.context.support.WithMockUser(roles = "ADMIN")
class PriorityAssessmentControllerTest {
    @org.springframework.boot.test.mock.mockito.MockBean
    private com.rmos.security.JwtUtils jwtUtils;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PriorityAssessmentService assessmentService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void assessPriority_ValidRequest_ReturnsAssessment() throws Exception {
        PriorityAssessmentRequest request = new PriorityAssessmentRequest();
        request.setTaskId(10L);
        request.setSafetyLevel(SafetyLevel.HIGH);
        request.setTrainImpactLevel(ImpactLevel.MEDIUM);
        request.setCriticality(ImpactLevel.LOW);
        request.setUrgency(ImpactLevel.LOW);
        request.setDurationMinutes(60);
        request.setWorkforceRequired(WorkforceLevel.MEDIUM);

        PriorityAssessment mockResponse = new PriorityAssessment();
        mockResponse.setTaskId(10L);
        mockResponse.setPriorityScore(0.85);
        mockResponse.setPriorityBand(PriorityBand.CRITICAL);
        mockResponse.setFactorBreakdown(Collections.singletonList(new PriorityFactorContribution()));

        when(assessmentService.assessPriority(any(PriorityAssessmentRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/api/priority/assess")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskId").value(10L))
                .andExpect(jsonPath("$.priorityScore").value(0.85))
                .andExpect(jsonPath("$.priorityBand").value("CRITICAL"))
                .andExpect(jsonPath("$.factorBreakdown").isArray());
    }

    @Test
    void assessPriority_MissingDuration_ReturnsBadRequest() throws Exception {
        // Missing duration (null)
        PriorityAssessmentRequest request = new PriorityAssessmentRequest();
        request.setTaskId(10L);
        request.setSafetyLevel(SafetyLevel.HIGH);
        request.setTrainImpactLevel(ImpactLevel.MEDIUM);
        request.setCriticality(ImpactLevel.LOW);
        request.setUrgency(ImpactLevel.LOW);
        request.setWorkforceRequired(WorkforceLevel.MEDIUM);

        mockMvc.perform(post("/api/priority/assess")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}





