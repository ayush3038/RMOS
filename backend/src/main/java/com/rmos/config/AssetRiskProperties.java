package com.rmos.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "rmos.asset-risk")
public class AssetRiskProperties {

    private Weights weights = new Weights();
    private References references = new References();
    private Thresholds thresholds = new Thresholds();

    public Weights getWeights() {
        return weights;
    }

    public void setWeights(Weights weights) {
        this.weights = weights;
    }

    public References getReferences() {
        return references;
    }

    public void setReferences(References references) {
        this.references = references;
    }

    public Thresholds getThresholds() {
        return thresholds;
    }

    public void setThresholds(Thresholds thresholds) {
        this.thresholds = thresholds;
    }

    public static class Weights {
        private double overdue = 0.20;
        private double failureHistory = 0.25;
        private double recentFailure = 0.25;
        private double maintenanceRecency = 0.15;
        private double criticalMaintenance = 0.15;

        public double getOverdue() {
            return overdue;
        }

        public void setOverdue(double overdue) {
            this.overdue = overdue;
        }

        public double getFailureHistory() {
            return failureHistory;
        }

        public void setFailureHistory(double failureHistory) {
            this.failureHistory = failureHistory;
        }

        public double getRecentFailure() {
            return recentFailure;
        }

        public void setRecentFailure(double recentFailure) {
            this.recentFailure = recentFailure;
        }

        public double getMaintenanceRecency() {
            return maintenanceRecency;
        }

        public void setMaintenanceRecency(double maintenanceRecency) {
            this.maintenanceRecency = maintenanceRecency;
        }

        public double getCriticalMaintenance() {
            return criticalMaintenance;
        }

        public void setCriticalMaintenance(double criticalMaintenance) {
            this.criticalMaintenance = criticalMaintenance;
        }
    }

    public static class References {
        private double overdueTasks = 5.0;
        private double recentFailures = 3.0;
        private double maintenanceDays = 180.0;
        private double criticalMaintenance = 3.0;

        public double getOverdueTasks() {
            return overdueTasks;
        }

        public void setOverdueTasks(double overdueTasks) {
            this.overdueTasks = overdueTasks;
        }

        public double getRecentFailures() {
            return recentFailures;
        }

        public void setRecentFailures(double recentFailures) {
            this.recentFailures = recentFailures;
        }

        public double getMaintenanceDays() {
            return maintenanceDays;
        }

        public void setMaintenanceDays(double maintenanceDays) {
            this.maintenanceDays = maintenanceDays;
        }

        public double getCriticalMaintenance() {
            return criticalMaintenance;
        }

        public void setCriticalMaintenance(double criticalMaintenance) {
            this.criticalMaintenance = criticalMaintenance;
        }
    }

    public static class Thresholds {
        private double veryHigh = 0.80;
        private double high = 0.60;
        private double medium = 0.35;
        private double low = 0.15;

        public double getVeryHigh() {
            return veryHigh;
        }

        public void setVeryHigh(double veryHigh) {
            this.veryHigh = veryHigh;
        }

        public double getHigh() {
            return high;
        }

        public void setHigh(double high) {
            this.high = high;
        }

        public double getMedium() {
            return medium;
        }

        public void setMedium(double medium) {
            this.medium = medium;
        }

        public double getLow() {
            return low;
        }

        public void setLow(double low) {
            this.low = low;
        }
    }
}
