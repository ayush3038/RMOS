package com.rmos.dto.chat;

/**
 * Availability report for a single configured chat provider.
 */
public class ChatProviderStatus {

    private String type;
    private boolean configured;
    private String description;

    public ChatProviderStatus() {
    }

    public ChatProviderStatus(String type, boolean configured, String description) {
        this.type = type;
        this.configured = configured;
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isConfigured() {
        return configured;
    }

    public void setConfigured(boolean configured) {
        this.configured = configured;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
