package com.rmos.dto.ai;

import com.rmos.domain.ai.ExplanationAudience;
import com.rmos.domain.ai.ExplanationContextType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

public class ExplanationRequest {

    @NotNull(message = "contextType is required")
    private ExplanationContextType contextType;

    @NotNull(message = "context is required")
    private Map<String, Object> context; // Using map to allow struct injection without tying to raw string JSON parsing
                                         // initially, simplifies spring binding

    @NotNull(message = "audience is required")
    private ExplanationAudience audience;

    private String language = "en";

    public ExplanationContextType getContextType() {
        return contextType;
    }

    public void setContextType(ExplanationContextType contextType) {
        this.contextType = contextType;
    }

    public Map<String, Object> getContext() {
        return context;
    }

    public void setContext(Map<String, Object> context) {
        this.context = context;
    }

    public ExplanationAudience getAudience() {
        return audience;
    }

    public void setAudience(ExplanationAudience audience) {
        this.audience = audience;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}
