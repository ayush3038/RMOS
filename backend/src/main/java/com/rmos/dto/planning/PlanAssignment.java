package com.rmos.dto.planning;

import com.rmos.domain.planning.PlanningTaskStatus;
import java.time.LocalDateTime;

public class PlanAssignment {

    private Long taskId;
    private LocalDateTime assignedStart;
    private LocalDateTime assignedEnd;
    private Long assignedAssetId;
    private PlanningTaskStatus status;

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public LocalDateTime getAssignedStart() {
        return assignedStart;
    }

    public void setAssignedStart(LocalDateTime assignedStart) {
        this.assignedStart = assignedStart;
    }

    public LocalDateTime getAssignedEnd() {
        return assignedEnd;
    }

    public void setAssignedEnd(LocalDateTime assignedEnd) {
        this.assignedEnd = assignedEnd;
    }

    public Long getAssignedAssetId() {
        return assignedAssetId;
    }

    public void setAssignedAssetId(Long assignedAssetId) {
        this.assignedAssetId = assignedAssetId;
    }

    public PlanningTaskStatus getStatus() {
        return status;
    }

    public void setStatus(PlanningTaskStatus status) {
        this.status = status;
    }
}
