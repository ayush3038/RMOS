package com.rmos.config;

import com.rmos.domain.auth.Role;
import com.rmos.domain.auth.User;
import com.rmos.repository.auth.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Focused tests for the development-only demo account repair path.
 *
 * <p>Root cause covered here: a pre-existing local database can hold an
 * {@code admin} row whose BCrypt hash no longer matches the configured demo
 * password. The default (non-repair) behaviour deliberately preserves it, which
 * is what makes the documented demo login fail with
 * "Invalid username or password". The explicit repair switch fixes only that one
 * demo account, and never under the prod profile.
 */
@SpringBootTest(properties = {
        "rmos.ai.chat.provider=none",
        "rmos.ai.nim.enabled=false"
})
@ActiveProfiles("test")
@Transactional
class DemoAccountRepairTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private DemoCredentialsProperties demoCredentials;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        userRepository.flush();
        demoCredentials.setEnabled(true);
        demoCredentials.setRepairPassword(false);
    }

    @AfterEach
    void cleanUp() {
        userRepository.deleteAll();
        demoCredentials.setRepairPassword(false);
    }

    private DataInitializer initializerWithProfile(String activeProfile) {
        MockEnvironment environment = new MockEnvironment();
        if (activeProfile != null) {
            environment.setActiveProfiles(activeProfile);
        }
        return new DataInitializer(userRepository, passwordEncoder, demoCredentials, environment);
    }

    private User seedStaleDemoUser() {
        User user = new User();
        user.setName("RMOS Demo Administrator");
        user.setUsername(demoCredentials.getUsername());
        // A stale hash from an older configuration - not the configured demo password.
        user.setPasswordHash(passwordEncoder.encode("an-older-password"));
        user.setRole(Role.ADMIN);
        user.setActive(false);
        return userRepository.save(user);
    }

    @Test
    void staleDemoPasswordIsPreservedByDefault() {
        User seeded = seedStaleDemoUser();

        initializerWithProfile("dev").initDemoAccount();

        Optional<User> after = userRepository.findByUsername(demoCredentials.getUsername());
        assertThat(after).isPresent();
        // Preserved, not silently overwritten - this is the safe default.
        assertThat(passwordEncoder.matches("an-older-password", after.get().getPasswordHash())).isTrue();
        assertThat(passwordEncoder.matches(demoCredentials.getPassword(), after.get().getPasswordHash())).isFalse();
        assertThat(after.get().getId()).isEqualTo(seeded.getId());
    }

    @Test
    void repairSwitchResetsDemoAccountToConfiguredCredentials() {
        seedStaleDemoUser();
        demoCredentials.setRepairPassword(true);

        initializerWithProfile("dev").initDemoAccount();

        User repaired = userRepository.findByUsername(demoCredentials.getUsername()).orElseThrow();
        assertThat(passwordEncoder.matches(demoCredentials.getPassword(), repaired.getPasswordHash())).isTrue();
        // The account is re-activated so the documented demo login can succeed.
        assertThat(repaired.isActive()).isTrue();
        assertThat(repaired.getRole()).isEqualTo(Role.valueOf(demoCredentials.getRole()));
        assertThat(userRepository.count()).isEqualTo(1);
    }

    @Test
    void repairIsHardDisabledUnderProdProfile() {
        seedStaleDemoUser();
        demoCredentials.setRepairPassword(true);

        initializerWithProfile("prod").initDemoAccount();

        User untouched = userRepository.findByUsername(demoCredentials.getUsername()).orElseThrow();
        assertThat(passwordEncoder.matches("an-older-password", untouched.getPasswordHash())).isTrue();
        assertThat(untouched.isActive()).isFalse();
    }

    @Test
    void repairNeverTouchesOtherUsers() {
        User demoUser = seedStaleDemoUser();

        User other = new User();
        other.setName("Production Reviewer");
        other.setUsername("reviewer");
        other.setPasswordHash(passwordEncoder.encode("a-real-user-password"));
        other.setRole(Role.REVIEWER);
        other.setActive(true);
        userRepository.save(other);

        demoCredentials.setRepairPassword(true);
        initializerWithProfile("dev").initDemoAccount();

        User untouched = userRepository.findByUsername("reviewer").orElseThrow();
        assertThat(passwordEncoder.matches("a-real-user-password", untouched.getPasswordHash())).isTrue();

        User repaired = userRepository.findByUsername(demoCredentials.getUsername()).orElseThrow();
        assertThat(repaired.getId()).isEqualTo(demoUser.getId());
        assertThat(passwordEncoder.matches(demoCredentials.getPassword(), repaired.getPasswordHash())).isTrue();
    }

    @Test
    void healthyDemoAccountIsLeftCompletelyUnchanged() {
        initializerWithProfile("dev").initDemoAccount();
        User first = userRepository.findByUsername(demoCredentials.getUsername()).orElseThrow();
        String originalHash = first.getPasswordHash();

        demoCredentials.setRepairPassword(true);
        initializerWithProfile("dev").initDemoAccount();

        User second = userRepository.findByUsername(demoCredentials.getUsername()).orElseThrow();
        assertThat(second.getPasswordHash()).isEqualTo(originalHash);
        assertThat(userRepository.count()).isEqualTo(1);
    }
}