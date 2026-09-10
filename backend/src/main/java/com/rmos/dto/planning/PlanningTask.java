package com.rmos.dto.planning;

import com.rmos.domain.planning.PlanningTaskStatus;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

public class PlanningTask {

    @NotNull(message = "Task ID must not be null")
    private Long taskId;

    @NotNull(message = "Asset ID must not be null")
    private Long assetId;

    @NotNull(message = "Priority score must not be null")
    @DecimalMin(value = "0.0", message = "Priority score must be between 0 and 1")
    @DecimalMax(value = "1.0", message = "Priority score must be between 0 and 1")
    private Double priorityScore;

    @NotBlank(message = "Department must not be blank")
    private String department;

    @NotNull(message = "Duration must not be null")
    @Positive(message = "Duration must be positive")
    private Integer durationMinutes;

    private LocalDateTime earliestStart;

    private LocalDateTime latestFinish;

    private PlanningTaskStatus status = PlanningTaskStatus.UNASSIGNED;

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Long getAssetId() {
        return assetId;
    }

    public void setAssetId(Long assetId) {
        this.assetId = assetId;
    }

    public Double getPriorityScore() {
        return priorityScore;
    }

    public void setPriorityScore(Double priorityScore) {
        this.priorityScore = priorityScore;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public LocalDateTime getEarliestStart() {
        return earliestStart;
    }

    public void setEarliestStart(LocalDateTime earliestStart) {
        this.earliestStart = earliestStart;
    }

    public LocalDateTime getLatestFinish() {
        return latestFinish;
    }

    public void setLatestFinish(LocalDateTime latestFinish) {
        this.latestFinish = latestFinish;
    }

    public PlanningTaskStatus getStatus() {
        return status;
    }

    public void setStatus(PlanningTaskStatus status) {
        this.status = status;
    }
}
