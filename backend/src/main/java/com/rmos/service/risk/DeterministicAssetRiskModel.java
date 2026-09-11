package com.rmos.service.risk;

import com.rmos.config.AssetRiskProperties;
import com.rmos.domain.risk.RiskBand;
import com.rmos.dto.risk.AssetRiskInput;
import com.rmos.dto.risk.AssetRiskPrediction;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DeterministicAssetRiskModel implements AssetRiskModel {

    private final AssetRiskProperties properties;

    public DeterministicAssetRiskModel(AssetRiskProperties properties) {
        this.properties = properties;
    }

    @Override
    public AssetRiskPrediction predict(AssetRiskInput input) {
        AssetRiskPrediction prediction = new AssetRiskPrediction();
        prediction.setAssetId(input.getAssetId());
        prediction.setModelVersion("DET-1.0-BASELINE");

        double overdueVal = input.getOverdueTaskCount() != null ? input.getOverdueTaskCount() : 0.0;
        double failVal = input.getFailureCount() != null ? input.getFailureCount() : 0.0;
        double eventVal = input.getMaintenanceEventCount() != null ? input.getMaintenanceEventCount() : 0.0;
        double recentFailVal = input.getRecentFailureCount() != null ? input.getRecentFailureCount() : 0.0;
        double recencyVal = input.getDaysSinceLastMaintenance() != null ? input.getDaysSinceLastMaintenance() : 0.0;
        double critVal = input.getCriticalMaintenanceCount() != null ? input.getCriticalMaintenanceCount() : 0.0;

        double overdueComponent = Math.min(overdueVal / Math.max(properties.getReferences().getOverdueTasks(), 1.0),
                1.0);
        double failureFraction = Math.min(failVal / Math.max(eventVal, 1.0), 1.0);
        double recentFailureComponent = Math
                .min(recentFailVal / Math.max(properties.getReferences().getRecentFailures(), 1.0), 1.0);
        double recencyComponent = Math.min(recencyVal / Math.max(properties.getReferences().getMaintenanceDays(), 1.0),
                1.0);
        double criticalComponent = Math
                .min(critVal / Math.max(properties.getReferences().getCriticalMaintenance(), 1.0), 1.0);

        double rawRisk = (overdueComponent * properties.getWeights().getOverdue()) +
                (failureFraction * properties.getWeights().getFailureHistory()) +
                (recentFailureComponent * properties.getWeights().getRecentFailure()) +
                (recencyComponent * properties.getWeights().getMaintenanceRecency()) +
                (criticalComponent * properties.getWeights().getCriticalMaintenance());

        double boundedRisk = Math.max(0.0, Math.min(rawRisk, 1.0));
        prediction.setRiskScore(boundedRisk);
        prediction.setHealthScore(Math.max(0.0, Math.min(1.0 - boundedRisk, 1.0)));
        prediction.setRiskBand(determineBand(boundedRisk));

        // Confidence: count non-null features vs total features expected
        int featuresProvided = countFeatures(input);
        int totalExpectedFeatures = 5;
        prediction.setConfidence((double) featuresProvided / totalExpectedFeatures);

        prediction.setExplanation(buildExplanation(input, overdueComponent, failureFraction, recentFailureComponent,
                recencyComponent, criticalComponent));

        return prediction;
    }

    private int countFeatures(AssetRiskInput in) {
        int c = 0;
        if (in.getOverdueTaskCount() != null)
            c++;
        if (in.getFailureCount() != null)
            c++;
        if (in.getRecentFailureCount() != null)
            c++;
        if (in.getDaysSinceLastMaintenance() != null)
            c++;
        if (in.getCriticalMaintenanceCount() != null)
            c++;
        return c;
    }

    private RiskBand determineBand(double risk) {
        if (risk >= properties.getThresholds().getVeryHigh())
            return RiskBand.VERY_HIGH;
        if (risk >= properties.getThresholds().getHigh())
            return RiskBand.HIGH;
        if (risk >= properties.getThresholds().getMedium())
            return RiskBand.MEDIUM;
        if (risk >= properties.getThresholds().getLow())
            return RiskBand.LOW;
        return RiskBand.VERY_LOW;
    }

    private String buildExplanation(AssetRiskInput in, double overdue, double fail, double rec, double recency,
            double crit) {
        List<String> reasons = new ArrayList<>();
        if (overdue > 0.5)
            reasons.add("overdue maintenance");
        if (rec > 0.5)
            reasons.add("recent failure history");
        if (fail > 0.5)
            reasons.add("high failure rates");
        if (recency > 0.5)
            reasons.add("maintenance recency");
        if (crit > 0.5)
            reasons.add("critical maintenance counts");

        if (reasons.isEmpty()) {
            return "Baseline normative risk levels.";
        } else {
            return "Risk elevated by " + String.join(" and ", reasons) + ".";
        }
    }
}
