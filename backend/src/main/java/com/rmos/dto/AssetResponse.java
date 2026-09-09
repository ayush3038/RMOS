package com.rmos.dto;

import com.rmos.domain.Asset;

public class AssetResponse {
    private Long id;
    private String assetId;
    private String assetType;
    private String sectionId;
    private String department;
    private boolean active;

    public AssetResponse() {
    }

    public AssetResponse(Long id, String assetId, String assetType, String sectionId, String department,
            boolean active) {
        this.id = id;
        this.assetId = assetId;
        this.assetType = assetType;
        this.sectionId = sectionId;
        this.department = department;
        this.active = active;
    }

    public static AssetResponse fromEntity(Asset asset) {
        return new AssetResponse(
                asset.getId(),
                asset.getAssetId(),
                asset.getAssetType(),
                asset.getSectionId(),
                asset.getDepartment() != null ? asset.getDepartment().name() : null,
                asset.isActive());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAssetId() {
        return assetId;
    }

    public void setAssetId(String assetId) {
        this.assetId = assetId;
    }

    public String getAssetType() {
        return assetType;
    }

    public void setAssetType(String assetType) {
        this.assetType = assetType;
    }

    public String getSectionId() {
        return sectionId;
    }

    public void setSectionId(String sectionId) {
        this.sectionId = sectionId;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
