package com.rmos.service.planning;

import com.rmos.dto.planning.PlanningRequest;
import com.rmos.dto.planning.PlanningResult;
import org.springframework.stereotype.Service;

@Service
public class PlanningService {

    private final PlanningOptimizer optimizer;

    public PlanningService(PlanningOptimizer optimizer) {
        this.optimizer = optimizer;
    }

    public PlanningResult plan(PlanningRequest request) {
        // Request validation is primarily handled via @Valid at the controller layer.
        // Additional business validation boundaries can act here.
        if (request.getPlanningWindowStart().isAfter(request.getPlanningWindowEnd())) {
            throw new IllegalArgumentException("Planning window start must be before end");
        }

        return optimizer.optimize(request);
    }
}
