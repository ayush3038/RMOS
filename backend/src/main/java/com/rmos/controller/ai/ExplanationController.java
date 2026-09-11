package com.rmos.controller.ai;

import com.rmos.dto.ai.ExplanationRequest;
import com.rmos.dto.ai.ExplanationResponse;
import com.rmos.service.ai.ExplanationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/explanations")
public class ExplanationController {

    private final ExplanationService explanationService;

    public ExplanationController(ExplanationService explanationService) {
        this.explanationService = explanationService;
    }

    @PostMapping
    public ResponseEntity<ExplanationResponse> getExplanation(@Valid @RequestBody ExplanationRequest request) {
        ExplanationResponse response = explanationService.explain(request);
        return ResponseEntity.ok(response);
    }
}
