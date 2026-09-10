package com.rmos.dto.planning;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public class PlanningRequest {

    @NotBlank(message = "Planning ID must not be blank")
    private String planningId;

    @NotEmpty(message = "Task collection must not be empty")
    @Valid
    private List<PlanningTask> tasks;

    @Valid
    private List<PlanningConstraint> constraints;

    @NotNull(message = "Planning window start must not be null")
    private LocalDateTime planningWindowStart;

    @NotNull(message = "Planning window end must not be null")
    private LocalDateTime planningWindowEnd;

    public String getPlanningId() {
        return planningId;
    }

    public void setPlanningId(String planningId) {
        this.planningId = planningId;
    }

    public List<PlanningTask> getTasks() {
        return tasks;
    }

    public void setTasks(List<PlanningTask> tasks) {
        this.tasks = tasks;
    }

    public List<PlanningConstraint> getConstraints() {
        return constraints;
    }

    public void setConstraints(List<PlanningConstraint> constraints) {
        this.constraints = constraints;
    }

    public LocalDateTime getPlanningWindowStart() {
        return planningWindowStart;
    }

    public void setPlanningWindowStart(LocalDateTime planningWindowStart) {
        this.planningWindowStart = planningWindowStart;
    }

    public LocalDateTime getPlanningWindowEnd() {
        return planningWindowEnd;
    }

    public void setPlanningWindowEnd(LocalDateTime planningWindowEnd) {
        this.planningWindowEnd = planningWindowEnd;
    }
}
