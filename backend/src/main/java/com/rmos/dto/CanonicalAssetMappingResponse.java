package com.rmos.dto;

import com.rmos.domain.DataSourceType;
import java.time.LocalDateTime;

public class CanonicalAssetMappingResponse {
    private Long id;
    private DataSourceType sourceType;
    private String sourceAssetId;
    private String canonicalAssetId;
    private boolean active;
    private LocalDateTime mappedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DataSourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(DataSourceType sourceType) {
        this.sourceType = sourceType;
    }

    public String getSourceAssetId() {
        return sourceAssetId;
    }

    public void setSourceAssetId(String sourceAssetId) {
        this.sourceAssetId = sourceAssetId;
    }

    public String getCanonicalAssetId() {
        return canonicalAssetId;
    }

    public void setCanonicalAssetId(String canonicalAssetId) {
        this.canonicalAssetId = canonicalAssetId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getMappedAt() {
        return mappedAt;
    }

    public void setMappedAt(LocalDateTime mappedAt) {
        this.mappedAt = mappedAt;
    }
}
