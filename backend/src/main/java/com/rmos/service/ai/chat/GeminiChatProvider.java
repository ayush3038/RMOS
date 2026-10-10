package com.rmos.service.ai.chat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rmos.config.ChatProperties;
import com.rmos.config.GeminiProperties;
import com.rmos.dto.chat.ChatTurn;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Google Gemini chat provider.
 *
 * <p>Talks to Google's documented Generative Language endpoint:
 * {@code POST {baseUrl}/{apiVersion}/models/{model}:generateContent} using the
 * {@code x-goog-api-key} header. This is deliberately a separate transport from
 * the OpenAI-compatible {@link OpenAiCompatibleChatProvider} because Gemini uses
 * its own {@code contents}/{@code parts} schema and its own authentication header.
 *
 * <p>Cost/privacy guardrails:
 * <ul>
 *   <li>No billing is enabled or required; only Google's free-tier models are used.</li>
 *   <li>There is no paid fallback: if the key is missing, the quota is exhausted or
 *       the call fails, the provider reports a clear unavailability and the chat
 *       service either falls back to the next configured provider or returns
 *       {@code 503}.</li>
 *   <li>A placeholder key is treated as "not configured" so it can never produce a
 *       fake successful response.</li>
 * </ul>
 */
@Component
public class GeminiChatProvider implements ChatProvider {

    private final ChatProperties chatProperties;
    private final GeminiProperties geminiProperties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public GeminiChatProvider(ChatProperties chatProperties,
            GeminiProperties geminiProperties,
            RestTemplateBuilder restTemplateBuilder,
            ObjectMapper objectMapper) {
        this.chatProperties = chatProperties;
        this.geminiProperties = geminiProperties;
        this.objectMapper = objectMapper;
        Duration timeout = Duration.ofSeconds(Math.max(1, resolveTimeoutSeconds()));
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(timeout)
                .setReadTimeout(timeout)
                .build();
    }

    private int resolveTimeoutSeconds() {
        int configured = geminiProperties.getTimeoutSeconds();
        return configured > 0 ? configured : Math.max(1, chatProperties.getTimeoutSeconds());
    }

    @Override
    public String type() {
        return "gemini";
    }

    @Override
    public String model() {
        return resolveModel();
    }

    @Override
    public boolean isConfigured() {
        return StringUtils.hasText(resolveModel()) && hasUsableApiKey();
    }

    @Override
    public String describe() {
        return type() + " (" + (isConfigured() ? "configured" : "not configured")
                + ") - model " + resolveModel() + " at " + resolveBaseUrl();
    }

    private boolean hasUsableApiKey() {
        return !GeminiProperties.isPlaceholder(resolveApiKey());
    }

    private String resolveApiKey() {
        if (StringUtils.hasText(chatProperties.getGemini().getApiKey())) {
            return chatProperties.getGemini().getApiKey().trim();
        }
        String shared = chatProperties.getApiKey();
        if (StringUtils.hasText(shared)) {
            return shared.trim();
        }
        return geminiProperties.getApiKey() == null ? "" : geminiProperties.getApiKey().trim();
    }

    private String resolveBaseUrl() {
        String specific = chatProperties.getGemini().getBaseUrl();
        if (StringUtils.hasText(specific)) {
            return trimTrailingSlash(specific);
        }
        return trimTrailingSlash(geminiProperties.getBaseUrl());
    }

    private String resolveModel() {
        String specific = chatProperties.getGemini().getModel();
        if (StringUtils.hasText(specific)) {
            return specific.trim();
        }
        String shared = chatProperties.getModel();
        if (StringUtils.hasText(shared)) {
            return shared.trim();
        }
        return geminiProperties.getModel();
    }

    // ------------------------------------------------------------------
    // Completion
    // ------------------------------------------------------------------

    @Override
    public ChatCompletion complete(List<ChatTurn> messages) {
        if (!isConfigured()) {
            throw new ChatProviderException(type(),
                    "gemini provider is not configured (set GEMINI_API_KEY and GEMINI_MODEL)");
        }

        String url = generateContentUrl(resolveModel());
        String payload = buildPayload(messages);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // Google's documented authentication header.
        headers.set("x-goog-api-key", resolveApiKey());

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, new HttpEntity<>(payload, headers),
                    String.class);
            return parseCompletion(response.getBody());
        } catch (ChatProviderException e) {
            throw e;
        } catch (HttpStatusCodeException e) {
            throw translateHttpError(e);
        } catch (ResourceAccessException e) {
            throw new ChatProviderException(type(),
                    "Could not reach gemini at " + hostOf(url) + ": " + safeMessage(e), e);
        } catch (RestClientException e) {
            throw new ChatProviderException(type(), "gemini request failed: " + safeMessage(e), e);
        }
    }

    /** Google quota / availability problems must surface as a clear, honest error. */
    private ChatProviderException translateHttpError(HttpStatusCodeException e) {
        int status = e.getStatusCode().value();
        String detail = extractErrorMessage(e.getResponseBodyAsString());
        String base = "gemini request failed with HTTP " + status;
        if (!detail.isEmpty()) {
            base = base + ": " + detail;
        }
        if (status == 429) {
            return new ChatProviderException(type(),
                    base + ". Google Gemini free-tier quota/rate limit reached. RMOS does not enable billing "
                            + "and will not switch to a paid provider. Wait for the quota window to reset or use a "
                            + "different configured provider.", e);
        }
        if (status == 401 || status == 403) {
            return new ChatProviderException(type(),
                    base + ". Check GEMINI_API_KEY and that the Gemini API is enabled for the project.", e);
        }
        if (status == 404) {
            return new ChatProviderException(type(),
                    base + ". Check GEMINI_MODEL (default gemini-3.1-flash-lite) and its availability for this key.", e);
        }
        return new ChatProviderException(type(), base, e);
    }

    /** Pull a short human-readable message out of Google's JSON error envelope. */
    private String extractErrorMessage(String body) {
        if (!StringUtils.hasText(body)) {
            return "";
        }
        try {
            JsonNode root = objectMapper.readTree(body);
            JsonNode error = root.path("error");
            if (error.isObject()) {
                String message = error.path("message").asText("");
                if (StringUtils.hasText(message)) {
                    return truncate(message, 200);
                }
            }
            return "";
        } catch (Exception ignored) {
            return "";
        }
    }

    private String generateContentUrl(String model) {
        String base = trimTrailingSlash(resolveBaseUrl());
        String version = geminiProperties.getApiVersion();
        if (!StringUtils.hasText(version)) {
            version = "v1beta";
        }
        if (base.endsWith("/" + version)) {
            return base + "/models/" + URLEncoder.encode(model, StandardCharsets.UTF_8) + ":generateContent";
        }
        return base + "/" + version + "/models/" + URLEncoder.encode(model, StandardCharsets.UTF_8) + ":generateContent";
    }

    private String buildPayload(List<ChatTurn> messages) {
        String systemInstruction = null;
        List<Map<String, Object>> contents = new ArrayList<>();

        for (ChatTurn turn : messages) {
            if (turn == null || !StringUtils.hasText(turn.getContent())) {
                continue;
            }
            String role = turn.getRole() == null ? "user" : turn.getRole().trim().toLowerCase(Locale.ROOT);
            if ("system".equals(role)) {
                // Gemini carries system instructions out of band.
                systemInstruction = systemInstruction == null ? turn.getContent()
                        : systemInstruction + "\n\n" + turn.getContent();
                continue;
            }
            String geminiRole = "assistant".equals(role) ? "model" : "user";
            contents.add(Map.of("role", geminiRole,
                    "parts", List.of(Map.of("text", turn.getContent()))));
        }

        Map<String, Object> generationConfig = new LinkedHashMap<>();
        generationConfig.put("temperature", chatProperties.getTemperature());

        Map<String, Object> body = new LinkedHashMap<>();
        if (StringUtils.hasText(systemInstruction)) {
            body.put("systemInstruction", Map.of("parts", List.of(Map.of("text", systemInstruction))));
        }
        body.put("contents", contents);
        body.put("generationConfig", generationConfig);

        try {
            return objectMapper.writeValueAsString(body);
        } catch (Exception e) {
            throw new ChatProviderException(type(), "Could not serialize chat payload", e);
        }
    }

    private ChatCompletion parseCompletion(String body) {
        if (!StringUtils.hasText(body)) {
            throw new ChatProviderException(type(), "gemini returned an empty response");
        }
        try {
            JsonNode root = objectMapper.readTree(body);
            JsonNode candidates = root.path("candidates");
            if (candidates.isArray() && candidates.size() > 0) {
                JsonNode parts = candidates.get(0).path("content").path("parts");
                StringBuilder text = new StringBuilder();
                if (parts.isArray()) {
                    for (JsonNode part : parts) {
                        String value = part.path("text").asText("");
                        if (StringUtils.hasText(value)) {
                            text.append(value);
                        }
                    }
                }
                if (text.length() > 0) {
                    String model = resolveModel();
                    return new ChatCompletion(text.toString().trim(), model);
                }
                String blockReason = candidates.get(0).path("finishReason").asText("");
                throw new ChatProviderException(type(),
                        "gemini returned no text content"
                                + (StringUtils.hasText(blockReason) ? " (finishReason=" + blockReason + ")" : ""));
            }
            throw new ChatProviderException(type(),
                    "gemini returned an unexpected response structure: " + truncate(body, 200));
        } catch (ChatProviderException e) {
            throw e;
        } catch (Exception e) {
            throw new ChatProviderException(type(), "gemini returned a malformed response", e);
        }
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private static String trimTrailingSlash(String value) {
        return value == null ? "" : value.replaceAll("/+$", "");
    }

    private static String hostOf(String url) {
        try {
            java.net.URI uri = java.net.URI.create(url);
            return uri.getHost();
        } catch (Exception e) {
            return "generativelanguage.googleapis.com";
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