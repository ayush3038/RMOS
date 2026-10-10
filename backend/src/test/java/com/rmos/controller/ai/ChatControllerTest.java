package com.rmos.controller.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rmos.dto.chat.ChatRequest;
import com.rmos.dto.chat.ChatResponse;
import com.rmos.dto.chat.ChatStatusResponse;
import com.rmos.service.ai.chat.ChatAssistantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@org.springframework.context.annotation.Import({com.rmos.security.SecurityConfig.class,
        com.rmos.security.JwtAuthFilter.class,
        com.rmos.security.CustomAuthenticationEntryPoint.class,
        com.rmos.security.CustomAccessDeniedHandler.class})
@WebMvcTest(ChatController.class)
@WithMockUser(roles = "VIEWER")
class ChatControllerTest {

    @org.springframework.boot.test.mock.mockito.MockBean
    private com.rmos.security.JwtUtils jwtUtils;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChatAssistantService chatService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void messages_AuthenticatedUser_ReturnsReply() throws Exception {
        ChatResponse response = new ChatResponse();
        response.setReply("The plan prioritises MT-001 because of its critical safety rating.");
        response.setProvider("nim");
        response.setModel("meta/llama-3.1-8b-instruct");
        response.setRetrievedContextCount(2);
        response.setSystemDataIncluded(true);
        response.setRequiresHumanReview(true);
        response.setDisclaimer("AI-generated explanation for review.");

        when(chatService.ask(any(), any())).thenReturn(response);

        ChatRequest request = new ChatRequest("Why was this asset prioritized?", null);

        mockMvc.perform(post("/api/v1/chat/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reply").value(
                        "The plan prioritises MT-001 because of its critical safety rating."))
                .andExpect(jsonPath("$.provider").value("nim"))
                .andExpect(jsonPath("$.retrievedContextCount").value(2))
                .andExpect(jsonPath("$.requiresHumanReview").value(true));
    }

    @Test
    void messages_BlankMessage_ReturnsBadRequest() throws Exception {
        ChatRequest request = new ChatRequest("   ", null);

        mockMvc.perform(post("/api/v1/chat/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    void messages_MalformedBody_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/chat/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ not json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MALFORMED_REQUEST"));
    }

    @Test
    @WithAnonymousUser
    void messages_Unauthenticated_ReturnsUnauthorized() throws Exception {
        ChatRequest request = new ChatRequest("Explain this maintenance plan.", null);

        mockMvc.perform(post("/api/v1/chat/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void messages_ProviderUnavailable_ReturnsServiceUnavailableWithMessage() throws Exception {
        when(chatService.ask(any(), any())).thenThrow(new ResponseStatusException(
                org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE,
                "No AI provider is configured. Configure RMOS_NIM_API_KEY + RMOS_NIM_MODEL."));

        ChatRequest request = new ChatRequest("Explain this maintenance plan.", null);

        mockMvc.perform(post("/api/v1/chat/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.message").value(
                        "No AI provider is configured. Configure RMOS_NIM_API_KEY + RMOS_NIM_MODEL."))
                .andExpect(jsonPath("$.code").value("503_SERVICE_UNAVAILABLE"));
    }

    @Test
    void status_AuthenticatedUser_ReturnsAvailability() throws Exception {
        ChatStatusResponse status = new ChatStatusResponse();
        status.setAvailable(true);
        status.setProvider("ollama");
        status.setModel("llama3.2");
        status.setMessage("AI assistant ready via ollama (document retrieval not configured).");
        status.setRetrievalConfigured(false);

        when(chatService.status()).thenReturn(status);

        mockMvc.perform(get("/api/v1/chat/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.provider").value("ollama"))
                .andExpect(jsonPath("$.model").value("llama3.2"))
                .andExpect(jsonPath("$.retrievalConfigured").value(false));
    }

    @Test
    @WithAnonymousUser
    void status_Unauthenticated_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/chat/status"))
                .andExpect(status().isUnauthorized());
    }
}
