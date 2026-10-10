package com.rmos.config;

import com.rmos.domain.auth.Role;
import com.rmos.domain.auth.User;
import com.rmos.repository.auth.UserRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

/**
 * Initializes the demo account on application startup.
 * <p>
 * Default behaviour is idempotent and non-destructive: it never creates duplicate
 * users and never overwrites a deliberately changed password.
 * <p>
 * Because a local database can predate the current defaults (an {@code admin} row
 * seeded with an older password, or a deactivated demo account), the documented
 * demo login then fails with "Invalid username or password" even though the login
 * page shows valid credentials. {@link DemoCredentialsProperties#isRepairPassword()}
 * enables an explicit, development-only repair of <em>that single demo account</em>.
 * <p>
 * Repair is hard-disabled under the {@code prod} profile so production
 * initialisation stays safe and idempotent. Production user passwords are never
 * overwritten: only the configured demo username is ever eligible.
 * <p>
 * Logging is diagnostic only - it never records passwords, hashes or tokens.
 */
@Component
public class DataInitializer {

        private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

        private final UserRepository userRepository;
        private final PasswordEncoder passwordEncoder;
        private final DemoCredentialsProperties demoCredentials;
        private final Environment environment;

        public DataInitializer(UserRepository userRepository,
                        PasswordEncoder passwordEncoder,
                        DemoCredentialsProperties demoCredentials,
                        Environment environment) {
                this.userRepository = userRepository;
                this.passwordEncoder = passwordEncoder;
                this.demoCredentials = demoCredentials;
                this.environment = environment;
        }

        @PostConstruct
        @Transactional
        public void initDemoAccount() {
                if (!demoCredentials.isEnabled()) {
                        logger.info("Demo account seeding is disabled (rmos.demo.enabled=false)");
                        return;
                }

                String username = demoCredentials.getUsername();
                String rawPassword = demoCredentials.getPassword();
                Role role;

                try {
                        role = Role.valueOf(demoCredentials.getRole().toUpperCase());
                } catch (IllegalArgumentException e) {
                        logger.warn("Invalid demo role '{}', defaulting to ADMIN", demoCredentials.getRole());
                        role = Role.ADMIN;
                }

                if (!StringUtils.hasText(username) || !StringUtils.hasText(rawPassword)) {
                        logger.warn("Demo account seeding skipped: configured username/password is blank");
                        return;
                }

                Optional<User> existingUser = userRepository.findByUsername(username);

                if (existingUser.isPresent()) {
                        reconcileExistingDemoAccount(existingUser.get(), username, rawPassword, role);
                        return;
                }

                createDemoAccount(username, rawPassword, role);
        }

        private void createDemoAccount(String username, String rawPassword, Role role) {
                User demoUser = new User();
                demoUser.setId(UUID.randomUUID());
                demoUser.setName("RMOS Demo Administrator");
                demoUser.setUsername(username);
                demoUser.setPasswordHash(passwordEncoder.encode(rawPassword));
                demoUser.setRole(role);
                demoUser.setActive(true);

                userRepository.save(demoUser);
                logger.info("Created demo account: username='{}', role={}, active=true, id={}",
                        username, role, demoUser.getId());
        }

        /**
         * Existing demo account: verify it can actually authenticate, then either
         * leave it untouched (default) or repair it when the explicit
         * development-only repair switch is enabled outside of production.
         */
        private void reconcileExistingDemoAccount(User user, String username, String rawPassword, Role role) {
                boolean passwordMatches = passwordEncoder.matches(rawPassword, user.getPasswordHash());

                logger.info("Demo account '{}' already exists (id={}, role={}, active={}, passwordMatchesConfiguredDefault={})",
                        username, user.getId(), user.getRole(), user.isActive(), passwordMatches);

                if (passwordMatches && user.isActive()) {
                        logger.debug("Demo account '{}' is usable with the configured credentials; nothing to do", username);
                        return;
                }

                if (!repairAllowed()) {
                        logger.warn("Demo account '{}' needs repair (passwordMatchesConfiguredDefault={}, active={}) but "
                                        + "repair is disabled. The documented demo login will fail. Enable the explicit "
                                        + "development-only repair with RMOS_DEMO_REPAIR_PASSWORD=true (never used under "
                                        + "the prod profile), or set RMOS_DEMO_PASSWORD to the existing password.",
                                username, passwordMatches, user.isActive());
                        return;
                }

                boolean changed = false;
                if (!passwordMatches) {
                        user.setPasswordHash(passwordEncoder.encode(rawPassword));
                        changed = true;
                }
                if (!user.isActive()) {
                        user.setActive(true);
                        changed = true;
                }
                if (user.getRole() != role) {
                        user.setRole(role);
                        changed = true;
                }
                userRepository.save(user);
                logger.warn("Repaired development demo account '{}' (id={}): password reset to the configured default, "
                                + "account re-activated, role aligned to {}. No other user was modified.",
                        username, user.getId(), role);
        }

        /** Repair only when explicitly requested and never under the prod profile. */
        private boolean repairAllowed() {
                if (!demoCredentials.isRepairPassword()) {
                        return false;
                }
                if (isProductionProfileActive()) {
                        logger.error("RMOS_DEMO_REPAIR_PASSWORD is set but the 'prod' profile is active; "
                                        + "refusing to repair the demo account. Production initialisation stays read-only.");
                        return false;
                }
                return true;
        }

        private boolean isProductionProfileActive() {
                if (environment == null) {
                        return false;
                }
                for (String profile : environment.getActiveProfiles()) {
                        if ("prod".equalsIgnoreCase(profile) || "production".equalsIgnoreCase(profile)) {
                                return true;
                        }
                }
                // Fall back to the conventional property when no profile object is available.
                String configured = environment.getProperty("spring.profiles.active", "");
                return Arrays.stream(configured.split(","))
                                .map(String::trim)
                                .anyMatch(p -> "prod".equalsIgnoreCase(p) || "production".equalsIgnoreCase(p));
        }

        /** Minimal local helper to avoid an extra import for a blank check. */
        private static final class StringUtils {
                private StringUtils() {
                }

                static boolean hasText(String value) {
                        return value != null && !value.trim().isEmpty();
                }
        }
}