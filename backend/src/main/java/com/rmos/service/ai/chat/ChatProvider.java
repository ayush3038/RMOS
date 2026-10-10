package com.rmos.service.ai.chat;

import com.rmos.dto.chat.ChatTurn;

import java.util.List;

/**
 * Provider abstraction for chat completions.
 *
 * The configured model can be swapped (NVIDIA NIM hosted, a local NIM
 * container, Ollama, or any other OpenAI-compatible endpoint) without any
 * change to the chat service, controller or UI.
 */
public interface ChatProvider {

    /** Stable identifier used in configuration, e.g. "gemini" or "ollama". */
    String type();

    /**
     * Resolved model identifier for this provider, e.g.
     * {@code gemini-3.1-flash-lite} or {@code llama3.2}.
     *
     * <p>Reported by {@code GET /api/v1/chat/status} so the UI can show the
     * exact model in use instead of parsing it back out of {@link #describe()}.
     * Never contains a key, token or any other secret.
     */
    String model();

    /** True when this provider has enough configuration to be attempted. */
    boolean isConfigured();

    /** Human readable description for status reporting. Never contains secrets. */
    String describe();

    /**
     * Executes a chat completion request.
     *
     * @param messages conversation turns beginning with the system prompt
     * @return the model generated assistant text plus provenance metadata
     * @throws ChatProviderException when the provider fails or is unavailable
     */
    ChatCompletion complete(List<ChatTurn> messages);
}
