package com.rmos.dto.integration;

import java.time.LocalDateTime;
import java.util.Map;

public class IncidentContext {

    private String id;
    private String sourceSystem;
    private String sourceRecordId;
    private String incidentType;
    private String severity; // e.g. CRITICAL, HIGH, LOW
    private Long assetId;
    private String corridorId;
    private String trainId;
    private LocalDateTime occurredAt;
    private LocalDateTime sourceUpdatedAt;
    private LocalDateTime receivedAt;
    private String status;
    private LocalDateTime impactWindowStart;
    private LocalDateTime impactWindowEnd;
    private String description;
    private Map<String, Object> metadata;
    private String freshness; // FRESH, STALE, UNKNOWN

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSourceSystem() {
        return sourceSystem;
    }

    public void setSourceSystem(String sourceSystem) {
        this.sourceSystem = sourceSystem;
    }

    public String getSourceRecordId() {
        return sourceRecordId;
    }

    public void setSourceRecordId(String sourceRecordId) {
        this.sourceRecordId = sourceRecordId;
    }

    public String getIncidentType() {
        return incidentType;
    }

    public void setIncidentType(String incidentType) {
        this.incidentType = incidentType;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public Long getAssetId() {
        return assetId;
    }

    public void setAssetId(Long assetId) {
        this.assetId = assetId;
    }

    public String getCorridorId() {
        return corridorId;
    }

    public void setCorridorId(String corridorId) {
        this.corridorId = corridorId;
    }

    public String getTrainId() {
        return trainId;
    }

    public void setTrainId(String trainId) {
        this.trainId = trainId;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(LocalDateTime occurredAt) {
        this.occurredAt = occurredAt;
    }

    public LocalDateTime getSourceUpdatedAt() {
        return sourceUpdatedAt;
    }

    public void setSourceUpdatedAt(LocalDateTime sourceUpdatedAt) {
        this.sourceUpdatedAt = sourceUpdatedAt;
    }

    public LocalDateTime getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(LocalDateTime receivedAt) {
        this.receivedAt = receivedAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getImpactWindowStart() {
        return impactWindowStart;
    }

    public void setImpactWindowStart(LocalDateTime impactWindowStart) {
        this.impactWindowStart = impactWindowStart;
    }

    public LocalDateTime getImpactWindowEnd() {
        return impactWindowEnd;
    }

    public void setImpactWindowEnd(LocalDateTime impactWindowEnd) {
        this.impactWindowEnd = impactWindowEnd;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public String getFreshness() {
        return freshness;
    }

    public void setFreshness(String freshness) {
        this.freshness = freshness;
    }
}
