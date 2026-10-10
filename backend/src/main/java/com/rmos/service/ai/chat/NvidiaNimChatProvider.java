package com.rmos.service.ai.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rmos.config.ChatProperties;
import com.rmos.config.NimProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;

/**
 * NVIDIA NIM chat provider.
 *
 * Reuses the existing {@link NimProperties} (rmos.ai.nim.*) configuration when
 * no chat-specific override is supplied, so the structured explanation feature
 * and the assistant share the same endpoint credentials.
 *
 * Works against both the hosted NVIDIA API (requires an API key) and a local
 * NIM container (loopback, key optional).
 */
@Component
public class NvidiaNimChatProvider extends OpenAiCompatibleChatProvider {

    private final NimProperties nimProperties;

    public NvidiaNimChatProvider(ChatProperties chatProperties,
            NimProperties nimProperties,
            RestTemplateBuilder restTemplateBuilder,
            ObjectMapper objectMapper) {
        super(chatProperties, restTemplateBuilder, objectMapper);
        this.nimProperties = nimProperties;
    }

    @Override
    public String type() {
        return "nim";
    }

    @Override
    protected String defaultBaseUrl() {
        return nimProperties.getBaseUrl();
    }

    @Override
    protected String defaultModel() {
        return nimProperties.getModel();
    }

    @Override
    protected String specificBaseUrl() {
        return properties.getNim().getBaseUrl();
    }

    @Override
    protected String specificModel() {
        return properties.getNim().getModel();
    }

    @Override
    protected String fallbackApiKey() {
        return nimProperties.getApiKey();
    }

    @Override
    protected boolean apiKeyRequired() {
        return true;
    }
}
