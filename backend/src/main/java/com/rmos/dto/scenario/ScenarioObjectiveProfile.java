package com.rmos.dto.scenario;

public class ScenarioObjectiveProfile {

    private boolean maximizeScheduledTasks;
    private boolean maximizePriorityCoverage;
    private boolean minimizeUnscheduledTasks;
    private boolean minimizeTrainImpact;
    private boolean minimizeWorkforceLoad;

    public boolean isMaximizeScheduledTasks() {
        return maximizeScheduledTasks;
    }

    public void setMaximizeScheduledTasks(boolean maximizeScheduledTasks) {
        this.maximizeScheduledTasks = maximizeScheduledTasks;
    }

    public boolean isMaximizePriorityCoverage() {
        return maximizePriorityCoverage;
    }

    public void setMaximizePriorityCoverage(boolean maximizePriorityCoverage) {
        this.maximizePriorityCoverage = maximizePriorityCoverage;
    }

    public boolean isMinimizeUnscheduledTasks() {
        return minimizeUnscheduledTasks;
    }

    public void setMinimizeUnscheduledTasks(boolean minimizeUnscheduledTasks) {
        this.minimizeUnscheduledTasks = minimizeUnscheduledTasks;
    }

    public boolean isMinimizeTrainImpact() {
        return minimizeTrainImpact;
    }

    public void setMinimizeTrainImpact(boolean minimizeTrainImpact) {
        this.minimizeTrainImpact = minimizeTrainImpact;
    }

    public boolean isMinimizeWorkforceLoad() {
        return minimizeWorkforceLoad;
    }

    public void setMinimizeWorkforceLoad(boolean minimizeWorkforceLoad) {
        this.minimizeWorkforceLoad = minimizeWorkforceLoad;
    }
}
