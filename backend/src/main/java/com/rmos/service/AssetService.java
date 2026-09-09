package com.rmos.service;

import com.rmos.dto.AssetResponse;
import com.rmos.exception.ResourceNotFoundException;
import com.rmos.repository.AssetRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AssetService {

    private final AssetRepository assetRepository;

    public AssetService(AssetRepository assetRepository) {
        this.assetRepository = assetRepository;
    }

    public List<AssetResponse> getAllAssets() {
        return assetRepository.findAll().stream()
                .map(AssetResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public AssetResponse getAssetById(Long id) {
        return assetRepository.findById(id)
                .map(AssetResponse::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found with id: " + id));
    }
}
