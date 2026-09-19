-- Update V2 to reflect optimistic locking entity limits
ALTER TABLE planning_runs ADD COLUMN entity_version BIGINT DEFAULT 0;
