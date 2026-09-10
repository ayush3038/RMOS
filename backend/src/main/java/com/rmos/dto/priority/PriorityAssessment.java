package com.rmos.dto.priority;

import com.rmos.domain.priority.PriorityBand;
import java.util.ArrayList;
import java.util.List;

public class PriorityAssessment {
    private Long taskId;
    private double priorityScore;
    private PriorityBand priorityBand;
    private List<PriorityFactorContribution> factorBreakdown = new ArrayList<>();

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public double getPriorityScore() {
        return priorityScore;
    }

    public void setPriorityScore(double priorityScore) {
        this.priorityScore = priorityScore;
    }

    public PriorityBand getPriorityBand() {
        return priorityBand;
    }

    public void setPriorityBand(PriorityBand priorityBand) {
        this.priorityBand = priorityBand;
    }

    public List<PriorityFactorContribution> getFactorBreakdown() {
        return factorBreakdown;
    }

    public void setFactorBreakdown(List<PriorityFactorContribution> factorBreakdown) {
        this.factorBreakdown = factorBreakdown;
    }
}
