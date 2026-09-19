package com.rmos.controller.safety;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rmos.domain.safety.RuleEvaluationStatus;
import com.rmos.domain.safety.RuleSeverity;
import com.rmos.domain.safety.SafetyRuleType;
import com.rmos.dto.safety.RuleEvaluation;
import com.rmos.dto.safety.SafetyValidationRequest;
import com.rmos.dto.safety.SafetyValidationResult;
import com.rmos.service.safety.SafetyValidationService;
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

@org.springframework.context.annotation.Import({com.rmos.security.SecurityConfig.class, com.rmos.security.JwtAuthFilter.class, com.rmos.security.CustomAuthenticationEntryPoint.class, com.rmos.security.CustomAccessDeniedHandler.class})
@WebMvcTest(SafetyValidationController.class)
@org.springframework.security.test.context.support.WithMockUser(roles = "ADMIN")
class SafetyValidationControllerTest {
    @org.springframework.boot.test.mock.mockito.MockBean
    private com.rmos.security.JwtUtils jwtUtils;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SafetyValidationService safetyValidationService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void validate_ValidRequest_Returns200AndValidTrue() throws Exception {
        SafetyValidationRequest request = new SafetyValidationRequest();
        request.setAssetId(1L);
        request.setDepartment("OHE");
        request.setRequestedDurationMinutes(60);

        SafetyValidationResult result = new SafetyValidationResult();
        result.setValid(true);
        result.addEvaluation(new RuleEvaluation(SafetyRuleType.REQUIRED_ASSET, RuleSeverity.HARD,
                RuleEvaluationStatus.PASSED, "Passed"));

        when(safetyValidationService.validate(any(SafetyValidationRequest.class))).thenReturn(result);

        mockMvc.perform(post("/api/safety/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.evaluations[0].ruleType").value("REQUIRED_ASSET"));
    }

    @Test
    void validate_HardRuleFailure_Returns200AndValidFalse() throws Exception {
        SafetyValidationRequest request = new SafetyValidationRequest();
        request.setAssetId(null);

        SafetyValidationResult result = new SafetyValidationResult();
        result.setValid(false);
        result.addEvaluation(new RuleEvaluation(SafetyRuleType.REQUIRED_ASSET, RuleSeverity.HARD,
                RuleEvaluationStatus.FAILED, "Asset missing"));

        when(safetyValidationService.validate(any(SafetyValidationRequest.class))).thenReturn(result);

        mockMvc.perform(post("/api/safety/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(false))
                .andExpect(jsonPath("$.evaluations[0].status").value("FAILED"));
    }

    @Test
    void validate_MalformedRequest_Returns400() throws Exception {
        // sending invalid JSON
        mockMvc.perform(post("/api/safety/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ invalid }"))
                .andExpect(status().isBadRequest());
    }
}





