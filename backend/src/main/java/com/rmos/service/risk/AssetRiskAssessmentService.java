package com.rmos.service.risk;

import com.rmos.dto.risk.AssetRiskInput;
import com.rmos.dto.risk.AssetRiskPrediction;
import org.springframework.stereotype.Service;

@Service
public class AssetRiskAssessmentService {

    private final AssetRiskModel riskModel;

    // The interface isolates standard ML implementation specifics.
    // DeterministicAssetRiskModel is presently injected via Spring standard
    // components.
    public AssetRiskAssessmentService(AssetRiskModel riskModel) {
        this.riskModel = riskModel;
    }

    public AssetRiskPrediction assessRisk(AssetRiskInput input) {
        return riskModel.predict(input);
    }
}
