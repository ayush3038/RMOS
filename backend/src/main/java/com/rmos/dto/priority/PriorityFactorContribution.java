package com.rmos.dto.priority;

import com.rmos.domain.priority.PriorityFactor;

public class PriorityFactorContribution {
    private PriorityFactor factor;
    private double value;
    private double weight;
    private double contribution;
    private String explanation;

    public PriorityFactor getFactor() {
        return factor;
    }

    public void setFactor(PriorityFactor factor) {
        this.factor = factor;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public double getContribution() {
        return contribution;
    }

    public void setContribution(double contribution) {
        this.contribution = contribution;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }
}
