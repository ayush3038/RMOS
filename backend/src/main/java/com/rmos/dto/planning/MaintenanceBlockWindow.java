package com.rmos.dto.planning;

import com.rmos.domain.planning.BlockWindowStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.AssertTrue;

import java.time.LocalDateTime;

public class MaintenanceBlockWindow {

    @NotBlank(message = "Block ID required")
    private String blockId;

    @NotBlank(message = "Section ID required")
    private String sectionId;

    @NotNull(message = "Window start required")
    private LocalDateTime windowStart;

    @NotNull(message = "Window end required")
    private LocalDateTime windowEnd;

    private BlockWindowStatus status;

    @AssertTrue(message = "Window start must be before window end")
    private boolean isWindowValid() {
        if (windowStart != null && windowEnd != null) {
            return windowStart.isBefore(windowEnd);
        }
        return true;
    }

    public String getBlockId() {
        return blockId;
    }

    public void setBlockId(String blockId) {
        this.blockId = blockId;
    }

    public String getSectionId() {
        return sectionId;
    }

    public void setSectionId(String sectionId) {
        this.sectionId = sectionId;
    }

    public LocalDateTime getWindowStart() {
        return windowStart;
    }

    public void setWindowStart(LocalDateTime windowStart) {
        this.windowStart = windowStart;
    }

    public LocalDateTime getWindowEnd() {
        return windowEnd;
    }

    public void setWindowEnd(LocalDateTime windowEnd) {
        this.windowEnd = windowEnd;
    }

    public BlockWindowStatus getStatus() {
        return status;
    }

    public void setStatus(BlockWindowStatus status) {
        this.status = status;
    }
}
