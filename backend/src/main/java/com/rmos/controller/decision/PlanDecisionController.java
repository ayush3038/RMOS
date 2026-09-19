package com.rmos.controller.decision;

import com.rmos.domain.decision.PlanDecisionAudit;
import com.rmos.dto.decision.PlanDecisionResult;
import com.rmos.dto.decision.PlanReviewRequest;
import com.rmos.service.decision.PlanDecisionAuditService;
import com.rmos.service.decision.PlanDecisionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/plans/decisions")
public class PlanDecisionController {

    private final PlanDecisionService planDecisionService;
    private final PlanDecisionAuditService planDecisionAuditService;

    public PlanDecisionController(PlanDecisionService planDecisionService,
            PlanDecisionAuditService planDecisionAuditService) {
        this.planDecisionService = planDecisionService;
        this.planDecisionAuditService = planDecisionAuditService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('REVIEWER', 'ADMIN')")
    public ResponseEntity<PlanDecisionResult> createDecision(@Valid @RequestBody PlanReviewRequest request) {
        try {
            PlanDecisionResult result = planDecisionService.processDecision(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/{decisionId}")
    public ResponseEntity<PlanDecisionAudit> getDecision(@PathVariable String decisionId) {
        return planDecisionAuditService.getDecision(decisionId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
