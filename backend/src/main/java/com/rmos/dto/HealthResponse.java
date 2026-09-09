package com.rmos.dto;

public class HealthResponse {
    private String application;
    private String status;
    private String environment;

    public HealthResponse(String application, String status, String environment) {
        this.application = application;
        this.status = status;
        this.environment = environment;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }
}
