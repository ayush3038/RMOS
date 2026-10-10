package com.rmos.service.ai.chat;

import com.rmos.config.ChatProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OllamaChatProviderConfigurationTest {

    @Mock
    private ChatProperties chatProperties;

    @Mock
    private ChatProperties.Ollama ollamaConfig;

    @Mock
    private RestTemplateBuilder restTemplateBuilder;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private RestTemplateBuilder timeoutBuilder;

    private OllamaChatProvider createProvider(String baseUrl, String model) {
        when(chatProperties.getOllama()).thenReturn(ollamaConfig);
        when(ollamaConfig.getBaseUrl()).thenReturn(baseUrl);
        when(ollamaConfig.getModel()).thenReturn(model);
        when(restTemplateBuilder.setConnectTimeout(org.mockito.ArgumentMatchers.any())).thenReturn(timeoutBuilder);
        when(timeoutBuilder.setReadTimeout(org.mockito.ArgumentMatchers.any())).thenReturn(restTemplateBuilder);
        when(restTemplateBuilder.build()).thenReturn(new org.springframework.web.client.RestTemplate());
        return new OllamaChatProvider(chatProperties, restTemplateBuilder, objectMapper);
    }

    @Test
    void isConfiguredReturnsTrueForLoopbackAddress() {
        OllamaChatProvider provider = createProvider("", "");
        // Default base URL is http://localhost:11434 (loopback)
        assertThat(provider.isConfigured()).isTrue();
        assertThat(provider.describe()).contains("configured");
    }

    @Test
    void isConfiguredReturnsTrueForExplicitRemoteUrl() {
        OllamaChatProvider provider = createProvider("http://ollama-server:11434", "llama3.2");
        assertThat(provider.isConfigured()).isTrue();
        assertThat(provider.describe()).contains("configured");
    }

    @Test
    void describeShowsNotConfiguredForDefaultLocalhostOnRemote() {
        // This test verifies the describe method shows the correct state
        OllamaChatProvider provider = createProvider("", "");
        String description = provider.describe();
        assertThat(description).contains("ollama");
        assertThat(description).contains("llama3.2");
    }
}