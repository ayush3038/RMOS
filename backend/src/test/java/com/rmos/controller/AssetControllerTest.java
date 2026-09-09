package com.rmos.controller;

import com.rmos.dto.AssetResponse;
import com.rmos.exception.ResourceNotFoundException;
import com.rmos.service.AssetService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AssetController.class)
class AssetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AssetService assetService;

    @Test
    void testGetAllAssets() throws Exception {
        AssetResponse asset1 = new AssetResponse(1L, "A-123", "Locomotive", "SEC-1", "OPERATIONS", true);
        AssetResponse asset2 = new AssetResponse(2L, "A-124", "Wagon", "SEC-2", "MAINTENANCE", false);
        List<AssetResponse> assets = Arrays.asList(asset1, asset2);

        when(assetService.getAllAssets()).thenReturn(assets);

        mockMvc.perform(get("/api/assets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].assetId").value("A-123"))
                .andExpect(jsonPath("$[0].assetType").value("Locomotive"))
                .andExpect(jsonPath("$[0].sectionId").value("SEC-1"))
                .andExpect(jsonPath("$[0].department").value("OPERATIONS"))
                .andExpect(jsonPath("$[0].active").value(true))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].assetId").value("A-124"));
    }

    @Test
    void testGetAssetById() throws Exception {
        AssetResponse asset = new AssetResponse(1L, "A-123", "Locomotive", "SEC-1", "OPERATIONS", true);

        when(assetService.getAssetById(1L)).thenReturn(asset);

        mockMvc.perform(get("/api/assets/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.assetId").value("A-123"))
                .andExpect(jsonPath("$.assetType").value("Locomotive"));
    }

    @Test
    void testGetAssetById_NotFound() throws Exception {
        when(assetService.getAssetById(anyLong())).thenThrow(new ResourceNotFoundException("Asset not found"));

        mockMvc.perform(get("/api/assets/999"))
                .andExpect(status().isNotFound());
    }
}
