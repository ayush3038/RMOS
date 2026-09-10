package com.rmos.service.safety;

import com.rmos.dto.safety.SafetyValidationRequest;
import com.rmos.dto.safety.SafetyValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SafetyValidationServiceTest {

    @Mock
    private SafetyRuleEngine ruleEngine;

    @InjectMocks
    private SafetyValidationService service;

    @Test
    void validate_DelegatesToEngine() {
        SafetyValidationRequest request = new SafetyValidationRequest();
        when(ruleEngine.validate(any())).thenReturn(new SafetyValidationResult());

        SafetyValidationResult result = service.validate(request);

        assertNotNull(result);
        verify(ruleEngine).validate(request);
    }
}
