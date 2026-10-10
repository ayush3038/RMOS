package com.rmos.service.ai.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rmos.config.ChatProperties;
import com.rmos.dto.chat.ChatTurn;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OllamaChatProviderTest {

    private RestTemplateBuilder builder;
    private RestTemplate restTemplate;
    private ChatProperties chatProperties;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        builder = mock(RestTemplateBuilder.class);
        restTemplate = mock(RestTemplate.class);
        when(builder.setConnectTimeout(any(Duration.class))).thenReturn(builder);
        when(builder.setReadTimeout(any(Duration.class))).thenReturn(builder);
        when(builder.build()).thenReturn(restTemplate);
        chatProperties = new ChatProperties();
        objectMapper = new ObjectMapper();
    }

    private OllamaChatProvider provider() {
        return new OllamaChatProvider(chatProperties, builder, objectMapper);
    }

    @Test
    void defaults_AreConfiguredWithoutAnyEnvironment() {
        assertTrue(provider().isConfigured());
        assertTrue(provider().describe().contains("http://localhost:11434"));
        assertTrue(provider().describe().contains("llama3.2"));
    }

    @Test
    void type_IsOllama() {
        assertEquals("ollama", provider().type());
    }

    @Test
    @SuppressWarnings("unchecked")
    void complete_UsesOpenAiCompatibleLocalEndpointWithoutAuth() {
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), any(Class.class)))
                .thenReturn(ResponseEntity.ok(
                        "{\"model\":\"llama3.2\",\"choices\":[{\"message\":{\"content\":\"local answer\"}}]}"));

        ChatCompletion completion = provider().complete(List.of(new ChatTurn("user", "hi")));

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForEntity(urlCaptor.capture(), entityCaptor.capture(), any(Class.class));

        assertEquals("http://localhost:11434/v1/chat/completions", urlCaptor.getValue());
        assertFalse(entityCaptor.getValue().getHeaders().containsKey(HttpHeaders.AUTHORIZATION));
        assertEquals("local answer", completion.getContent());
        assertEquals("llama3.2", completion.getModel());
    }

    @Test
    void overrides_ChatLevelModelAppliesWhenOllamaModelBlank() {
        chatProperties.setModel("qwen2.5:7b");

        assertTrue(provider().isConfigured());
        assertTrue(provider().describe().contains("qwen2.5:7b"));
    }

    @Test
    void complete_ServerUnreachable_ThrowsProviderException() {
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), any(Class.class)))
                .thenThrow(new org.springframework.web.client.ResourceAccessException(
                        "I/O error: Connection refused: connect"));

        ChatProviderException ex = assertThrows(ChatProviderException.class,
                () -> provider().complete(List.of(new ChatTurn("user", "hi"))));
        assertEquals("ollama", ex.getProviderType());
        assertTrue(ex.getMessage().contains("Could not reach"));
    }
}
