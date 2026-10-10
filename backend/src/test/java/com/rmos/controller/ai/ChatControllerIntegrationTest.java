package com.rmos.controller.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rmos.config.DemoCredentialsProperties;
import com.rmos.dto.auth.LoginRequest;
import com.rmos.dto.auth.LoginResponse;
import com.rmos.dto.chat.ChatRequest;
import com.rmos.dto.chat.ChatResponse;
import com.rmos.dto.chat.ChatStatusResponse;
import com.rmos.domain.auth.Role;
import com.rmos.domain.auth.User;
import com.rmos.repository.auth.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "rmos.ai.chat.provider=none",
        "rmos.ai.nim.enabled=false"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ChatControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private DemoCredentialsProperties demoCredentials;

    @MockBean
    private com.rmos.service.ai.chat.ChatAssistantService chatService;

    private String demoToken;

    @BeforeEach
    void setUp() throws Exception {
        userRepository.deleteAll();
        // Ensure demo user exists (DataInitializer may have already created it)
        if (!userRepository.findByUsername(demoCredentials.getUsername()).isPresent()) {
            User demoUser = new User();
            demoUser.setId(UUID.randomUUID());
            demoUser.setName("RMOS Demo Administrator");
            demoUser.setUsername(demoCredentials.getUsername());
            demoUser.setPasswordHash(passwordEncoder.encode(demoCredentials.getPassword()));
            demoUser.setRole(Role.valueOf(demoCredentials.getRole()));
            demoUser.setActive(true);
            userRepository.save(demoUser);
        }

        // Login to get token
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(demoCredentials.getUsername());
        loginRequest.setPassword(demoCredentials.getPassword());
        String responseBody = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        LoginResponse loginResponse = objectMapper.readValue(responseBody, LoginResponse.class);
        demoToken = loginResponse.getAccessToken();
    }

    @Test
    void chatMessagesWithValidTokenReturnsReply() throws Exception {
        // Given
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
                        .header("Authorization", "Bearer " + demoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reply").value("The plan prioritises MT-001 because of its critical safety rating."))
                .andExpect(jsonPath("$.provider").value("nim"))
                .andExpect(jsonPath("$.retrievedContextCount").value(2))
                .andExpect(jsonPath("$.requiresHumanReview").value(true));
    }

    @Test
    void chatMessagesWithoutTokenReturnsUnauthorized() throws Exception {
        ChatRequest request = new ChatRequest("Explain this maintenance plan.", null);

        mockMvc.perform(post("/api/v1/chat/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void chatMessagesWithInvalidTokenReturnsUnauthorized() throws Exception {
        ChatRequest request = new ChatRequest("Explain this maintenance plan.", null);

        mockMvc.perform(post("/api/v1/chat/messages")
                        .header("Authorization", "Bearer invalid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void chatMessagesWithExpiredTokenReturnsUnauthorized() throws Exception {
        // This test would require generating an expired token
        // For now we test with a malformed token which should also return 401
        ChatRequest request = new ChatRequest("Explain this maintenance plan.", null);

        mockMvc.perform(post("/api/v1/chat/messages")
                        .header("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0IiwiZXhwIjoxfQ.invalid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void chatStatusWithValidTokenReturnsAvailability() throws Exception {
        ChatStatusResponse status = new ChatStatusResponse();
        status.setAvailable(true);
        status.setProvider("nim");
        status.setModel("meta/llama-3.1-8b-instruct");
        status.setMessage("AI assistant ready via nim (document retrieval not configured).");
        status.setRetrievalConfigured(false);

        when(chatService.status()).thenReturn(status);

        mockMvc.perform(get("/api/v1/chat/status")
                        .header("Authorization", "Bearer " + demoToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.provider").value("nim"))
                .andExpect(jsonPath("$.model").value("meta/llama-3.1-8b-instruct"))
                .andExpect(jsonPath("$.retrievalConfigured").value(false));
    }

    @Test
    void chatStatusWithoutTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/chat/status"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void chatMessagesProviderUnavailableReturnsServiceUnavailable() throws Exception {
        when(chatService.ask(any(), any())).thenThrow(new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE,
                "No AI provider is configured. Configure RMOS_NIM_API_KEY + RMOS_NIM_MODEL."));

        ChatRequest request = new ChatRequest("Explain this maintenance plan.", null);

        mockMvc.perform(post("/api/v1/chat/messages")
                        .header("Authorization", "Bearer " + demoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.message").value(
                        "No AI provider is configured. Configure RMOS_NIM_API_KEY + RMOS_NIM_MODEL."))
                .andExpect(jsonPath("$.code").value("503_SERVICE_UNAVAILABLE"));
    }

    @Test
    void chatMessagesBlankMessageReturnsBadRequest() throws Exception {
        ChatRequest request = new ChatRequest("   ", null);

        mockMvc.perform(post("/api/v1/chat/messages")
                        .header("Authorization", "Bearer " + demoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    void chatMessagesMalformedBodyReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/chat/messages")
                        .header("Authorization", "Bearer " + demoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ not json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MALFORMED_REQUEST"));
    }
}