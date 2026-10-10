package com.rmos.controller.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rmos.config.DemoCredentialsProperties;
import com.rmos.dto.auth.LoginRequest;
import com.rmos.dto.auth.LoginResponse;
import com.rmos.domain.auth.Role;
import com.rmos.domain.auth.User;
import com.rmos.repository.auth.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "rmos.ai.chat.provider=none",
        "rmos.ai.nim.enabled=false"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerIntegrationTest {

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

    @BeforeEach
    void setUp() {
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
    }

    @Test
    void loginWithValidDemoCredentialsReturnsToken() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername(demoCredentials.getUsername());
        request.setPassword(demoCredentials.getPassword());

        String responseBody = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.expiresIn").exists())
                .andExpect(jsonPath("$.role").value(demoCredentials.getRole()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        LoginResponse response = objectMapper.readValue(responseBody, LoginResponse.class);
        assertThat(response.getAccessToken()).isNotBlank();
        assertThat(response.getExpiresIn()).isPositive();
        assertThat(response.getRole()).isEqualTo(Role.valueOf(demoCredentials.getRole()));
    }

    @Test
    void loginWithInvalidPasswordReturnsUnauthorized() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername(demoCredentials.getUsername());
        request.setPassword("wrong-password");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void loginWithNonExistentUserReturnsUnauthorized() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("nonexistent");
        request.setPassword("password");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void loginWithInactiveUserReturnsUnauthorized() throws Exception {
        // Given - create inactive user
        User inactiveUser = new User();
        inactiveUser.setId(UUID.randomUUID());
        inactiveUser.setName("Inactive User");
        inactiveUser.setUsername("inactive");
        inactiveUser.setPasswordHash(passwordEncoder.encode("password"));
        inactiveUser.setRole(Role.VIEWER);
        inactiveUser.setActive(false);
        userRepository.save(inactiveUser);

        LoginRequest request = new LoginRequest();
        request.setUsername("inactive");
        request.setPassword("password");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}