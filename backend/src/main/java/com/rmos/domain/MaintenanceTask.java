package com.rmos.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "maintenance_tasks")
public class MaintenanceTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_code", unique = true, nullable = false)
    private String taskCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id")
    private Asset asset;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Department department;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PriorityLevel criticality;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PriorityLevel urgency;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status;

    @Column(name = "source_system")
    private String sourceSystem;

    public MaintenanceTask() {
    }

    public MaintenanceTask(String taskCode, Asset asset, Department department, PriorityLevel criticality,
            PriorityLevel urgency, LocalDateTime dueDate, Integer durationMinutes, TaskStatus status,
            String sourceSystem) {
        this.taskCode = taskCode;
        this.asset = asset;
        this.department = department;
        this.criticality = criticality;
        this.urgency = urgency;
        this.dueDate = dueDate;
        this.durationMinutes = durationMinutes;
        this.status = status;
        this.sourceSystem = sourceSystem;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTaskCode() {
        return taskCode;
    }

    public void setTaskCode(String taskCode) {
        this.taskCode = taskCode;
    }

    public Asset getAsset() {
        return asset;
    }

    public void setAsset(Asset asset) {
        this.asset = asset;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public PriorityLevel getCriticality() {
        return criticality;
    }

    public void setCriticality(PriorityLevel criticality) {
        this.criticality = criticality;
    }

    public PriorityLevel getUrgency() {
        return urgency;
    }

    public void setUrgency(PriorityLevel urgency) {
        this.urgency = urgency;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public String getSourceSystem() {
        return sourceSystem;
    }

    public void setSourceSystem(String sourceSystem) {
        this.sourceSystem = sourceSystem;
    }
}
