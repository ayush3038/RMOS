package com.rmos.controller.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rmos.service.integration.IncidentMappingService;
import com.rmos.security.JwtUtils;
import com.rmos.security.JwtAuthFilter;
import com.rmos.dto.integration.IncidentContext;
import com.rmos.service.integration.IncidentMappingService;
import com.rmos.security.JwtUtils;
import com.rmos.security.JwtAuthFilter;
import com.rmos.security.CustomAuthenticationEntryPoint;
import com.rmos.security.CustomAccessDeniedHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = IntegrationController.class)
@AutoConfigureMockMvc(addFilters = false)
class IntegrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IncidentMappingService mappingService;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    @MockBean
    private CustomAuthenticationEntryPoint authenticationEntryPoint;

    @MockBean
    private CustomAccessDeniedHandler accessDeniedHandler;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "ADMIN")
    void testOptimisticLockingConflictReturns409() throws Exception {
        IncidentContext req = new IncidentContext();
        req.setSourceSystem("TMS");

        when(mappingService.mapIncidentToPlanningContext(any()))
                .thenThrow(new ObjectOptimisticLockingFailureException("Mock Entity", 1L));

        mockMvc.perform(post("/api/v1/integration/events")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PLAN_VERSION_CONFLICT"));
    }
}
