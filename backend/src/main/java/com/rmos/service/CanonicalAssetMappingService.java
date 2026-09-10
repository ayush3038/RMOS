package com.rmos.service;

import com.rmos.domain.CanonicalAssetMapping;
import com.rmos.dto.CanonicalAssetMappingResponse;
import com.rmos.exception.ResourceNotFoundException;
import com.rmos.repository.CanonicalAssetMappingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CanonicalAssetMappingService {

    private final CanonicalAssetMappingRepository repository;

    public CanonicalAssetMappingService(CanonicalAssetMappingRepository repository) {
        this.repository = repository;
    }

    public List<CanonicalAssetMappingResponse> getAllMappings() {
        return repository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public CanonicalAssetMappingResponse getMappingById(Long id) {
        CanonicalAssetMapping mapping = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CanonicalAssetMapping not found with id: " + id));
        return mapToResponse(mapping);
    }

    private CanonicalAssetMappingResponse mapToResponse(CanonicalAssetMapping mapping) {
        CanonicalAssetMappingResponse response = new CanonicalAssetMappingResponse();
        response.setId(mapping.getId());
        response.setSourceType(mapping.getSourceType());
        response.setSourceAssetId(mapping.getSourceAssetId());
        response.setCanonicalAssetId(mapping.getCanonicalAssetId());
        response.setActive(mapping.isActive());
        response.setMappedAt(mapping.getMappedAt());
        return response;
    }
}
