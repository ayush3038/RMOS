package com.rmos.service.ai.chat;

/**
 * Raised when a chat provider cannot serve a request (network failure,
 * authentication failure, rate limit, malformed response, ...).
 * Messages must never contain credentials.
 */
public class ChatProviderException extends RuntimeException {

    private final String providerType;

    public ChatProviderException(String providerType, String message) {
        super(message);
        this.providerType = providerType;
    }

    public ChatProviderException(String providerType, String message, Throwable cause) {
        super(message, cause);
        this.providerType = providerType;
    }

    public String getProviderType() {
        return providerType;
    }
}
