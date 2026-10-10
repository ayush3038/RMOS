package com.rmos.service.ai.chat;

/**
 * Result of a successful provider chat completion call.
 */
public class ChatCompletion {

    private final String content;
    private final String model;

    public ChatCompletion(String content, String model) {
        this.content = content;
        this.model = model;
    }

    public String getContent() {
        return content;
    }

    public String getModel() {
        return model;
    }
}
