package com.rmos.service.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rmos.config.NimProperties;
import com.rmos.domain.ai.ExplanationAudience;
import com.rmos.domain.ai.ExplanationContextType;
import com.rmos.dto.ai.ExplanationRequest;
import com.rmos.dto.ai.ExplanationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NvidiaNimExplanationModelTest {

    private RestTemplate restTemplate;
    private NvidiaNimExplanationModel model;
    private NimProperties props;

    @BeforeEach
    void setUp() {
        props = new NimProperties();
        props.setEnabled(true);
        props.setModel("meta-llama3-70b-instruct");

        restTemplate = mock(RestTemplate.class);
        RestTemplateBuilder builder = mock(RestTemplateBuilder.class);
        when(builder.setConnectTimeout(any())).thenReturn(builder);
        when(builder.setReadTimeout(any())).thenReturn(builder);
        when(builder.build()).thenReturn(restTemplate);

        ExplanationResponseValidator validator = new ExplanationResponseValidator();
        ObjectMapper mapper = new ObjectMapper();

        model = new NvidiaNimExplanationModel(props, builder, mapper, validator);
    }

    private ExplanationRequest req() {
        ExplanationRequest r = new ExplanationRequest();
        r.setContextType(ExplanationContextType.SAFETY_VALIDATION);
        r.setAudience(ExplanationAudience.OPERATOR);
        r.setContext(Map.of("data", "test"));
        return r;
    }

    @Test
    void testValidJsonResponse() {
        String mockNimResponse = "{\"id\":\"chatcmpl-123\",\"model\":\"meta-llama3-70b-instruct\",\"choices\":[{\"index\":0,\"message\":{\"role\":\"assistant\",\"content\":\"{\\\"title\\\":\\\"Test\\\",\\\"summary\\\":\\\"Test sum\\\",\\\"sourceType\\\":\\\"SAFETY\\\",\\\"model\\\":\\\"meta-llama3-70b-instruct\\\",\\\"keyPoints\\\":[],\\\"warnings\\\":[],\\\"recommendedActions\\\":[],\\\"requiresHumanReview\\\":true}\"},\"finish_reason\":\"stop\"}]}";

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), ArgumentMatchers.<Class<String>>any()))
                .thenReturn(new ResponseEntity<>(mockNimResponse, HttpStatus.OK));

        ExplanationResponse res = model.explain(req());

        assertEquals("Test", res.getTitle());
        assertEquals("meta-llama3-70b-instruct", res.getModel());
    }

    @Test
    void testDisabledFails() {
        props.setEnabled(false);
        assertThrows(IllegalStateException.class, () -> model.explain(req()));
    }

    @Test
    void testMalformedJsonThrowsRuntime() {
        String badJson = "{ invalid.. }";

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), ArgumentMatchers.<Class<String>>any()))
                .thenReturn(new ResponseEntity<>(badJson, HttpStatus.OK));

        assertThrows(RuntimeException.class, () -> model.explain(req()));
    }
}
