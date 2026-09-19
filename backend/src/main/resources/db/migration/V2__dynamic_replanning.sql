-- V2: Dynamic Replanning Base Structures
-- M3.16

CREATE TABLE planning_runs (
    id VARCHAR(36) PRIMARY KEY,
    version INT NOT NULL,
    parent_plan_id VARCHAR(36),
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    trigger_event_id VARCHAR(100),
    replan_reason TEXT,
    snapshot_hash VARCHAR(255)
);

CREATE TABLE plan_assignments (
    id SERIAL PRIMARY KEY,
    planning_run_id VARCHAR(36) NOT NULL,
    task_id BIGINT NOT NULL,
    assigned_start TIMESTAMP,
    assigned_end TIMESTAMP,
    status VARCHAR(50) NOT NULL,
    FOREIGN KEY (planning_run_id) REFERENCES planning_runs(id) ON DELETE CASCADE
);
