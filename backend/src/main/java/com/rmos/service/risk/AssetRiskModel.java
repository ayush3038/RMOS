package com.rmos.service.risk;

import com.rmos.dto.risk.AssetRiskInput;
import com.rmos.dto.risk.AssetRiskPrediction;

public interface AssetRiskModel {
    AssetRiskPrediction predict(AssetRiskInput input);
}
