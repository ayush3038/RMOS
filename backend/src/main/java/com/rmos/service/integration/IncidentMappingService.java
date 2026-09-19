package com.rmos.service.integration;

import com.rmos.dto.integration.IncidentContext;
import com.rmos.dto.integration.IncidentMappingResult;
import com.rmos.dto.integration.IncidentMappingStatus;
import com.rmos.dto.planning.RailwayPlanningContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class IncidentMappingService {

    private static final Logger logger = LoggerFactory.getLogger(IncidentMappingService.class);

    /**
     * Deterministically maps a raw upstream Incident into an RMOS Railway Planning
     * Context impact block.
     */
    public IncidentMappingResult mapIncidentToPlanningContext(IncidentContext incident) {
        // Validate Identity & Freshness constraints immediately
        if (incident.getSourceRecordId() == null || incident.getSourceSystem() == null) {
            logMappingFailure(incident, "Missing authoritative event identity boundaries.");
            return new IncidentMappingResult(incident, IncidentMappingStatus.INVALID, null,
                    "Missing authoritative identity bound: sourceSystem or sourceRecordId.");
        }

        if ("STALE".equalsIgnoreCase(incident.getFreshness())
                || incident.getSourceUpdatedAt().isBefore(LocalDateTime.now().minusHours(24))) {
            logMappingFailure(incident, "Stale source timestamps exceed allowed TTL windows.");
            return new IncidentMappingResult(incident, IncidentMappingStatus.STALE, null,
                    "Incident data source TTL elapsed.");
        }

        if (incident.getAssetId() == null && incident.getCorridorId() == null && incident.getTrainId() == null) {
            logMappingFailure(incident, "No downstream locational bounding targets (Asset, Corridor, or Train).");
            return new IncidentMappingResult(incident, IncidentMappingStatus.UNMAPPED, null,
                    "No corresponding downstream spatial intersections available.");
        }

        // Validate time scopes safely
        if (incident.getImpactWindowStart() == null || incident.getImpactWindowEnd() == null) {
            logMappingFailure(incident, "Impact timeframe bounds are null/imprecise.");
            return new IncidentMappingResult(incident, IncidentMappingStatus.PARTIALLY_MAPPED, null,
                    "Imprecise downstream window limits.");
        }

        if (incident.getImpactWindowStart().isAfter(incident.getImpactWindowEnd())) {
            logMappingFailure(incident, "Impossible Impact Windows defined.");
            return new IncidentMappingResult(incident, IncidentMappingStatus.DATA_CONFLICT, null,
                    "Time bounding inverted.");
        }

        // Explicit creation of Railway Planning block overrides
        RailwayPlanningContext output = new RailwayPlanningContext();
        output.setPlanningWindowStart(incident.getImpactWindowStart());
        output.setPlanningWindowEnd(incident.getImpactWindowEnd());

        // Log detailed correlation tracing ID logic
        logger.info("[CORRELATION-ID={}] Deterministically bound incident {} from {} into safe plan limits.",
                incident.getId(), incident.getIncidentType(), incident.getSourceSystem());

        return new IncidentMappingResult(incident, IncidentMappingStatus.MAPPED, output,
                "Safely mapped within precise constraints.");
    }

    private void logMappingFailure(IncidentContext incident, String reason) {
        logger.warn("[CORRELATION-ID={}] Incident mapping invalidation: {} (Source: {}, ID: {})",
                incident.getId(), reason, incident.getSourceSystem(), incident.getSourceRecordId());
    }
}
