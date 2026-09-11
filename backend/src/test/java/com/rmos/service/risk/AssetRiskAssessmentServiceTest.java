package com.rmos.service.risk;

import com.rmos.dto.risk.AssetRiskInput;
import com.rmos.dto.risk.AssetRiskPrediction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AssetRiskAssessmentServiceTest {

    private AssetRiskModel model;
    private AssetRiskAssessmentService service;

    @BeforeEach
    void setUp() {
        model = mock(AssetRiskModel.class);
        service = new AssetRiskAssessmentService(model);
    }

    @Test
    void testAssessRiskInvokesModel() {
        AssetRiskInput input = new AssetRiskInput();
        input.setAssetId("A1");

        AssetRiskPrediction prediction = new AssetRiskPrediction();
        prediction.setAssetId("A1");
        prediction.setRiskScore(0.5);

        when(model.predict(any(AssetRiskInput.class))).thenReturn(prediction);

        AssetRiskPrediction result = service.assessRisk(input);

        assertEquals("A1", result.getAssetId());
        assertEquals(0.5, result.getRiskScore(), 0.01);
    }
}
