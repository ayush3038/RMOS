package com.rmos.service.planning;

import com.rmos.dto.planning.PlanningRequest;
import com.rmos.dto.planning.PlanningResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlanningServiceTest {

    @Mock
    private PlanningOptimizer optimizer;

    @InjectMocks
    private PlanningService service;

    @Test
    void plan_DelegatesToOptimizer() {
        PlanningRequest req = new PlanningRequest();
        req.setPlanningWindowStart(LocalDateTime.now());
        req.setPlanningWindowEnd(LocalDateTime.now().plusHours(1));

        when(optimizer.optimize(any(PlanningRequest.class))).thenReturn(new PlanningResult());

        PlanningResult result = service.plan(req);

        assertNotNull(result);
        verify(optimizer).optimize(req);
    }

    @Test
    void plan_InvalidWindow_ThrowsException() {
        PlanningRequest req = new PlanningRequest();
        // Start is after end
        req.setPlanningWindowStart(LocalDateTime.now().plusHours(2));
        req.setPlanningWindowEnd(LocalDateTime.now());

        assertThrows(IllegalArgumentException.class, () -> service.plan(req));
    }
}
