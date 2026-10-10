package com.rmos.service.ai.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rmos.config.ChatProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Local Ollama chat provider - the genuinely free, open-source option.
 *
 * Ollama runs open-weight models entirely on local hardware with no API key
 * and no usage quotas. It exposes an OpenAI-compatible endpoint at
 * {@code http://localhost:11434/v1/chat/completions} which this provider uses.
 *
 * Setup: install Ollama, run {@code ollama pull llama3.2} (or set
 * RMOS_AI_CHAT_OLLAMA_MODEL to another pulled model).
 *
 * This provider is only considered "configured" when running locally (loopback
 * address) or when explicitly configured with a remote Ollama endpoint.
 * It will NOT be considered configured on cloud deployments (e.g. Render)
 * where localhost:11434 is not reachable.
 */
@Component
public class OllamaChatProvider extends OpenAiCompatibleChatProvider {

    private static final String DEFAULT_BASE_URL = "http://localhost:11434";
    private static final String DEFAULT_MODEL = "llama3.2";

    public OllamaChatProvider(ChatProperties chatProperties,
            RestTemplateBuilder restTemplateBuilder,
            ObjectMapper objectMapper) {
        super(chatProperties, restTemplateBuilder, objectMapper);
    }

    @Override
    public String type() {
        return "ollama";
    }

    @Override
    protected String defaultBaseUrl() {
        return DEFAULT_BASE_URL;
    }

    @Override
    protected String defaultModel() {
        return DEFAULT_MODEL;
    }

    @Override
    protected String specificBaseUrl() {
        return properties.getOllama().getBaseUrl();
    }

    @Override
    protected String specificModel() {
        return properties.getOllama().getModel();
    }

    @Override
    protected boolean apiKeyRequired() {
        return false;
    }

    @Override
    public boolean isConfigured() {
        // Only consider Ollama configured if:
        // 1. Base URL and model are set (from parent check), AND
        // 2. The base URL is a loopback address (local dev) OR explicitly overridden to a non-default value
        if (!super.isConfigured()) {
            return false;
        }
        String baseUrl = resolveBaseUrl();
        // Allow loopback (local development)
        if (isLoopbackAddress(baseUrl)) {
            return true;
        }
        // Allow explicitly configured non-loopback URL (remote Ollama instance)
        String specificUrl = specificBaseUrl();
        if (StringUtils.hasText(specificUrl) && !specificUrl.equals(DEFAULT_BASE_URL)) {
            return true;
        }
        // Default localhost URL on a non-local environment = not configured
        return false;
    }

    private boolean isLoopbackAddress(String baseUrl) {
        String lower = baseUrl.toLowerCase();
        return lower.startsWith("http://localhost") || lower.startsWith("http://127.0.0.1")
                || lower.startsWith("https://localhost") || lower.startsWith("https://127.0.0.1");
    }

    @Override
    public String describe() {
        String base = resolveBaseUrl();
        String model = resolveModel();
        boolean configured = isConfigured();
        String state = configured ? "configured" : "not configured (localhost only)";
        return type() + " (" + state + ") - model " + model + " at " + base;
    }
}
