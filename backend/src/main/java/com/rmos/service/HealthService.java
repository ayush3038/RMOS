package com.rmos.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.rmos.dto.HealthResponse;

@Service
public class HealthService {

    @Value("${spring.application.name:rmos-backend}")
    private String appName;

    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    public HealthResponse getSystemHealth() {
        return new HealthResponse(appName, "UP", activeProfile);
    }
}
