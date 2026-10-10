package com.rmos.service.ai.chat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rmos.config.ChatProperties;
import com.rmos.dto.chat.ChatTurn;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Shared OpenAI-compatible chat completion transport used by both the NVIDIA
 * NIM provider and the local Ollama provider. Mirrors the hand-rolled
 * RestTemplate style of {@code NvidiaNimExplanationModel} (no external SDKs).
 */
public abstract class OpenAiCompatibleChatProvider implements ChatProvider {

    protected final ChatProperties properties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    protected OpenAiCompatibleChatProvider(ChatProperties properties,
            RestTemplateBuilder restTemplateBuilder,
            ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        Duration timeout = Duration.ofSeconds(Math.max(1, properties.getTimeoutSeconds()));
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(timeout)
                .setReadTimeout(timeout)
                .build();
    }

    /** Fallback base URL when no chat-specific override is supplied. */
    protected abstract String defaultBaseUrl();

    /** Fallback model name when no chat-specific override is supplied. */
    protected abstract String defaultModel();

    /** Whether an API key is required for the resolved base URL. */
    protected abstract boolean apiKeyRequired();

    /** Fallback API key source (NIM reuses rmos.ai.nim.api-key). */
    protected String fallbackApiKey() {
        return "";
    }

    @Override
    public String model() {
        return resolveModel();
    }

    protected String resolveBaseUrl() {
        String specific = specificBaseUrl();
        if (StringUtils.hasText(specific)) {
            return trimTrailingSlash(specific);
        }
        return trimTrailingSlash(defaultBaseUrl());
    }

    protected String resolveModel() {
        String specific = specificModel();
        if (StringUtils.hasText(specific)) {
            return specific;
        }
        if (StringUtils.hasText(properties.getModel())) {
            return properties.getModel();
        }
        return defaultModel();
    }

    protected String resolveApiKey() {
        if (StringUtils.hasText(properties.getApiKey())) {
            return properties.getApiKey();
        }
        return fallbackApiKey();
    }

    protected abstract String specificBaseUrl();

    protected abstract String specificModel();

    @Override
    public boolean isConfigured() {
        if (!StringUtils.hasText(resolveBaseUrl()) || !StringUtils.hasText(resolveModel())) {
            return false;
        }
        if (apiKeyRequired() && !StringUtils.hasText(resolveApiKey()) && !isLoopback(resolveBaseUrl())) {
            return false;
        }
        return true;
    }

    @Override
    public String describe() {
        String base = resolveBaseUrl();
        String model = resolveModel();
        String state = isConfigured() ? "configured" : "not configured";
        return type() + " (" + state + ") - model " + model + " at " + base;
    }

    @Override
    public ChatCompletion complete(List<ChatTurn> messages) {
        if (!isConfigured()) {
            throw new ChatProviderException(type(), type() + " provider is not configured");
        }

        String url = chatCompletionsUrl(resolveBaseUrl());
        String payload = buildPayload(messages, resolveModel());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String apiKey = resolveApiKey();
        if (StringUtils.hasText(apiKey)) {
            headers.setBearerAuth(apiKey);
        }

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, new HttpEntity<>(payload, headers),
                    String.class);
            return parseCompletion(response.getBody());
        } catch (ChatProviderException e) {
            throw e;
        } catch (RestClientException e) {
            throw new ChatProviderException(type(),
                    "Could not reach " + type() + " at " + hostOf(url) + ": " + safeMessage(e), e);
        }
    }

    private String buildPayload(List<ChatTurn> messages, String model) {
        List<Map<String, String>> serialized = new ArrayList<>();
        for (ChatTurn turn : messages) {
            serialized.add(Map.of("role", turn.getRole(), "content", turn.getContent()));
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model);
        body.put("messages", serialized);
        body.put("temperature", properties.getTemperature());
        body.put("stream", false);
        try {
            return objectMapper.writeValueAsString(body);
        } catch (Exception e) {
            throw new ChatProviderException(type(), "Could not serialize chat payload", e);
        }
    }

    private ChatCompletion parseCompletion(String body) {
        try {
            JsonNode root = objectMapper.readTree(body);
            JsonNode choices = root.path("choices");
            if (choices.isArray() && choices.size() > 0) {
                JsonNode content = choices.get(0).path("message").path("content");
                if (content.isTextual() && StringUtils.hasText(content.asText())) {
                    String model = root.hasNonNull("model") ? root.get("model").asText() : resolveModel();
                    return new ChatCompletion(content.asText().trim(), model);
                }
            }
            throw new ChatProviderException(type(),
                    type() + " returned an unexpected response structure: " + truncate(body, 200));
        } catch (ChatProviderException e) {
            throw e;
        } catch (Exception e) {
            throw new ChatProviderException(type(), type() + " returned a malformed response", e);
        }
    }

    protected static String chatCompletionsUrl(String baseUrl) {
        String base = trimTrailingSlash(baseUrl);
        if (base.endsWith("/v1")) {
            return base + "/chat/completions";
        }
        return base + "/v1/chat/completions";
    }

    private static String trimTrailingSlash(String value) {
        return value == null ? "" : value.replaceAll("/+$", "");
    }

    private static boolean isLoopback(String baseUrl) {
        String lower = baseUrl.toLowerCase();
        return lower.startsWith("http://localhost") || lower.startsWith("http://127.0.0.1")
                || lower.startsWith("https://localhost") || lower.startsWith("https://127.0.0.1");
    }

    private static String hostOf(String url) {
        try {
            return java.net.URI.create(url).getHost() + ":" + java.net.URI.create(url).getPort();
        } catch (Exception e) {
            return "configured host";
        }
    }

    private static String safeMessage(Exception e) {
        return truncate(e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage(), 200);
    }

    private static String truncate(String value, int max) {
        if (value == null) {
            return "";
        }
        return value.length() <= max ? value : value.substring(0, max) + "...";
    }
}
