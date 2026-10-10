package com.rmos.config;

import com.rmos.domain.auth.Role;
import com.rmos.domain.auth.User;
import com.rmos.repository.auth.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "rmos.ai.chat.provider=none",
        "rmos.ai.nim.enabled=false"
})
@ActiveProfiles("test")
@Transactional
class DataInitializerTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private DataInitializer dataInitializer;

    @Autowired
    private DemoCredentialsProperties demoCredentials;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        // Reset demo credentials to enabled for each test
        demoCredentials.setEnabled(true);
    }

    @AfterEach
    void cleanUp() {
        userRepository.deleteAll();
    }

    @Test
    void createsDemoAccountOnStartup() {
        // When
        dataInitializer.initDemoAccount();

        // Then
        Optional<User> user = userRepository.findByUsername(demoCredentials.getUsername());
        assertThat(user).isPresent();
        assertThat(user.get().getName()).isEqualTo("RMOS Demo Administrator");
        assertThat(user.get().getRole()).isEqualTo(Role.valueOf(demoCredentials.getRole()));
        assertThat(user.get().isActive()).isTrue();
        assertThat(passwordEncoder.matches(demoCredentials.getPassword(), user.get().getPasswordHash())).isTrue();
    }

    @Test
    void isIdempotentDoesNotCreateDuplicate() {
        // Given
        dataInitializer.initDemoAccount();
        long countAfterFirst = userRepository.count();

        // When
        dataInitializer.initDemoAccount();

        // Then
        assertThat(userRepository.count()).isEqualTo(countAfterFirst);
        Optional<User> user = userRepository.findByUsername(demoCredentials.getUsername());
        assertThat(user).isPresent();
        assertThat(user.get().getName()).isEqualTo("RMOS Demo Administrator");
    }

    @Test
    void preservesChangedPassword() {
        // Given
        dataInitializer.initDemoAccount();
        User user = userRepository.findByUsername(demoCredentials.getUsername()).orElseThrow();
        String changedPassword = "deliberately-changed-password";
        user.setPasswordHash(passwordEncoder.encode(changedPassword));
        userRepository.save(user);

        // When
        dataInitializer.initDemoAccount();

        // Then
        User updated = userRepository.findByUsername(demoCredentials.getUsername()).orElseThrow();
        assertThat(passwordEncoder.matches(changedPassword, updated.getPasswordHash())).isTrue();
        assertThat(passwordEncoder.matches(demoCredentials.getPassword(), updated.getPasswordHash())).isFalse();
    }

    @Test
    void respectsDisabledFlag() {
        // Given
        demoCredentials.setEnabled(false);
        userRepository.deleteAll(); // Ensure clean state

        // When
        dataInitializer.initDemoAccount();

        // Then
        assertThat(userRepository.findByUsername(demoCredentials.getUsername())).isEmpty();

        // Cleanup for other tests
        demoCredentials.setEnabled(true);
    }
}