package com.rmos.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration for the Google Gemini chat provider.
 *
 * <p>Bound to {@code GEMINI_API_KEY} / {@code GEMINI_MODEL} through the
 * application configuration. The API key is never committed, never exposed to the
 * React application and never logged.
 *
 * <p>Google's Gemini API currently offers a free tier for the stable
 * {@code gemini-3.1-flash-lite} model. Quotas, availability and service terms are
 * controlled by Google - RMOS never enables billing and never falls back to a
 * paid provider.
 */
@Component
@ConfigurationProperties(prefix = "rmos.ai.gemini")
public class GeminiProperties {

    /** Google's public Generative Language API base URL. */
    private String baseUrl = "https://generativelanguage.googleapis.com";

    /** Gemini API version segment. */
    private String apiVersion = "v1beta";

    /** Stable free-tier model identifier. Override with GEMINI_MODEL. */
    private String model = "gemini-3.1-flash-lite";

    /**
     * API key supplied through GEMINI_API_KEY. Empty means "not configured",
     * which leaves the provider disabled and never produces a fabricated answer.
     */
    private String apiKey = "";

    /** Read/connect timeout for provider inference calls (seconds). */
    private int timeoutSeconds = 45;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    /** Values such as the documented placeholder must never be treated as usable. */
    public boolean hasUsableApiKey() {
        return apiKey != null && !apiKey.trim().isEmpty() && !isPlaceholder(apiKey.trim());
    }

    /** True when the configured key is an obvious documentation placeholder. */
    public static boolean isPlaceholder(String value) {
        if (value == null) {
            return false;
        }
        String normalized = value.trim().toUpperCase(java.util.Locale.ROOT);
        return normalized.isEmpty()
                || normalized.contains("PASTE_YOUR_REAL_KEY_HERE")
                || normalized.contains("PASTE_REAL_KEY_HERE")
                || normalized.contains("PASTE_KEY_HERE")
                || normalized.contains("PASTE_REAL_KEY")
                || normalized.contains("YOUR_API_KEY")
                || normalized.contains("YOUR_REAL_KEY")
                || normalized.contains("REPLACE_ME")
                || normalized.contains("PLACEHOLDER")
                || normalized.equals("CHANGEME")
                || normalized.equals("XXX")
                || normalized.equals("TODO");
    }
}