package com.rmos.service.priority;

import com.rmos.domain.priority.ImpactLevel;
import com.rmos.domain.priority.PriorityBand;
import com.rmos.domain.priority.SafetyLevel;
import com.rmos.domain.priority.WorkforceLevel;
import com.rmos.dto.priority.PriorityAssessment;
import com.rmos.dto.priority.PriorityAssessmentRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PriorityAssessmentServiceTest {

    private PriorityAssessmentService service;

    @BeforeEach
    void setUp() {
        // Init with default weights and thresholds specified
        service = new PriorityAssessmentService(0.30, 0.25, 0.20, 0.15, 0.05, 0.05, 0.80, 0.60, 0.35);
    }

    private PriorityAssessmentRequest createBaseRequest() {
        PriorityAssessmentRequest r = new PriorityAssessmentRequest();
        r.setTaskId(1L);
        r.setSafetyLevel(SafetyLevel.LOW);
        r.setTrainImpactLevel(ImpactLevel.LOW);
        r.setCriticality(ImpactLevel.LOW);
        r.setUrgency(ImpactLevel.LOW);
        r.setDurationMinutes(60);
        r.setWorkforceRequired(WorkforceLevel.LOW);
        return r;
    }

    @Test
    void testHighestPrioritySafetyInputYieldsHighestContribution() {
        PriorityAssessmentRequest req1 = createBaseRequest();
        req1.setSafetyLevel(SafetyLevel.CRITICAL);

        PriorityAssessmentRequest req2 = createBaseRequest();
        req2.setTrainImpactLevel(ImpactLevel.CRITICAL); // Highest in rank #2

        PriorityAssessment res1 = service.assessPriority(req1);
        PriorityAssessment res2 = service.assessPriority(req2);

        assertTrue(res1.getPriorityScore() > res2.getPriorityScore(),
                "Safety should outscore train impact when both are maxed");
    }

    @Test
    void testNegativeDurationThrowsException() {
        PriorityAssessmentRequest req = createBaseRequest();
        req.setDurationMinutes(-10);

        assertThrows(IllegalArgumentException.class, () -> service.assessPriority(req));
    }

    @Test
    void testInvalidDurationThrowsException() {
        PriorityAssessmentRequest req = createBaseRequest();
        req.setDurationMinutes(0);

        assertThrows(IllegalArgumentException.class, () -> service.assessPriority(req));
    }

    @Test
    void testBreakdownsSumToFinalScore() {
        PriorityAssessmentRequest req = createBaseRequest();
        req.setSafetyLevel(SafetyLevel.CRITICAL);
        req.setCriticality(ImpactLevel.HIGH);
        req.setDurationMinutes(240); // Max normalized duration modifier

        PriorityAssessment res = service.assessPriority(req);

        double sum = res.getFactorBreakdown().stream()
                .mapToDouble(b -> b.getContribution())
                .sum();

        // Account for floating point drift during assertions
        assertEquals(res.getPriorityScore(), sum, 0.0001);
    }

    @Test
    void testDeterministicRepeatedOutputs() {
        PriorityAssessmentRequest req = createBaseRequest();
        req.setSafetyLevel(SafetyLevel.HIGH);

        PriorityAssessment res1 = service.assessPriority(req);
        PriorityAssessment res2 = service.assessPriority(req);

        assertEquals(res1.getPriorityScore(), res2.getPriorityScore());
        assertEquals(res1.getPriorityBand(), res2.getPriorityBand());
    }

    @Test
    void testScoreMappedToPriorityBandAtThreshold() {
        // Custom instance with known threshold exactly achievable
        // Just checking threshold classification
        PriorityAssessmentService thresholdService = new PriorityAssessmentService(0.5, 0.5, 0, 0, 0, 0, 1.0, 0.70,
                0.35);

        PriorityAssessmentRequest req = createBaseRequest();
        req.setSafetyLevel(SafetyLevel.CRITICAL); // 1.0 * 0.5 = 0.5
        req.setTrainImpactLevel(ImpactLevel.MEDIUM); // 0.5 * 0.5 = 0.25 (Total: 0.75 -> HIGH)

        PriorityAssessment res = thresholdService.assessPriority(req);

        assertEquals(PriorityBand.HIGH, res.getPriorityBand());
    }
}
