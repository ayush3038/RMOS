package com.rmos.dto.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Incoming chat message from the RMOS assistant UI.
 * Conversation history is replayed by the client; nothing is persisted.
 */
public class ChatRequest {

    @NotBlank(message = "message must not be blank")
    @Size(max = 4000, message = "message must not exceed 4000 characters")
    private String message;

    @Size(max = 20, message = "history must not exceed 20 messages")
    private List<ChatTurn> history;

    public ChatRequest() {
    }

    public ChatRequest(String message, List<ChatTurn> history) {
        this.message = message;
        this.history = history;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<ChatTurn> getHistory() {
        return history;
    }

    public void setHistory(List<ChatTurn> history) {
        this.history = history;
    }
}
