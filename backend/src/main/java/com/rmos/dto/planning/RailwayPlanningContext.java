package com.rmos.dto.planning;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.AssertTrue;

import java.time.LocalDateTime;
import java.util.List;

public class RailwayPlanningContext {

    @NotNull(message = "Planning window start required")
    private LocalDateTime planningWindowStart;

    @NotNull(message = "Planning window end required")
    private LocalDateTime planningWindowEnd;

    @Valid
    private List<RailwaySection> sections;

    @Valid
    private List<MaintenanceBlockWindow> blockWindows;

    @AssertTrue(message = "Context window start must be before end")
    private boolean isWindowValid() {
        if (planningWindowStart != null && planningWindowEnd != null) {
            return planningWindowStart.isBefore(planningWindowEnd);
        }
        return true;
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

    public List<RailwaySection> getSections() {
        return sections;
    }

    public void setSections(List<RailwaySection> sections) {
        this.sections = sections;
    }

    public List<MaintenanceBlockWindow> getBlockWindows() {
        return blockWindows;
    }

    public void setBlockWindows(List<MaintenanceBlockWindow> blockWindows) {
        this.blockWindows = blockWindows;
    }
}
