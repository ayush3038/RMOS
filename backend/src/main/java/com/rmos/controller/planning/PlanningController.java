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
import org.springframework.web.bind.annotation.PathVariable;
import com.rmos.service.replanning.DynamicReplanningService;
import com.rmos.domain.event.RmosRealtimeEvent;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/planning")
public class PlanningController {

    private final PlanningService planningService;
    private final DynamicReplanningService dynamicReplanningService;

    public PlanningController(PlanningService planningService, DynamicReplanningService dynamicReplanningService) {
        this.planningService = planningService;
        this.dynamicReplanningService = dynamicReplanningService;
    }

    @PostMapping("/plan")
    @PreAuthorize("hasAnyRole('PLANNER', 'ADMIN')")
    public ResponseEntity<PlanningResult> plan(@Valid @RequestBody PlanningRequest request) {
        PlanningResult result = planningService.plan(request);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/runs/{runId}/replan")
    @PreAuthorize("hasAnyRole('PLANNER', 'ADMIN')")
    public ResponseEntity<PlanningResult> replan(
            @PathVariable String runId,
            @Valid @RequestBody RmosRealtimeEvent event) {

        // M3.16: We need a synthetic base context constructed from the run ID.
        // For demonstration bounds in RMOS, we map the trigger through the service
        // natively.
        // We will construct an empty Request and let the mock base context fill in in
        // the service layer.
        PlanningRequest request = new PlanningRequest();

        PlanningResult result = dynamicReplanningService.handleRealtimeEvent(event, request);

        if (result == null) {
            // Signal NO_CONTENT if event was immaterial and generated no candidate
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(result);
    }
}
