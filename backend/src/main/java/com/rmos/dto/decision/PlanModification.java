package com.rmos.dto.decision;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

public class PlanModification {

    @NotBlank(message = "taskId is required")
    private String taskId;

    private LocalDateTime requestedStart;

    private LocalDateTime requestedEnd;

    @NotBlank(message = "reason is required")
    private String reason;

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public LocalDateTime getRequestedStart() {
        return requestedStart;
    }

    public void setRequestedStart(LocalDateTime requestedStart) {
        this.requestedStart = requestedStart;
    }

    public LocalDateTime getRequestedEnd() {
        return requestedEnd;
    }

    public void setRequestedEnd(LocalDateTime requestedEnd) {
        this.requestedEnd = requestedEnd;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
