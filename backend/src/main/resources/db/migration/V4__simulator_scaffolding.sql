CREATE TABLE simulation_state (
    id VARCHAR(50) PRIMARY KEY,
    is_active BOOLEAN NOT NULL DEFAULT FALSE,
    current_scenario VARCHAR(50),
    seed BIGINT,
    interval_seconds INT,
    current_sequence BIGINT,
    started_at TIMESTAMP,
    last_event_at TIMESTAMP
);
