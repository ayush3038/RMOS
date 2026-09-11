package com.rmos.controller.risk;

import com.rmos.dto.risk.AssetRiskInput;
import com.rmos.dto.risk.AssetRiskPrediction;
import com.rmos.service.risk.AssetRiskAssessmentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/asset-risk")
public class AssetRiskController {

    private final AssetRiskAssessmentService assessmentService;

    public AssetRiskController(AssetRiskAssessmentService assessmentService) {
        this.assessmentService = assessmentService;
    }

    @PostMapping("/assess")
    public ResponseEntity<AssetRiskPrediction> assessRisk(@Valid @RequestBody AssetRiskInput input) {
        AssetRiskPrediction prediction = assessmentService.assessRisk(input);
        return ResponseEntity.ok(prediction);
    }
}
