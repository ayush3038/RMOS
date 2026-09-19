package com.rmos.domain.planning;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "planning_runs")
public class PlanningRunEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private Integer version;

    @Column(name = "parent_plan_id")
    private String parentPlanId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlanningRunStatus status;

    @Version
    @Column(name = "entity_version")
    private Long entityVersion;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "trigger_event_id")
    private String triggerEventId;

    @Column(name = "replan_reason", columnDefinition = "TEXT")
    private String replanReason;

    @Column(name = "snapshot_hash")
    private String snapshotHash;

    @OneToMany(mappedBy = "planningRun", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlanAssignmentEntity> assignments = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getParentPlanId() {
        return parentPlanId;
    }

    public void setParentPlanId(String parentPlanId) {
        this.parentPlanId = parentPlanId;
    }

    public PlanningRunStatus getStatus() {
        return status;
    }

    public void setStatus(PlanningRunStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Long getEntityVersion() {
        return entityVersion;
    }

    public void setEntityVersion(Long entityVersion) {
        this.entityVersion = entityVersion;
    }

    public String getTriggerEventId() {
        return triggerEventId;
    }

    public void setTriggerEventId(String triggerEventId) {
        this.triggerEventId = triggerEventId;
    }

    public String getReplanReason() {
        return replanReason;
    }

    public void setReplanReason(String replanReason) {
        this.replanReason = replanReason;
    }

    public String getSnapshotHash() {
        return snapshotHash;
    }

    public void setSnapshotHash(String snapshotHash) {
        this.snapshotHash = snapshotHash;
    }

    public List<PlanAssignmentEntity> getAssignments() {
        return assignments;
    }

    public void setAssignments(List<PlanAssignmentEntity> assignments) {
        this.assignments = assignments;
        for (PlanAssignmentEntity assignment : assignments) {
            assignment.setPlanningRun(this);
        }
    }
}
