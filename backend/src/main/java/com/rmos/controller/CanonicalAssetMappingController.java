package com.rmos.controller;

import com.rmos.dto.CanonicalAssetMappingResponse;
import com.rmos.service.CanonicalAssetMappingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/integration/asset-mappings")
public class CanonicalAssetMappingController {

    private final CanonicalAssetMappingService canonicalAssetMappingService;

    public CanonicalAssetMappingController(CanonicalAssetMappingService canonicalAssetMappingService) {
        this.canonicalAssetMappingService = canonicalAssetMappingService;
    }

    @GetMapping
    public ResponseEntity<List<CanonicalAssetMappingResponse>> getAllMappings() {
        return ResponseEntity.ok(canonicalAssetMappingService.getAllMappings());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CanonicalAssetMappingResponse> getMappingById(@PathVariable Long id) {
        return ResponseEntity.ok(canonicalAssetMappingService.getMappingById(id));
    }
}
