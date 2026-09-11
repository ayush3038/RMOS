package com.rmos.dto.risk;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class AssetRiskInput {

    @NotBlank(message = "assetId is required")
    private String assetId;

    @NotBlank(message = "assetType is required")
    private String assetType;

    @NotNull
    @Min(value = 0, message = "maintenanceEventCount must be >= 0")
    private Integer maintenanceEventCount;

    @NotNull
    @Min(value = 0, message = "overdueTaskCount must be >= 0")
    private Integer overdueTaskCount;

    @NotNull
    @Min(value = 0, message = "failureCount must be >= 0")
    private Integer failureCount;

    @NotNull
    @Min(value = 0, message = "recentFailureCount must be >= 0")
    private Integer recentFailureCount;

    @NotNull
    @Min(value = 0, message = "recentMaintenanceCount must be >= 0")
    private Integer recentMaintenanceCount;

    @NotNull
    @Min(value = 0, message = "daysSinceLastMaintenance must be >= 0")
    private Integer daysSinceLastMaintenance;

    @NotNull
    @Min(value = 0, message = "criticalMaintenanceCount must be >= 0")
    private Integer criticalMaintenanceCount;

    // Getters and Setters

    public String getAssetId() {
        return assetId;
    }

    public void setAssetId(String assetId) {
        this.assetId = assetId;
    }

    public String getAssetType() {
        return assetType;
    }

    public void setAssetType(String assetType) {
        this.assetType = assetType;
    }

    public Integer getMaintenanceEventCount() {
        return maintenanceEventCount;
    }

    public void setMaintenanceEventCount(Integer maintenanceEventCount) {
        this.maintenanceEventCount = maintenanceEventCount;
    }

    public Integer getOverdueTaskCount() {
        return overdueTaskCount;
    }

    public void setOverdueTaskCount(Integer overdueTaskCount) {
        this.overdueTaskCount = overdueTaskCount;
    }

    public Integer getFailureCount() {
        return failureCount;
    }

    public void setFailureCount(Integer failureCount) {
        this.failureCount = failureCount;
    }

    public Integer getRecentFailureCount() {
        return recentFailureCount;
    }

    public void setRecentFailureCount(Integer recentFailureCount) {
        this.recentFailureCount = recentFailureCount;
    }

    public Integer getRecentMaintenanceCount() {
        return recentMaintenanceCount;
    }

    public void setRecentMaintenanceCount(Integer recentMaintenanceCount) {
        this.recentMaintenanceCount = recentMaintenanceCount;
    }

    public Integer getDaysSinceLastMaintenance() {
        return daysSinceLastMaintenance;
    }

    public void setDaysSinceLastMaintenance(Integer daysSinceLastMaintenance) {
        this.daysSinceLastMaintenance = daysSinceLastMaintenance;
    }

    public Integer getCriticalMaintenanceCount() {
        return criticalMaintenanceCount;
    }

    public void setCriticalMaintenanceCount(Integer criticalMaintenanceCount) {
        this.criticalMaintenanceCount = criticalMaintenanceCount;
    }
}
