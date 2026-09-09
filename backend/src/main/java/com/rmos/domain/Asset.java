package com.rmos.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "assets")
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "asset_id", unique = true, nullable = false)
    private String assetId;

    @Column(name = "asset_type", nullable = false)
    private String assetType;

    @Column(name = "section_id")
    private String sectionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Department department;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    public Asset() {
    }

    public Asset(String assetId, String assetType, String sectionId, Department department, boolean active) {
        this.assetId = assetId;
        this.assetType = assetType;
        this.sectionId = sectionId;
        this.department = department;
        this.active = active;
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

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
