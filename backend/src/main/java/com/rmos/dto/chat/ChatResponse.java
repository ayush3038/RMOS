package com.rmos.dto.chat;

import java.util.ArrayList;
import java.util.List;

/**
 * Assistant reply returned to the chat client.
 *
 * The reply is AI generated text only: it never mutates schedules, blocks or
 * permissions, and always carries a human-review disclaimer.
 */
public class ChatResponse {

    private String reply;
    private String provider;
    private String model;
    private int retrievedContextCount;
    private List<ChatSource> sources = new ArrayList<>();
    private boolean systemDataIncluded;
    private String generatedAt;
    private boolean requiresHumanReview = true;
    private String disclaimer;

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
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

    public int getRetrievedContextCount() {
        return retrievedContextCount;
    }

    public void setRetrievedContextCount(int retrievedContextCount) {
        this.retrievedContextCount = retrievedContextCount;
    }

    public List<ChatSource> getSources() {
        return sources;
    }

    public void setSources(List<ChatSource> sources) {
        this.sources = sources;
    }

    public boolean isSystemDataIncluded() {
        return systemDataIncluded;
    }

    public void setSystemDataIncluded(boolean systemDataIncluded) {
        this.systemDataIncluded = systemDataIncluded;
    }

    public String getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(String generatedAt) {
        this.generatedAt = generatedAt;
    }

    public boolean isRequiresHumanReview() {
        return requiresHumanReview;
    }

    public void setRequiresHumanReview(boolean requiresHumanReview) {
        this.requiresHumanReview = requiresHumanReview;
    }

    public String getDisclaimer() {
        return disclaimer;
    }

    public void setDisclaimer(String disclaimer) {
        this.disclaimer = disclaimer;
    }
}
