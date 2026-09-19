package com.rmos.simulator.service;

import com.rmos.dto.integration.IncidentContext;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class SimulationEventGenerator {

    public IncidentContext generateEventContext(Long sequence, String scenarioType, String incidentType, Long assetId) {
        IncidentContext idx = new IncidentContext();
        idx.setId("SIM-" + UUID.randomUUID().toString());
        idx.setSourceSystem("SIMULATOR");
        idx.setSourceRecordId("SEQ-" + sequence);
        idx.setIncidentType(incidentType);
        idx.setFreshness("FRESH");
        idx.setSourceUpdatedAt(LocalDateTime.now());
        idx.setReceivedAt(LocalDateTime.now());
        idx.setAssetId(assetId);
        // Map deterministic payload parameters inside metadata if needed.

        return idx;
    }
}
