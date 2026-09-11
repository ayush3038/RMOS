package com.rmos.service.ai;

import com.rmos.dto.ai.ExplanationResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class ExplanationResponseValidator {

    public void validate(ExplanationResponse response) {
        if (response == null) {
            throw new IllegalStateException("ExplanationResponse cannot be null");
        }
        if (!StringUtils.hasText(response.getTitle())) {
            throw new IllegalStateException("Response missing required 'title'");
        }
        if (!StringUtils.hasText(response.getSummary())) {
            throw new IllegalStateException("Response missing required 'summary'");
        }
        if (!StringUtils.hasText(response.getSourceType())) {
            throw new IllegalStateException("Response missing required 'sourceType'");
        }
        if (!StringUtils.hasText(response.getModel())) {
            throw new IllegalStateException("Response missing required 'model'");
        }
        if (response.getKeyPoints() == null) {
            throw new IllegalStateException("Response 'keyPoints' must not be null");
        }
        if (response.getWarnings() == null) {
            throw new IllegalStateException("Response 'warnings' must not be null");
        }
        if (response.getRecommendedActions() == null) {
            throw new IllegalStateException("Response 'recommendedActions' must not be null");
        }
        if (response.getConfidence() != null && (response.getConfidence() < 0.0 || response.getConfidence() > 1.0)) {
            throw new IllegalStateException("Confidence must be strictly between 0.0 and 1.0");
        }
        if (response.getRequiresHumanReview() == null) {
            throw new IllegalStateException("Response 'requiresHumanReview' must not be null");
        }
    }
}
