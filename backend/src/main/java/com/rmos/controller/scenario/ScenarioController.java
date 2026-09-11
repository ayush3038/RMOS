package com.rmos.controller.scenario;

import com.rmos.dto.scenario.ScenarioComparisonRequest;
import com.rmos.dto.scenario.ScenarioComparisonResult;
import com.rmos.dto.scenario.ScenarioRequest;
import com.rmos.dto.scenario.ScenarioResult;
import com.rmos.service.scenario.ScenarioComparisonService;
import com.rmos.service.scenario.ScenarioSimulationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.Set;

@RestController
@RequestMapping("/api/scenarios")
public class ScenarioController {

    private final ScenarioSimulationService simulationService;
    private final ScenarioComparisonService comparisonService;

    public ScenarioController(ScenarioSimulationService simulationService,
            ScenarioComparisonService comparisonService) {
        this.simulationService = simulationService;
        this.comparisonService = comparisonService;
    }

    @PostMapping("/simulate")
    public ResponseEntity<ScenarioResult> simulateScenario(@Valid @RequestBody ScenarioRequest request) {
        ScenarioResult result = simulationService.simulate(request);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/compare")
    public ResponseEntity<ScenarioComparisonResult> compareScenarios(
            @Valid @RequestBody ScenarioComparisonRequest request) {
        // Validate duplicates
        if (request.getScenarios() != null) {
            Set<String> ids = new HashSet<>();
            for (ScenarioResult res : request.getScenarios()) {
                if (res.getScenarioId() != null && !ids.add(res.getScenarioId())) {
                    return ResponseEntity.badRequest().body(null);
                }
            }
        }

        try {
            ScenarioComparisonResult result = comparisonService.compare(request);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
