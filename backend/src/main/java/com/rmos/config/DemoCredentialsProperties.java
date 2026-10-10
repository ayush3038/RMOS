package com.rmos.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration for demo account credentials.
 * These are intentionally public credentials for the RMOS demo environment.
 * The seeded account is restricted to demo/simulation data access only.
 */
@Component
@ConfigurationProperties(prefix = "rmos.demo")
public class DemoCredentialsProperties {

    /**
     * Username for the pre-created demo admin account.
     * Default: "admin"
     */
    private String username = "admin";

    /**
     * Password for the pre-created demo admin account.
     * Default: "demo123"
     * Should be overridden via RMOS_DEMO_PASSWORD environment variable in production.
     */
    private String password = "demo123";

    /**
     * Role assigned to the demo account.
     * Default: "ADMIN"
     */
    private String role = "ADMIN";

    /**
     * Whether to enable demo account seeding on startup.
     * Default: true
     */
    private boolean enabled = true;

    /**
     * Development-only repair switch.
     *
     * <p>When true, and the active Spring profile is <em>not</em> {@code prod},
     * the demo account is repaired on startup if its stored BCrypt hash no longer
     * matches the configured password, or if the account was deactivated.
     *
     * <p>This exists because a pre-existing local database may contain an
     * {@code admin} row seeded with an older password, which makes the documented
     * demo login fail with "Invalid username or password".
     *
     * <p>Safety: it only ever touches the single configured demo account, never
     * other users, and it is hard-disabled under the {@code prod} profile so a
     * production database is never silently rewritten.
     *
     * <p>Default: false (opt-in via {@code RMOS_DEMO_REPAIR_PASSWORD=true}).
     */
    private boolean repairPassword = false;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isRepairPassword() {
        return repairPassword;
    }

    public void setRepairPassword(boolean repairPassword) {
        this.repairPassword = repairPassword;
    }
}