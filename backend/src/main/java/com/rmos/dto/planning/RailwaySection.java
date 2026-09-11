package com.rmos.dto.planning;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class RailwaySection {

    @NotBlank(message = "Section ID required")
    private String sectionId;

    @NotBlank(message = "Corridor ID required")
    private String corridorId;

    @NotBlank(message = "Name required")
    private String name;

    @NotNull(message = "Active status required")
    private Boolean active;

    public String getSectionId() {
        return sectionId;
    }

    public void setSectionId(String sectionId) {
        this.sectionId = sectionId;
    }

    public String getCorridorId() {
        return corridorId;
    }

    public void setCorridorId(String corridorId) {
        this.corridorId = corridorId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
