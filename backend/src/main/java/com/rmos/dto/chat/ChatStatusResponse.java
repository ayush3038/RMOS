package com.rmos.dto.chat;

import java.util.ArrayList;
import java.util.List;

/**
 * Chat provider availability and setup guidance returned to the UI so the
 * assistant can show an honest availability indicator instead of pretending
 * a model is present.
 */
public class ChatStatusResponse {

    private boolean available;
    private String provider;
    private String model;
    private String message;
    private List<ChatProviderStatus> providers = new ArrayList<>();
    private List<String> setupInstructions = new ArrayList<>();
    private boolean retrievalConfigured;

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<ChatProviderStatus> getProviders() {
        return providers;
    }

    public void setProviders(List<ChatProviderStatus> providers) {
        this.providers = providers;
    }

    public List<String> getSetupInstructions() {
        return setupInstructions;
    }

    public void setSetupInstructions(List<String> setupInstructions) {
        this.setupInstructions = setupInstructions;
    }

    public boolean isRetrievalConfigured() {
        return retrievalConfigured;
    }

    public void setRetrievalConfigured(boolean retrievalConfigured) {
        this.retrievalConfigured = retrievalConfigured;
    }
}
