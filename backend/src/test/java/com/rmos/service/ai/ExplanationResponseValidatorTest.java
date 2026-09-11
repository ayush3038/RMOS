package com.rmos.service.ai;

import com.rmos.dto.ai.ExplanationResponse;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class ExplanationResponseValidatorTest {

    private final ExplanationResponseValidator validator = new ExplanationResponseValidator();

    private ExplanationResponse getValidResponse() {
        ExplanationResponse r = new ExplanationResponse();
        r.setTitle("A");
        r.setSummary("B");
        r.setSourceType("SAFETY_VALIDATION");
        r.setModel("M1");
        r.setKeyPoints(Collections.emptyList());
        r.setWarnings(Collections.emptyList());
        r.setRecommendedActions(Collections.emptyList());
        r.setRequiresHumanReview(true);
        r.setConfidence(0.9);
        return r;
    }

    @Test
    void testValid() {
        assertDoesNotThrow(() -> validator.validate(getValidResponse()));
    }

    @Test
    void testMissingTitleThrows() {
        ExplanationResponse r = getValidResponse();
        r.setTitle("");
        assertThrows(IllegalStateException.class, () -> validator.validate(r));
    }

    @Test
    void testNullArraysThrows() {
        ExplanationResponse r = getValidResponse();
        r.setKeyPoints(null);
        assertThrows(IllegalStateException.class, () -> validator.validate(r));
    }

    @Test
    void testInvalidConfidenceThrows() {
        ExplanationResponse r = getValidResponse();
        r.setConfidence(-0.1);
        assertThrows(IllegalStateException.class, () -> validator.validate(r));

        r.setConfidence(1.1);
        assertThrows(IllegalStateException.class, () -> validator.validate(r));
    }
}
