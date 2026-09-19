package com.rmos.controller.planning;

import com.rmos.dto.planning.PlanningRequest;
import com.rmos.dto.planning.PlanningResult;
import com.rmos.service.planning.PlanningService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/planning")
public class PlanningController {

    private final PlanningService planningService;

    public PlanningController(PlanningService planningService) {
        this.planningService = planningService;
    }

    @PostMapping("/plan")
    @PreAuthorize("hasAnyRole('PLANNER', 'ADMIN')")
    public ResponseEntity<PlanningResult> plan(@Valid @RequestBody PlanningRequest request) {
        PlanningResult result = planningService.plan(request);
        return ResponseEntity.ok(result);
    }
}
