package com.rmos.service.ai.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rmos.config.ChatProperties;
import com.rmos.config.NimProperties;
import com.rmos.dto.chat.ChatTurn;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
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

class NvidiaNimChatProviderTest {

    private RestTemplateBuilder builder;
    private RestTemplate restTemplate;
    private ChatProperties chatProperties;
    private NimProperties nimProperties;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        builder = mock(RestTemplateBuilder.class);
        restTemplate = mock(RestTemplate.class);
        when(builder.setConnectTimeout(any(Duration.class))).thenReturn(builder);
        when(builder.setReadTimeout(any(Duration.class))).thenReturn(builder);
        when(builder.build()).thenReturn(restTemplate);
        chatProperties = new ChatProperties();
        nimProperties = new NimProperties();
        objectMapper = new ObjectMapper();
    }

    private NvidiaNimChatProvider provider() {
        return new NvidiaNimChatProvider(chatProperties, nimProperties, builder, objectMapper);
    }

    @Test
    void isConfigured_HostedEndpointWithKey_ReturnsTrue() {
        nimProperties.setBaseUrl("https://integrate.api.nvidia.com/v1");
        nimProperties.setModel("meta/llama-3.1-8b-instruct");
        nimProperties.setApiKey("nvapi-test");

        assertTrue(provider().isConfigured());
    }

    @Test
    void isConfigured_ModelMissing_ReturnsFalse() {
        nimProperties.setBaseUrl("https://integrate.api.nvidia.com/v1");
        nimProperties.setApiKey("nvapi-test");

        assertFalse(provider().isConfigured());
    }

    @Test
    void isConfigured_HostedEndpointWithoutKey_ReturnsFalse() {
        nimProperties.setBaseUrl("https://integrate.api.nvidia.com/v1");
        nimProperties.setModel("meta/llama-3.1-8b-instruct");

        assertFalse(provider().isConfigured());
    }

    @Test
    void isConfigured_LoopbackWithoutKey_ReturnsTrue() {
        nimProperties.setBaseUrl("http://localhost:8000");
        nimProperties.setModel("meta/llama-3.1-8b-instruct");

        assertTrue(provider().isConfigured());
    }

    @Test
    @SuppressWarnings("unchecked")
    void complete_BaseUrlWithV1Suffix_DoesNotDoublePrefix() {
        nimProperties.setBaseUrl("https://integrate.api.nvidia.com/v1/");
        nimProperties.setModel("meta/llama-3.1-8b-instruct");
        nimProperties.setApiKey("nvapi-test");

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), any(Class.class)))
                .thenReturn(ResponseEntity.ok(
                        "{\"model\":\"meta/llama-3.1-8b-instruct\",\"choices\":[{\"message\":{\"content\":\"hello\"}}]}"));

        ChatCompletion completion = provider().complete(List.of(new ChatTurn("user", "hi")));

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForEntity(urlCaptor.capture(), entityCaptor.capture(), any(Class.class));

        assertEquals("https://integrate.api.nvidia.com/v1/chat/completions", urlCaptor.getValue());
        assertEquals("hello", completion.getContent());
        assertEquals("meta/llama-3.1-8b-instruct", completion.getModel());
        assertEquals("Bearer nvapi-test", entityCaptor.getValue().getHeaders().getFirst(HttpHeaders.AUTHORIZATION));
    }

    @Test
    @SuppressWarnings("unchecked")
    void complete_LocalBaseUrl_AppendsV1() {
        nimProperties.setBaseUrl("http://localhost:8000");
        nimProperties.setModel("qwen/qwen2.5-7b-instruct");

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), any(Class.class)))
                .thenReturn(ResponseEntity.ok(
                        "{\"choices\":[{\"message\":{\"content\":\"local\"}}]}"));

        provider().complete(List.of(new ChatTurn("user", "hi")));

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForEntity(urlCaptor.capture(), entityCaptor.capture(), any(Class.class));

        assertEquals("http://localhost:8000/v1/chat/completions", urlCaptor.getValue());
        // No API key configured on loopback: no Authorization header may be sent.
        assertFalse(entityCaptor.getValue().getHeaders().containsKey(HttpHeaders.AUTHORIZATION));
    }

    @Test
    void complete_HttpError_ThrowsProviderExceptionWithoutSecrets() {
        nimProperties.setBaseUrl("https://integrate.api.nvidia.com/v1");
        nimProperties.setModel("meta/llama-3.1-8b-instruct");
        nimProperties.setApiKey("nvapi-super-secret");

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), any(Class.class)))
                .thenThrow(HttpClientErrorException.create(
                        org.springframework.http.HttpStatus.UNAUTHORIZED,
                        "Unauthorized", HttpHeaders.EMPTY,
                        "{\"error\":\"invalid api key\"}".getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));

        ChatProviderException ex = assertThrows(ChatProviderException.class,
                () -> provider().complete(List.of(new ChatTurn("user", "hi"))));

        assertEquals("nim", ex.getProviderType());
        assertTrue(ex.getMessage().contains("401"));
        assertFalse(ex.getMessage().contains("nvapi-super-secret"));
    }

    @Test
    void complete_NotConfigured_ThrowsProviderException() {
        ChatProviderException ex = assertThrows(ChatProviderException.class,
                () -> provider().complete(List.of(new ChatTurn("user", "hi"))));
        assertTrue(ex.getMessage().contains("not configured"));
    }

    @Test
    void complete_MalformedResponse_ThrowsProviderException() {
        nimProperties.setBaseUrl("http://localhost:8000");
        nimProperties.setModel("qwen/qwen2.5-7b-instruct");

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), any(Class.class)))
                .thenReturn(ResponseEntity.ok("{\"unexpected\":true}"));

        ChatProviderException ex = assertThrows(ChatProviderException.class,
                () -> provider().complete(List.of(new ChatTurn("user", "hi"))));
        assertTrue(ex.getMessage().contains("unexpected response structure"));
    }
}
