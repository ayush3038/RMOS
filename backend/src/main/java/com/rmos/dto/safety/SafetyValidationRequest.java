package com.rmos.dto.safety;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class SafetyValidationRequest {

    private Long maintenanceTaskId;
    private Long assetId;
    private Integer requestedDurationMinutes;
    private String department;
    private LocalDateTime dataTimestamp;

    public Long getMaintenanceTaskId() {
        return maintenanceTaskId;
    }

    public void setMaintenanceTaskId(Long maintenanceTaskId) {
        this.maintenanceTaskId = maintenanceTaskId;
    }

    public Long getAssetId() {
        return assetId;
    }

    public void setAssetId(Long assetId) {
        this.assetId = assetId;
    }

    public Integer getRequestedDurationMinutes() {
        return requestedDurationMinutes;
    }

    public void setRequestedDurationMinutes(Integer requestedDurationMinutes) {
        this.requestedDurationMinutes = requestedDurationMinutes;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public LocalDateTime getDataTimestamp() {
        return dataTimestamp;
    }

    public void setDataTimestamp(LocalDateTime dataTimestamp) {
        this.dataTimestamp = dataTimestamp;
    }
}
