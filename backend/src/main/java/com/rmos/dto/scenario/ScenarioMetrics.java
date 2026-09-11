package com.rmos.dto.scenario;

public class ScenarioMetrics {

    private int totalTasks;
    private int scheduledTasks;
    private int unscheduledTasks;

    private Double scheduledPrioritySum;
    private Double averagePriorityOfScheduledTasks;

    private int totalScheduledMinutes;
    private int totalUnscheduledMinutes;

    private Double blockWindowUtilization;

    private int trainImpactTotal;
    private int workforceRequirementTotal;

    // Getters and setters
    public int getTotalTasks() {
        return totalTasks;
    }

    public void setTotalTasks(int totalTasks) {
        this.totalTasks = totalTasks;
    }

    public int getScheduledTasks() {
        return scheduledTasks;
    }

    public void setScheduledTasks(int scheduledTasks) {
        this.scheduledTasks = scheduledTasks;
    }

    public int getUnscheduledTasks() {
        return unscheduledTasks;
    }

    public void setUnscheduledTasks(int unscheduledTasks) {
        this.unscheduledTasks = unscheduledTasks;
    }

    public Double getScheduledPrioritySum() {
        return scheduledPrioritySum;
    }

    public void setScheduledPrioritySum(Double scheduledPrioritySum) {
        this.scheduledPrioritySum = scheduledPrioritySum;
    }

    public Double getAveragePriorityOfScheduledTasks() {
        return averagePriorityOfScheduledTasks;
    }

    public void setAveragePriorityOfScheduledTasks(Double averagePriorityOfScheduledTasks) {
        this.averagePriorityOfScheduledTasks = averagePriorityOfScheduledTasks;
    }

    public int getTotalScheduledMinutes() {
        return totalScheduledMinutes;
    }

    public void setTotalScheduledMinutes(int totalScheduledMinutes) {
        this.totalScheduledMinutes = totalScheduledMinutes;
    }

    public int getTotalUnscheduledMinutes() {
        return totalUnscheduledMinutes;
    }

    public void setTotalUnscheduledMinutes(int totalUnscheduledMinutes) {
        this.totalUnscheduledMinutes = totalUnscheduledMinutes;
    }

    public Double getBlockWindowUtilization() {
        return blockWindowUtilization;
    }

    public void setBlockWindowUtilization(Double blockWindowUtilization) {
        this.blockWindowUtilization = blockWindowUtilization;
    }

    public int getTrainImpactTotal() {
        return trainImpactTotal;
    }

    public void setTrainImpactTotal(int trainImpactTotal) {
        this.trainImpactTotal = trainImpactTotal;
    }

    public int getWorkforceRequirementTotal() {
        return workforceRequirementTotal;
    }

    public void setWorkforceRequirementTotal(int workforceRequirementTotal) {
        this.workforceRequirementTotal = workforceRequirementTotal;
    }
}
