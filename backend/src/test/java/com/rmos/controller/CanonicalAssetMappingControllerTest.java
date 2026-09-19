package com.rmos.controller;

import com.rmos.domain.DataSourceType;
import com.rmos.dto.CanonicalAssetMappingResponse;
import com.rmos.service.CanonicalAssetMappingService;
import com.rmos.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@org.springframework.context.annotation.Import({com.rmos.security.SecurityConfig.class, com.rmos.security.JwtAuthFilter.class, com.rmos.security.CustomAuthenticationEntryPoint.class, com.rmos.security.CustomAccessDeniedHandler.class})
@WebMvcTest(CanonicalAssetMappingController.class)
@org.springframework.security.test.context.support.WithMockUser(roles = "ADMIN")
class CanonicalAssetMappingControllerTest {
    @org.springframework.boot.test.mock.mockito.MockBean
    private com.rmos.security.JwtUtils jwtUtils;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CanonicalAssetMappingService canonicalAssetMappingService;

    @Test
    void getAllMappings_Returns200() throws Exception {
        CanonicalAssetMappingResponse response = new CanonicalAssetMappingResponse();
        response.setId(1L);
        response.setSourceAssetId("SA-1");
        response.setCanonicalAssetId("CA-1");
        response.setSourceType(DataSourceType.BDMS);

        when(canonicalAssetMappingService.getAllMappings()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/integration/asset-mappings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sourceAssetId").value("SA-1"))
                .andExpect(jsonPath("$[0].canonicalAssetId").value("CA-1"))
                .andExpect(jsonPath("$[0].sourceType").value("BDMS"));
    }

    @Test
    void getMappingById_ExistingId_Returns200() throws Exception {
        CanonicalAssetMappingResponse response = new CanonicalAssetMappingResponse();
        response.setId(1L);
        response.setCanonicalAssetId("CA-1");

        when(canonicalAssetMappingService.getMappingById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/integration/asset-mappings/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.canonicalAssetId").value("CA-1"));
    }

    @Test
    void getMappingById_NonExistingId_Returns404() throws Exception {
        when(canonicalAssetMappingService.getMappingById(99L)).thenThrow(new ResourceNotFoundException("Not found"));

        mockMvc.perform(get("/api/integration/asset-mappings/99"))
                .andExpect(status().isNotFound());
    }
}





