package com.rmos.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration for the RMOS AI assistant chat feature.
 *
 * Provider selection is a comma-separated preference chain (for example
 * "nim,ollama") that is tried in order at request time. All values can be
 * overridden through environment variables; no secret is ever hard-coded.
 */
@Component
@ConfigurationProperties(prefix = "rmos.ai.chat")
public class ChatProperties {

    /**
     * Comma separated provider chain, e.g. "gemini" or "gemini,ollama" or "none".
     *
     * <p>This in-code fallback must stay in step with {@code rmos.ai.chat.provider}
     * in application.yml / application-dev.yml / application-prod.yml. It used to
     * default to "nim,ollama", which meant that any context that did not load the
     * YAML (for example a profile without the property) silently selected Ollama as
     * the first configured provider - NVIDIA NIM is never configured while access
     * is blocked. Gemini is the intended default; Ollama stays opt-in via
     * "gemini,ollama".
     */
    private String provider = "gemini";

    /** Optional model override applied to the selected provider. */
    private String model = "";

    /** Optional API key override applied to providers that require one (NIM). */
    private String apiKey = "";

    /** Read timeout (seconds) for provider inference calls. */
    private int timeoutSeconds = 45;

    /** Hard cap for a single user message, in characters. */
    private int maxInputChars = 4000;

    /** Maximum number of prior conversation turns replayed to the model. */
    private int maxHistoryMessages = 12;

    /** Number of authorized document chunks retrieved for RAG context. */
    private int topK = 5;

    /** Sampling temperature for chat completions. */
    private double temperature = 0.2;

    private final Nim nim = new Nim();
    private final Ollama ollama = new Ollama();
    private final Gemini gemini = new Gemini();

    /** Google Gemini overrides. Free-tier model, no billing, no paid fallback. */
    public static class Gemini {
        /** Optional base URL override for Google's Generative Language API. */
        private String baseUrl = "";
        /** Optional model override, e.g. gemini-3.1-flash-lite (falls back to rmos.ai.gemini.model). */
        private String model = "";
        /** Optional API key override (falls back to GEMINI_API_KEY / rmos.ai.gemini.api-key). */
        private String apiKey = "";

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
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
    }

    public static class Nim {
        /** Optional override for the NIM base URL (falls back to rmos.ai.nim.base-url). */
        private String baseUrl = "";
        /** Optional override for the NIM chat model (falls back to rmos.ai.nim.model). */
        private String model = "";

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }
    }

    public static class Ollama {
        /** Optional base URL override for the local Ollama server (default http://localhost:11434). */
        private String baseUrl = "";
        /** Optional model tag override served by Ollama (default llama3.2). */
        private String model = "";

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
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

    public int getMaxInputChars() {
        return maxInputChars;
    }

    public void setMaxInputChars(int maxInputChars) {
        this.maxInputChars = maxInputChars;
    }

    public int getMaxHistoryMessages() {
        return maxHistoryMessages;
    }

    public void setMaxHistoryMessages(int maxHistoryMessages) {
        this.maxHistoryMessages = maxHistoryMessages;
    }

    public int getTopK() {
        return topK;
    }

    public void setTopK(int topK) {
        this.topK = topK;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public Nim getNim() {
        return nim;
    }

    public Ollama getOllama() {
        return ollama;
    }

    public Gemini getGemini() {
        return gemini;
    }
}
