package com.rmos.service.ai;

import com.rmos.dto.ai.ExplanationRequest;
import com.rmos.dto.ai.ExplanationResponse;

public interface ExplanationModel {
    ExplanationResponse explain(ExplanationRequest request);
}
