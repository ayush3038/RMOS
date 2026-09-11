package com.rmos.service.ai;

import com.rmos.config.NimProperties;
import com.rmos.dto.ai.ExplanationRequest;
import com.rmos.dto.ai.ExplanationResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ExplanationService {

    private final ExplanationModel model;
    private final NimProperties nimProperties;

    // Orchestrates explicit error handling and checks enabled status
    public ExplanationService(ExplanationModel model, NimProperties nimProperties) {
        this.model = model;
        this.nimProperties = nimProperties;
    }

    public ExplanationResponse explain(ExplanationRequest request) {
        if (!nimProperties.isEnabled()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "NVIDIA NIM Explanation service is currently disabled in configuration");
        }

        try {
            return model.explain(request);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "NVIDIA NIM inference exception: " + e.getMessage(), e);
        }
    }
}
