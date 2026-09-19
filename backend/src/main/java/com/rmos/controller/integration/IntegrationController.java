package com.rmos.controller.integration;

import com.rmos.dto.integration.IncidentContext;
import com.rmos.dto.integration.IncidentMappingResult;
import com.rmos.service.integration.IncidentMappingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/integration")
public class IntegrationController {

    private final IncidentMappingService mappingService;

    @Autowired
    public IntegrationController(IncidentMappingService mappingService) {
        this.mappingService = mappingService;
    }

    @PostMapping("/events")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PLANNER')") // Protected Endpoint boundary check
    public ResponseEntity<IncidentMappingResult> consumeExternalEvent(
            @RequestHeader(value = "X-Correlation-ID", required = false) String correlationId,
            @RequestBody IncidentContext incident) {

        IncidentMappingResult result = mappingService.mapIncidentToPlanningContext(incident);
        return ResponseEntity.ok(result);
    }
}
