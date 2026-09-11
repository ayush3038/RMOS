package com.rmos.service.ai;

import com.rmos.config.NimProperties;
import com.rmos.domain.ai.ExplanationAudience;
import com.rmos.domain.ai.ExplanationContextType;
import com.rmos.dto.ai.ExplanationRequest;
import com.rmos.dto.ai.ExplanationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.springframework.web.server.ResponseStatusException;

class ExplanationServiceTest {

    private ExplanationModel model;
    private NimProperties properties;
    private ExplanationService service;

    @BeforeEach
    void setUp() {
        model = mock(ExplanationModel.class);
        properties = new NimProperties();
        service = new ExplanationService(model, properties);
    }

    private ExplanationRequest req() {
        ExplanationRequest r = new ExplanationRequest();
        r.setContextType(ExplanationContextType.PLANNING_RESULT);
        r.setAudience(ExplanationAudience.OPERATOR);
        r.setContext(Map.of());
        return r;
    }

    @Test
    void testDisabledBehavior() {
        properties.setEnabled(false);
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.explain(req()));
        assertEquals(org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE, ex.getStatusCode());
        verify(model, never()).explain(any());
    }

    @Test
    void testEnabledSuccess() {
        properties.setEnabled(true);
        ExplanationResponse expected = new ExplanationResponse();
        expected.setTitle("Success");
        when(model.explain(any())).thenReturn(expected);

        ExplanationResponse res = service.explain(req());

        assertEquals("Success", res.getTitle());
        verify(model, times(1)).explain(any());
    }

    @Test
    void testModelExceptionHandledCleanly() {
        properties.setEnabled(true);
        when(model.explain(any())).thenThrow(new RuntimeException("Crash"));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.explain(req()));
        assertEquals(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, ex.getStatusCode());
    }
}
