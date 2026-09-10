package com.rmos.controller.safety;

import com.rmos.dto.safety.SafetyValidationRequest;
import com.rmos.dto.safety.SafetyValidationResult;
import com.rmos.service.safety.SafetyValidationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/safety")
public class SafetyValidationController {

    private final SafetyValidationService validationService;

    public SafetyValidationController(SafetyValidationService validationService) {
        this.validationService = validationService;
    }

    @PostMapping("/validate")
    public ResponseEntity<SafetyValidationResult> validate(@Valid @RequestBody SafetyValidationRequest request) {
        SafetyValidationResult result = validationService.validate(request);
        return ResponseEntity.ok(result);
    }
}
