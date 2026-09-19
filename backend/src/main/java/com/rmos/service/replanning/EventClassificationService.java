package com.rmos.service.replanning;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rmos.domain.event.RmosRealtimeEvent;
import org.springframework.stereotype.Service;

@Service
public class EventClassificationService {

    private final ObjectMapper objectMapper;
    private static final int DELAY_MATERIAL_THRESHOLD_MINUTES = 15;

    public EventClassificationService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public enum ChangeMateriality {
        MATERIAL,
        NON_MATERIAL
    }

    /**
     * Deterministically classifies an incident event as material or non-material
     * based on explicitly defined rules.
     */
    public ChangeMateriality classify(RmosRealtimeEvent event) {
        if (event == null || event.getEventType() == null) {
            return ChangeMateriality.NON_MATERIAL;
        }

        try {
            JsonNode payload = null;
            if (event.getPayload() != null && !event.getPayload().isEmpty()) {
                payload = objectMapper.readTree(event.getPayload());
            }

            return switch (event.getEventType()) {
                case "TRAIN_DELAY", "TRAIN_MOVEMENT_UPDATED" -> classifyTrainDelay(payload);
                case "BLOCK_WINDOW_CHANGED", "BLOCK_WINDOW_UNAVAILABLE" -> ChangeMateriality.MATERIAL;
                case "TASK_PRIORITY_CHANGED", "TASK_URGENCY_INCREASED" -> ChangeMateriality.MATERIAL;
                case "TASK_DURATION_CHANGED", "TASK_CANCELLED", "NEW_MAINTENANCE_TASK" -> ChangeMateriality.MATERIAL;
                case "CORRIDOR_AVAILABILITY_CHANGED" -> ChangeMateriality.MATERIAL;
                default -> ChangeMateriality.NON_MATERIAL;
            };
        } catch (Exception e) {
            // Unparseable payload -> safety fallback is NON_MATERIAL so we don't spam
            // broken replans.
            return ChangeMateriality.NON_MATERIAL;
        }
    }

    private ChangeMateriality classifyTrainDelay(JsonNode payload) {
        if (payload != null && payload.has("delayMinutes")) {
            int delay = payload.get("delayMinutes").asInt(0);
            if (delay >= DELAY_MATERIAL_THRESHOLD_MINUTES) {
                return ChangeMateriality.MATERIAL;
            }
        }
        return ChangeMateriality.NON_MATERIAL;
    }
}
