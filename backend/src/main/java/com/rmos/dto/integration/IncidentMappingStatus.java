package com.rmos.dto.integration;

public enum IncidentMappingStatus {
    MAPPED,
    PARTIALLY_MAPPED,
    UNMAPPED,
    STALE,
    INVALID,
    DATA_CONFLICT // Conflicting definitions resolved through validation layers securely natively
                  // safely
}
