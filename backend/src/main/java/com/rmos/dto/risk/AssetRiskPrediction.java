package com.rmos.dto.risk;

import com.rmos.domain.risk.RiskBand;

public class AssetRiskPrediction {

    private String assetId;
    private Double riskScore;
    private Double healthScore;
    private RiskBand riskBand;
    private String modelVersion;
    private String explanation;
    private Double confidence;

    public String getAssetId() {
        return assetId;
    }

    public void setAssetId(String assetId) {
        this.assetId = assetId;
    }

    public Double getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(Double riskScore) {
        this.riskScore = riskScore;
    }

    public Double getHealthScore() {
        return healthScore;
    }

    public void setHealthScore(Double healthScore) {
        this.healthScore = healthScore;
    }

    public RiskBand getRiskBand() {
        return riskBand;
    }

    public void setRiskBand(RiskBand riskBand) {
        this.riskBand = riskBand;
    }

    public String getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }
}
