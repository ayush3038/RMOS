package com.rmos.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;

@Entity
@Table(name = "canonical_asset_mapping", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "source_type", "source_asset_id" })
}, indexes = {
        @Index(name = "idx_canonical_asset_id", columnList = "canonical_asset_id")
})
public class CanonicalAssetMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false)
    private DataSourceType sourceType;

    @Column(name = "source_asset_id", nullable = false)
    private String sourceAssetId;

    @Column(name = "canonical_asset_id", nullable = false)
    private String canonicalAssetId;

    @Column(name = "active")
    private boolean active;

    @Column(name = "mapped_at")
    private LocalDateTime mappedAt;

    // Getters and Setters

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
