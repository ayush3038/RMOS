package com.rmos.dto;

import com.rmos.domain.MaintenanceTask;
import java.time.LocalDateTime;

public class MaintenanceTaskResponse {
    private Long id;
    private String taskCode;
    private String assetId;
    private String department;
    private String criticality;
    private String urgency;
    private LocalDateTime dueDate;
    private Integer durationMinutes;
    private String status;
    private String sourceSystem;

    public MaintenanceTaskResponse() {
    }

    public MaintenanceTaskResponse(Long id, String taskCode, String assetId, String department, String criticality,
            String urgency, LocalDateTime dueDate, Integer durationMinutes, String status, String sourceSystem) {
        this.id = id;
        this.taskCode = taskCode;
        this.assetId = assetId;
        this.department = department;
        this.criticality = criticality;
        this.urgency = urgency;
        this.dueDate = dueDate;
        this.durationMinutes = durationMinutes;
        this.status = status;
        this.sourceSystem = sourceSystem;
    }

    public static MaintenanceTaskResponse fromEntity(MaintenanceTask task) {
        return new MaintenanceTaskResponse(
                task.getId(),
                task.getTaskCode(),
                task.getAsset() != null ? task.getAsset().getAssetId() : null,
                task.getDepartment() != null ? task.getDepartment().name() : null,
                task.getCriticality() != null ? task.getCriticality().name() : null,
                task.getUrgency() != null ? task.getUrgency().name() : null,
                task.getDueDate(),
                task.getDurationMinutes(),
                task.getStatus() != null ? task.getStatus().name() : null,
                task.getSourceSystem());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTaskCode() {
        return taskCode;
    }

    public void setTaskCode(String taskCode) {
        this.taskCode = taskCode;
    }

    public String getAssetId() {
        return assetId;
    }

    public void setAssetId(String assetId) {
        this.assetId = assetId;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getCriticality() {
        return criticality;
    }

    public void setCriticality(String criticality) {
        this.criticality = criticality;
    }

    public String getUrgency() {
        return urgency;
    }

    public void setUrgency(String urgency) {
        this.urgency = urgency;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSourceSystem() {
        return sourceSystem;
    }

    public void setSourceSystem(String sourceSystem) {
        this.sourceSystem = sourceSystem;
    }
}
