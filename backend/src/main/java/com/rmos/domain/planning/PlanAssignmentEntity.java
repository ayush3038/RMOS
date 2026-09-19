package com.rmos.domain.planning;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "plan_assignments")
public class PlanAssignmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "planning_run_id", nullable = false)
    private PlanningRunEntity planningRun;

    @Column(name = "task_id", nullable = false)
    private Long taskId;

    @Column(name = "assigned_start")
    private LocalDateTime assignedStart;

    @Column(name = "assigned_end")
    private LocalDateTime assignedEnd;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlanningTaskStatus status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PlanningRunEntity getPlanningRun() {
        return planningRun;
    }

    public void setPlanningRun(PlanningRunEntity planningRun) {
        this.planningRun = planningRun;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public LocalDateTime getAssignedStart() {
        return assignedStart;
    }

    public void setAssignedStart(LocalDateTime assignedStart) {
        this.assignedStart = assignedStart;
    }

    public LocalDateTime getAssignedEnd() {
        return assignedEnd;
    }

    public void setAssignedEnd(LocalDateTime assignedEnd) {
        this.assignedEnd = assignedEnd;
    }

    public PlanningTaskStatus getStatus() {
        return status;
    }

    public void setStatus(PlanningTaskStatus status) {
        this.status = status;
    }
}
