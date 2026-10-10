package com.rmos.service.ai.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rmos.config.ChatProperties;
import com.rmos.config.GeminiProperties;
import com.rmos.dto.chat.ChatTurn;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
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

/**
 * Gemini provider tests. All HTTP responses are mocked - no live API key and no
 * network access is ever required.
 */
class GeminiChatProviderTest {

    private RestTemplateBuilder builder;
    private RestTemplate restTemplate;
    private ChatProperties chatProperties;
    private GeminiProperties geminiProperties;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        builder = mock(RestTemplateBuilder.class);
        restTemplate = mock(RestTemplate.class);
        when(builder.setConnectTimeout(any(Duration.class))).thenReturn(builder);
        when(builder.setReadTimeout(any(Duration.class))).thenReturn(builder);
        when(builder.build()).thenReturn(restTemplate);
        chatProperties = new ChatProperties();
        geminiProperties = new GeminiProperties();
        objectMapper = new ObjectMapper();
    }

    private GeminiChatProvider provider() {
        return new GeminiChatProvider(chatProperties, geminiProperties, builder, objectMapper);
    }

    // ------------------------------------------------------------------
    // Configuration
    // ------------------------------------------------------------------

    @Test
    void defaultModelIsTheDocumentedStableFlashLiteModel() {
        assertEquals("gemini-3.1-flash-lite", geminiProperties.getModel());
        assertEquals("https://generativelanguage.googleapis.com", geminiProperties.getBaseUrl());
        assertEquals("v1beta", geminiProperties.getApiVersion());
    }

    @Test
    void typeIsGemini() {
        assertEquals("gemini", provider().type());
    }

    @Test
    void isConfigured_WithRealKey_ReturnsTrue() {
        geminiProperties.setApiKey("AIzaSyTEST-REAL-KEY");
        assertTrue(provider().isConfigured());
    }

    @Test
    void isConfigured_WithoutKey_ReturnsFalse() {
        geminiProperties.setApiKey("");
        assertFalse(provider().isConfigured());
    }

    @Test
    void isConfigured_WithPlaceholderKey_ReturnsFalse() {
        geminiProperties.setApiKey("PASTE_YOUR_REAL_KEY_HERE");
        assertFalse(provider().isConfigured());
    }

    @Test
    void isConfigured_WithPlaceholderKey_NeverAttemptsARequest() {
        geminiProperties.setApiKey("PASTE_YOUR_REAL_KEY_HERE");

        ChatProviderException ex = assertThrows(ChatProviderException.class,
                () -> provider().complete(List.of(new ChatTurn("user", "hi"))));

        assertTrue(ex.getMessage().contains("not configured"));
        // No HTTP call may be made with a placeholder key.
        org.mockito.Mockito.verifyNoInteractions(restTemplate);
    }

    @Test
    void describeNeverContainsTheApiKey() {
        geminiProperties.setApiKey("AIzaSySUPER-SECRET");
        assertFalse(provider().describe().contains("AIzaSySUPER-SECRET"));
    }

    // ------------------------------------------------------------------
    // Completion
    // ------------------------------------------------------------------

    @Test
    @SuppressWarnings("unchecked")
    void complete_UsesDocumentedEndpointAuthHeaderAndModel() {
        geminiProperties.setApiKey("AIzaSyTEST-REAL-KEY");

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), any(Class.class)))
                .thenReturn(ResponseEntity.ok(
                        "{\"candidates\":[{\"content\":{\"role\":\"model\",\"parts\":[{\"text\":\"Block planning is constrained.\"}]},\"finishReason\":\"STOP\"}]}"));

        ChatCompletion completion = provider().complete(List.of(
                new ChatTurn("system", "You are the RMOS assistant."),
                new ChatTurn("user", "Explain block planning.")));

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForEntity(urlCaptor.capture(), entityCaptor.capture(), any(Class.class));

        assertEquals("https://generativelanguage.googleapis.com/v1beta/models/"
                + "gemini-3.1-flash-lite:generateContent", urlCaptor.getValue());
        assertEquals("AIzaSyTEST-REAL-KEY",
                entityCaptor.getValue().getHeaders().getFirst("x-goog-api-key"));
        assertEquals("gemini-3.1-flash-lite", completion.getModel());
        assertEquals("Block planning is constrained.", completion.getContent());

        String payload = entityCaptor.getValue().getBody().toString();
        // system prompt is sent out of band as systemInstruction
        assertTrue(payload.contains("systemInstruction"));
        assertFalse(payload.contains("\"role\":\"system\""));
        // assistant turns map to the Gemini "model" role
        assertTrue(payload.contains("contents"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void complete_AssistantHistoryTurnMapsToModelRole() {
        geminiProperties.setApiKey("AIzaSyTEST-REAL-KEY");
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), any(Class.class)))
                .thenReturn(ResponseEntity.ok(
                        "{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"ok\"}]}}]}"));

        provider().complete(List.of(
                new ChatTurn("user", "first"),
                new ChatTurn("assistant", "second"),
                new ChatTurn("user", "third")));

        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForEntity(anyString(), entityCaptor.capture(), any(Class.class));
        assertTrue(entityCaptor.getValue().getBody().toString().contains("\"role\":\"model\""));
    }

    @Test
    @SuppressWarnings("unchecked")
    void complete_RespectsConfiguredModelOverride() {
        geminiProperties.setApiKey("AIzaSyTEST-REAL-KEY");
        geminiProperties.setModel("gemini-3.1-flash-lite");
        chatProperties.getGemini().setModel("gemini-3.1-flash-lite");

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), any(Class.class)))
                .thenReturn(ResponseEntity.ok(
                        "{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"ok\"}]}}]}"));

        provider().complete(List.of(new ChatTurn("user", "hi")));

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(restTemplate).postForEntity(urlCaptor.capture(), any(HttpEntity.class), any(Class.class));
        assertTrue(urlCaptor.getValue().contains("models/gemini-3.1-flash-lite:generateContent"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void complete_RateLimited_ReturnsHonestQuotaErrorWithoutKey() {
        geminiProperties.setApiKey("AIzaSySUPER-SECRET");

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), any(Class.class)))
                .thenThrow(HttpClientErrorException.create(
                        HttpStatus.TOO_MANY_REQUESTS, "Too Many Requests", HttpHeaders.EMPTY,
                        ("{\"error\":{\"code\":429,\"message\":\"Resource has been exhausted\"}}")
                                .getBytes(StandardCharsets.UTF_8),
                        StandardCharsets.UTF_8));

        ChatProviderException ex = assertThrows(ChatProviderException.class,
                () -> provider().complete(List.of(new ChatTurn("user", "hi"))));

        assertEquals("gemini", ex.getProviderType());
        assertTrue(ex.getMessage().contains("429"));
        assertTrue(ex.getMessage().contains("quota"));
        assertFalse(ex.getMessage().contains("AIzaSySUPER-SECRET"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void complete_Unauthorized_ExplainsKeyProblem() {
        geminiProperties.setApiKey("AIzaSyBAD");

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), any(Class.class)))
                .thenThrow(HttpClientErrorException.create(
                        HttpStatus.UNAUTHORIZED, "Unauthorized", HttpHeaders.EMPTY,
                        "{\"error\":{\"code\":401,\"message\":\"API key not valid\"}}"
                                .getBytes(StandardCharsets.UTF_8),
                        StandardCharsets.UTF_8));

        ChatProviderException ex = assertThrows(ChatProviderException.class,
                () -> provider().complete(List.of(new ChatTurn("user", "hi"))));
        assertTrue(ex.getMessage().contains("GEMINI_API_KEY"));
        assertFalse(ex.getMessage().contains("AIzaSyBAD"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void complete_Timeout_IsReportedAsProviderFailure() {
        geminiProperties.setApiKey("AIzaSyTEST-REAL-KEY");

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), any(Class.class)))
                .thenThrow(new ResourceAccessException("Read timed out"));

        ChatProviderException ex = assertThrows(ChatProviderException.class,
                () -> provider().complete(List.of(new ChatTurn("user", "hi"))));
        assertEquals("gemini", ex.getProviderType());
    }

    @Test
    @SuppressWarnings("unchecked")
    void complete_ServerError_IsReportedAsProviderFailure() {
        geminiProperties.setApiKey("AIzaSyTEST-REAL-KEY");

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), any(Class.class)))
                .thenThrow(HttpServerErrorException.create(
                        HttpStatus.INTERNAL_SERVER_ERROR, "Server Error", HttpHeaders.EMPTY,
                        new byte[0], StandardCharsets.UTF_8));

        ChatProviderException ex = assertThrows(ChatProviderException.class,
                () -> provider().complete(List.of(new ChatTurn("user", "hi"))));
        assertTrue(ex.getMessage().contains("500"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void complete_MalformedResponse_ThrowsProviderException() {
        geminiProperties.setApiKey("AIzaSyTEST-REAL-KEY");

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), any(Class.class)))
                .thenReturn(ResponseEntity.ok("{\"unexpected\":true}"));

        ChatProviderException ex = assertThrows(ChatProviderException.class,
                () -> provider().complete(List.of(new ChatTurn("user", "hi"))));
        assertTrue(ex.getMessage().contains("unexpected response structure"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void complete_MultipleParts_AreConcatenated() {
        geminiProperties.setApiKey("AIzaSyTEST-REAL-KEY");

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), any(Class.class)))
                .thenReturn(ResponseEntity.ok(
                        "{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"line one \"},{\"text\":\"line two\"}]}}]}"));

        ChatCompletion completion = provider().complete(List.of(new ChatTurn("user", "hi")));
        assertEquals("line one line two", completion.getContent());
    }

    @Test
    @SuppressWarnings("unchecked")
    void complete_BlockedSafetyResponse_ReportsNoTextContent() {
        geminiProperties.setApiKey("AIzaSyTEST-REAL-KEY");

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), any(Class.class)))
                .thenReturn(ResponseEntity.ok(
                        "{\"candidates\":[{\"content\":{\"parts\":[]},\"finishReason\":\"SAFETY\"}]}"));

        ChatProviderException ex = assertThrows(ChatProviderException.class,
                () -> provider().complete(List.of(new ChatTurn("user", "hi"))));
        assertTrue(ex.getMessage().contains("no text content"));
    }
}