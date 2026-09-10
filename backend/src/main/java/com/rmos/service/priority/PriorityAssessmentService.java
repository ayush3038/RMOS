package com.rmos.service.priority;

import com.rmos.domain.priority.ImpactLevel;
import com.rmos.domain.priority.PriorityBand;
import com.rmos.domain.priority.PriorityFactor;
import com.rmos.domain.priority.SafetyLevel;
import com.rmos.domain.priority.WorkforceLevel;
import com.rmos.dto.priority.PriorityAssessment;
import com.rmos.dto.priority.PriorityAssessmentRequest;
import com.rmos.dto.priority.PriorityFactorContribution;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PriorityAssessmentService {

    private final double safetyWeight;
    private final double trainImpactWeight;
    private final double criticalityWeight;
    private final double urgencyWeight;
    private final double durationWeight;
    private final double workforceWeight;

    private final double criticalThreshold;
    private final double highThreshold;
    private final double mediumThreshold;

    public PriorityAssessmentService(
            @Value("${rmos.priority.weights.safety:0.30}") double safetyWeight,
            @Value("${rmos.priority.weights.train-impact:0.25}") double trainImpactWeight,
            @Value("${rmos.priority.weights.criticality:0.20}") double criticalityWeight,
            @Value("${rmos.priority.weights.urgency:0.15}") double urgencyWeight,
            @Value("${rmos.priority.weights.duration:0.05}") double durationWeight,
            @Value("${rmos.priority.weights.workforce:0.05}") double workforceWeight,
            @Value("${rmos.priority.thresholds.critical:0.80}") double criticalThreshold,
            @Value("${rmos.priority.thresholds.high:0.60}") double highThreshold,
            @Value("${rmos.priority.thresholds.medium:0.35}") double mediumThreshold) {
        this.safetyWeight = safetyWeight;
        this.trainImpactWeight = trainImpactWeight;
        this.criticalityWeight = criticalityWeight;
        this.urgencyWeight = urgencyWeight;
        this.durationWeight = durationWeight;
        this.workforceWeight = workforceWeight;
        this.criticalThreshold = criticalThreshold;
        this.highThreshold = highThreshold;
        this.mediumThreshold = mediumThreshold;
    }

    public PriorityAssessment assessPriority(PriorityAssessmentRequest request) {
        if (request.getDurationMinutes() == null || request.getDurationMinutes() <= 0) {
            throw new IllegalArgumentException("Duration must be strictly positive");
        }

        PriorityAssessment assessment = new PriorityAssessment();
        assessment.setTaskId(request.getTaskId());

        double totalScore = 0.0;

        // 1. Safety Factor
        double safetyVal = normalizeSafety(request.getSafetyLevel());
        totalScore += addContribution(assessment, PriorityFactor.SAFETY, safetyVal, safetyWeight,
                "Safety critical assessment. Rated as " + request.getSafetyLevel().name());

        // 2. Train Impact Factor
        double impactVal = normalizeImpact(request.getTrainImpactLevel());
        totalScore += addContribution(assessment, PriorityFactor.TRAIN_IMPACT, impactVal, trainImpactWeight,
                "Operational train impact. Rated as " + request.getTrainImpactLevel().name());

        // 3. Criticality Factor
        double critVal = normalizeImpact(request.getCriticality());
        totalScore += addContribution(assessment, PriorityFactor.CRITICALITY, critVal, criticalityWeight,
                "Asset criticality to infrastructure. Rated as " + request.getCriticality().name());

        // 4. Urgency Factor
        double urgVal = normalizeImpact(request.getUrgency());
        totalScore += addContribution(assessment, PriorityFactor.URGENCY, urgVal, urgencyWeight,
                "Task operational urgency. Rated as " + request.getUrgency().name());

        // 5. Duration Factor (normalized scaling)
        double durVal = normalizeDuration(request.getDurationMinutes());
        totalScore += addContribution(assessment, PriorityFactor.DURATION, durVal, durationWeight,
                "Duration assessment based on " + request.getDurationMinutes() + " minutes block");

        // 6. Workforce Factor
        double workVal = normalizeWorkforce(request.getWorkforceRequired());
        totalScore += addContribution(assessment, PriorityFactor.WORKFORCE, workVal, workforceWeight,
                "Resource footprint. Rated as " + request.getWorkforceRequired().name());

        assessment.setPriorityScore(totalScore);
        assessment.setPriorityBand(determineBand(totalScore));

        return assessment;
    }

    private double addContribution(PriorityAssessment assessment, PriorityFactor factor, double value, double weight,
            String explanation) {
        double contribution = value * weight;
        PriorityFactorContribution pfc = new PriorityFactorContribution();
        pfc.setFactor(factor);
        pfc.setValue(value);
        pfc.setWeight(weight);
        pfc.setContribution(contribution);
        pfc.setExplanation(explanation);
        assessment.getFactorBreakdown().add(pfc);
        return contribution;
    }

    private double normalizeSafety(SafetyLevel safetyLevel) {
        return switch (safetyLevel) {
            case CRITICAL -> 1.0;
            case HIGH -> 0.75;
            case MEDIUM -> 0.5;
            case LOW -> 0.25;
            default -> 0.0;
        };
    }

    private double normalizeImpact(ImpactLevel impactLevel) {
        return switch (impactLevel) {
            case CRITICAL -> 1.0;
            case HIGH -> 0.75;
            case MEDIUM -> 0.5;
            case LOW -> 0.25;
            case NONE -> 0.0;
            default -> 0.0;
        };
    }

    private double normalizeWorkforce(WorkforceLevel workforceLevel) {
        return switch (workforceLevel) {
            case HIGH -> 1.0;
            case MEDIUM -> 0.66;
            case LOW -> 0.33;
            case NONE -> 0.0;
            default -> 0.0;
        };
    }

    private double normalizeDuration(int minutes) {
        // Deterministic scaling map. Let's establish that 240 mins (4 hours) reaches
        // peak value of 1.0
        // Blocks larger than 240 get a full 1.0 multiplier.
        double maxMins = 240.0;
        return Math.min(minutes / maxMins, 1.0);
    }

    private PriorityBand determineBand(double score) {
        if (score >= criticalThreshold) {
            return PriorityBand.CRITICAL;
        } else if (score >= highThreshold) {
            return PriorityBand.HIGH;
        } else if (score >= mediumThreshold) {
            return PriorityBand.MEDIUM;
        } else {
            return PriorityBand.LOW;
        }
    }
}
