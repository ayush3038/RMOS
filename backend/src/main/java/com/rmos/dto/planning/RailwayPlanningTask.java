package com.rmos.dto.planning;

import jakarta.validation.Valid;

public class RailwayPlanningTask extends PlanningTask {

    private String sectionId;

    private String corridorId;

    @Valid
    private MaintenanceBlockWindow maintenanceBlockWindow;

    @Valid
    private TrainImpactProfile trainImpactProfile;

    @Valid
    private WorkforceProfile workforceProfile;

    public String getSectionId() {
        return sectionId;
    }

    public void setSectionId(String sectionId) {
        this.sectionId = sectionId;
    }

    public String getCorridorId() {
        return corridorId;
    }

    public void setCorridorId(String corridorId) {
        this.corridorId = corridorId;
    }

    public MaintenanceBlockWindow getMaintenanceBlockWindow() {
        return maintenanceBlockWindow;
    }

    public void setMaintenanceBlockWindow(MaintenanceBlockWindow maintenanceBlockWindow) {
        this.maintenanceBlockWindow = maintenanceBlockWindow;
    }

    public TrainImpactProfile getTrainImpactProfile() {
        return trainImpactProfile;
    }

    public void setTrainImpactProfile(TrainImpactProfile trainImpactProfile) {
        this.trainImpactProfile = trainImpactProfile;
    }

    public WorkforceProfile getWorkforceProfile() {
        return workforceProfile;
    }

    public void setWorkforceProfile(WorkforceProfile workforceProfile) {
        this.workforceProfile = workforceProfile;
    }
}
