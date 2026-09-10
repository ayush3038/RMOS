package com.rmos.service.planning;

import com.rmos.dto.planning.PlanningRequest;
import com.rmos.dto.planning.PlanningResult;

public interface PlanningOptimizer {
    PlanningResult optimize(PlanningRequest request);
}
