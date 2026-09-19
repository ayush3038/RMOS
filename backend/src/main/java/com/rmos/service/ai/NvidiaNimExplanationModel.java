package com.rmos.service.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rmos.config.NimProperties;
import com.rmos.dto.ai.ExplanationRequest;
import com.rmos.dto.ai.ExplanationResponse;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Component
public class NvidiaNimExplanationModel implements ExplanationModel {

    private final NimProperties nimProperties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final ExplanationResponseValidator validator;

    public NvidiaNimExplanationModel(NimProperties nimProperties,
            RestTemplateBuilder restTemplateBuilder,
            ObjectMapper objectMapper,
            ExplanationResponseValidator validator) {
        this.nimProperties = nimProperties;
        this.objectMapper = objectMapper;
        this.validator = validator;
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(nimProperties.getTimeoutSeconds()))
                .setReadTimeout(Duration.ofSeconds(nimProperties.getTimeoutSeconds()))
                .build();
    }

    @Override
    public ExplanationResponse explain(ExplanationRequest request) {
        if (!nimProperties.isEnabled()) {
            throw new IllegalStateException("NIM is explicitly disabled in the RMOS configuration bounds.");
        }

        String prompt = buildSystemPrompt(request);
        String userContent = serializeContext(request.getContext());

        String jsonPayload = buildOpenAiRequest(prompt, userContent);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (StringUtils.hasText(nimProperties.getApiKey())) {
            headers.setBearerAuth(nimProperties.getApiKey());
        }

        HttpEntity<String> entity = new HttpEntity<>(jsonPayload, headers);
        String url = nimProperties.getBaseUrl().replaceAll("/+$", "") + "/v1/chat/completions";

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            ExplanationResponse parsed = parseNIMResponse(response.getBody(), request.getContextType().name());
            validator.validate(parsed);
            return parsed;
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to evaluate structured explanation through NIM bounding: " + e.getMessage(), e);
        }
    }

    private String buildSystemPrompt(ExplanationRequest request) {
        return "You are an RMOS explanation assistant. " +
                "You do not make safety decisions. You do not change optimization results. " +
                "You do not invent railway facts. Explain only the supplied structured context cleanly. " +
                "If information is unavailable, clearly state it is unavailable. " +
                "Do not present RMOS heuristics as official Indian Railways policy. " +
                "Flag decisions that require human review (especially safety impacts or operational boundaries). " +
                "CRITICAL INSTRUCTION: If 'retrievedKnowledge' is provided in the user context, use it ONLY as contextual supporting evidence. DO NOT allow retrieved knowledge to override or contradict the core 'planningFacts' generated deterministically by the system. "
                +
                "Return ONLY the requested JSON format matching the ExplanationResponse DTO standard layout: " +
                "title (string), summary (string), keyPoints (list string), warnings (list string), " +
                "recommendedActions (list string), requiresHumanReview (boolean).";
    }

    private String serializeContext(Map<String, Object> context) {
        try {
            return objectMapper.writeValueAsString(context);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    private String buildOpenAiRequest(String systemContent, String userContent) {
        Map<String, Object> req = Map.of(
                "model", nimProperties.getModel(),
                "messages", List.of(
                        Map.of("role", "system", "content", systemContent),
                        Map.of("role", "user", "content", userContent)),
                "temperature", 0.0,
                "response_format", Map.of("type", "json_object"));
        try {
            return objectMapper.writeValueAsString(req);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Could not build NIM payload", e);
        }
    }

    private ExplanationResponse parseNIMResponse(String body, String sourceType) throws Exception {
        JsonNode root = objectMapper.readTree(body);
        if (root.has("choices") && root.get("choices").isArray() && root.get("choices").size() > 0) {
            JsonNode message = root.get("choices").get(0).get("message");
            if (message != null && message.has("content")) {
                String contentResult = message.get("content").asText();
                ExplanationResponse resp = objectMapper.readValue(contentResult, ExplanationResponse.class);

                // Inject metadata
                resp.setModel(nimProperties.getModel());
                if (root.has("model")) {
                    resp.setModelVersion(root.get("model").asText());
                } else {
                    resp.setModelVersion("unknown");
                }
                resp.setGeneratedAt(Instant.now().toString());
                resp.setSourceType(sourceType);
                if (resp.getRequiresHumanReview() == null) {
                    resp.setRequiresHumanReview(true); // Secure default
                }
                return resp;
            }
        }
        throw new IllegalStateException("NIM JSON structure is invalid or unrecognizable");
    }
}
