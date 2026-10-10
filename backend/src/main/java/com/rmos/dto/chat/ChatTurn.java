package com.rmos.dto.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * A single conversation turn exchanged with the RMOS AI assistant.
 * Used both for client supplied history and for provider payloads.
 */
public class ChatTurn {

    @NotBlank(message = "role must not be blank")
    @Pattern(regexp = "user|assistant", message = "role must be either 'user' or 'assistant'")
    private String role;

    @NotBlank(message = "content must not be blank")
    @Size(max = 4000, message = "content must not exceed 4000 characters")
    private String content;

    public ChatTurn() {
    }

    public ChatTurn(String role, String content) {
        this.role = role;
        this.content = content;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
