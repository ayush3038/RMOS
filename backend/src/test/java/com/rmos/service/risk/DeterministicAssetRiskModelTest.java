package com.rmos.service.risk;

import com.rmos.config.AssetRiskProperties;
import com.rmos.domain.risk.RiskBand;
import com.rmos.dto.risk.AssetRiskInput;
import com.rmos.dto.risk.AssetRiskPrediction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeterministicAssetRiskModelTest {

    private DeterministicAssetRiskModel model;
    private AssetRiskProperties properties;

    @BeforeEach
    void setUp() {
        properties = new AssetRiskProperties();
        model = new DeterministicAssetRiskModel(properties);
    }

    private AssetRiskInput createZeroInput() {
        AssetRiskInput in = new AssetRiskInput();
        in.setAssetId("A1");
        in.setAssetType("SIGNAL");
        in.setMaintenanceEventCount(0);
        in.setOverdueTaskCount(0);
        in.setFailureCount(0);
        in.setRecentFailureCount(0);
        in.setRecentMaintenanceCount(0);
        in.setDaysSinceLastMaintenance(0);
        in.setCriticalMaintenanceCount(0);
        return in;
    }

    @Test
    void predict_AllZero_VeryLowRisk() {
        AssetRiskInput input = createZeroInput();
        AssetRiskPrediction pred = model.predict(input);

        assertEquals(0.0, pred.getRiskScore(), 0.01);
        assertEquals(1.0, pred.getHealthScore(), 0.01);
        assertEquals(RiskBand.VERY_LOW, pred.getRiskBand());
        assertEquals(1.0, pred.getConfidence(), 0.01);
    }

    @Test
    void testDivisionByZeroHandledSafely() {
        AssetRiskInput input = createZeroInput();
        input.setFailureCount(2); // but event count is 0
        AssetRiskPrediction pred = model.predict(input);

        // Capped at failureCount / max(0, 1) = 2/1 = 2 -> capped to 1.0 block
        assertTrue(pred.getRiskScore() > 0.0);
    }

    @Test
    void testHighOverdueIncreasesRisk() {
        AssetRiskInput in1 = createZeroInput();
        AssetRiskPrediction p1 = model.predict(in1);

        AssetRiskInput in2 = createZeroInput();
        in2.setOverdueTaskCount(10);
        AssetRiskPrediction p2 = model.predict(in2);

        assertTrue(p2.getRiskScore() > p1.getRiskScore());
    }

    @Test
    void testRiskAndHealthClamped() {
        AssetRiskInput in = createZeroInput();
        in.setOverdueTaskCount(100);
        in.setFailureCount(100);
        in.setRecentFailureCount(100);
        in.setDaysSinceLastMaintenance(1000);
        in.setCriticalMaintenanceCount(100);

        AssetRiskPrediction pred = model.predict(in);
        assertEquals(1.0, pred.getRiskScore(), 0.01);
        assertEquals(0.0, pred.getHealthScore(), 0.01);
        assertEquals(RiskBand.VERY_HIGH, pred.getRiskBand());
    }

    @Test
    void testConfidenceWithMissingFeatures() {
        AssetRiskInput in = new AssetRiskInput();
        in.setAssetId("A2");
        in.setAssetType("TRACK");
        in.setOverdueTaskCount(1); // 1 feature present out of 5 required for risk calculation

        AssetRiskPrediction pred = model.predict(in);
        assertEquals(0.2, pred.getConfidence(), 0.01); // 1/5 = 0.2
    }
}
