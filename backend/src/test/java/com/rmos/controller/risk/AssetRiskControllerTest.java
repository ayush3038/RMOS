package com.rmos.controller.risk;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rmos.dto.risk.AssetRiskInput;
import com.rmos.dto.risk.AssetRiskPrediction;
import com.rmos.service.risk.AssetRiskAssessmentService;
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

@WebMvcTest(AssetRiskController.class)
class AssetRiskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AssetRiskAssessmentService service;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void assessRisk_ValidRequest_ReturnsOk() throws Exception {
        AssetRiskInput in = new AssetRiskInput();
        in.setAssetId("A1");
        in.setAssetType("OHE");
        in.setMaintenanceEventCount(0);
        in.setOverdueTaskCount(0);
        in.setFailureCount(0);
        in.setRecentFailureCount(0);
        in.setRecentMaintenanceCount(0);
        in.setDaysSinceLastMaintenance(0);
        in.setCriticalMaintenanceCount(0);

        AssetRiskPrediction pred = new AssetRiskPrediction();
        pred.setAssetId("A1");
        pred.setRiskScore(0.2);
        pred.setHealthScore(0.8);

        when(service.assessRisk(any(AssetRiskInput.class))).thenReturn(pred);

        mockMvc.perform(post("/api/asset-risk/assess")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(in)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assetId").value("A1"))
                .andExpect(jsonPath("$.riskScore").value(0.2));
    }

    @Test
    void assessRisk_InvalidRequest_ReturnsBadRequest() throws Exception {
        AssetRiskInput in = new AssetRiskInput();
        in.setAssetId(""); // Blank triggers @NotBlank

        mockMvc.perform(post("/api/asset-risk/assess")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(in)))
                .andExpect(status().isBadRequest());
    }
}
