package com.rmos.dto.integration;

import com.rmos.dto.planning.RailwayPlanningContext;

public class IncidentMappingResult {

    private IncidentContext incident;
    private IncidentMappingStatus status;
    private RailwayPlanningContext context;
    private String reason;

    public IncidentMappingResult(IncidentContext incident, IncidentMappingStatus status, RailwayPlanningContext context,
            String reason) {
        this.incident = incident;
        this.status = status;
        this.context = context;
        this.reason = reason;
    }

    public IncidentContext getIncident() {
        return incident;
    }

    public void setIncident(IncidentContext incident) {
        this.incident = incident;
    }

    public IncidentMappingStatus getStatus() {
        return status;
    }

    public void setStatus(IncidentMappingStatus status) {
        this.status = status;
    }

    public RailwayPlanningContext getContext() {
        return context;
    }

    public void setContext(RailwayPlanningContext context) {
        this.context = context;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
