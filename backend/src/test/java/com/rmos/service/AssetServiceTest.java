package com.rmos.service;

import com.rmos.domain.Asset;
import com.rmos.domain.Department;
import com.rmos.dto.AssetResponse;
import com.rmos.exception.ResourceNotFoundException;
import com.rmos.repository.AssetRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssetServiceTest {

    @Mock
    private AssetRepository assetRepository;

    @InjectMocks
    private AssetService assetService;

    @Test
    void testGetAllAssets() {
        Asset asset = new Asset("ASSET-001", "Signal", "SEC-01", Department.S_AND_T, true);
        asset.setId(1L);

        when(assetRepository.findAll()).thenReturn(List.of(asset));

        List<AssetResponse> responses = assetService.getAllAssets();

        assertEquals(1, responses.size());
        assertEquals("ASSET-001", responses.get(0).getAssetId());
        assertEquals("S_AND_T", responses.get(0).getDepartment());
        verify(assetRepository, times(1)).findAll();
    }

    @Test
    void testGetAssetByIdFound() {
        Asset asset = new Asset("ASSET-001", "Signal", "SEC-01", Department.S_AND_T, true);
        asset.setId(1L);

        when(assetRepository.findById(1L)).thenReturn(Optional.of(asset));

        AssetResponse response = assetService.getAssetById(1L);

        assertNotNull(response);
        assertEquals("ASSET-001", response.getAssetId());
        verify(assetRepository, times(1)).findById(1L);
    }

    @Test
    void testGetAssetByIdNotFound() {
        when(assetRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> assetService.getAssetById(99L));
        verify(assetRepository, times(1)).findById(99L);
    }
}
