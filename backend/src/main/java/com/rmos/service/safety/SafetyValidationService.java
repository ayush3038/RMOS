package com.rmos.service.safety;

import com.rmos.dto.safety.SafetyValidationRequest;
import com.rmos.dto.safety.SafetyValidationResult;
import org.springframework.stereotype.Service;

@Service
public class SafetyValidationService {

    private final SafetyRuleEngine ruleEngine;

    public SafetyValidationService(SafetyRuleEngine ruleEngine) {
        this.ruleEngine = ruleEngine;
    }

    public SafetyValidationResult validate(SafetyValidationRequest request) {
        return ruleEngine.validate(request);
    }
}
