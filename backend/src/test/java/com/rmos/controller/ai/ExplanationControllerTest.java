package com.rmos.controller.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rmos.domain.ai.ExplanationAudience;
import com.rmos.domain.ai.ExplanationContextType;
import com.rmos.dto.ai.ExplanationRequest;
import com.rmos.dto.ai.ExplanationResponse;
import com.rmos.service.ai.ExplanationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@org.springframework.context.annotation.Import({com.rmos.security.SecurityConfig.class, com.rmos.security.JwtAuthFilter.class, com.rmos.security.CustomAuthenticationEntryPoint.class, com.rmos.security.CustomAccessDeniedHandler.class})
@WebMvcTest(ExplanationController.class)
@org.springframework.security.test.context.support.WithMockUser(roles = "ADMIN")
class ExplanationControllerTest {
    @org.springframework.boot.test.mock.mockito.MockBean
    private com.rmos.security.JwtUtils jwtUtils;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExplanationService service;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void explain_ValidRequest_Success() throws Exception {
        ExplanationRequest req = new ExplanationRequest();
        req.setContextType(ExplanationContextType.SAFETY_VALIDATION);
        req.setAudience(ExplanationAudience.ANALYST);
        req.setContext(Map.of("rule", "FOO"));

        ExplanationResponse res = new ExplanationResponse();
        res.setTitle("ABC");

        when(service.explain(any())).thenReturn(res);

        mockMvc.perform(post("/api/explanations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("ABC"));
    }

    @Test
    void explain_InvalidRequest_BadRequest() throws Exception {
        ExplanationRequest req = new ExplanationRequest(); // Null fields

        mockMvc.perform(post("/api/explanations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void explain_Disabled_ServiceUnavailable() throws Exception {
        ExplanationRequest req = new ExplanationRequest();
        req.setContextType(ExplanationContextType.SAFETY_VALIDATION);
        req.setAudience(ExplanationAudience.ANALYST);
        req.setContext(Map.of("rule", "FOO"));

        when(service.explain(any())).thenThrow(new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE, "Disabled"));

        mockMvc.perform(post("/api/explanations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isServiceUnavailable());
    }
}





