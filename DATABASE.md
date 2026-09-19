RMOS Database Design

PostgreSQL is the primary database. PostGIS may be enabled for location-aware asset and maintenance visualization.

Core entities

users

id (UUID, PK)

name

username

password_hash

role

department

active

created_at

departments

id (UUID, PK)

code

name

active

Examples: ENGINEERING, SIGNALLING, TRACTION, OPERATING, ADMIN.

assets

id (UUID, PK)

canonical_asset_id (unique)

asset_type

name

division

department_id (FK)

latitude

longitude

criticality

health_status

created_at

updated_at

asset_name_mappings

id (UUID, PK)

asset_id (FK)

division

source_system

source_name

mapping_confidence

verified

created_at

Purpose: resolve division-local names to a canonical asset.

maintenance_tasks

id (UUID, PK)

source_system

source_record_id

department_id (FK)

asset_id (FK)

task_type

description

severity

criticality

urgency

overdue_days

estimated_duration_min

workforce_required

status

reported_at

due_at

source_updated_at

created_at

updated_at

asset_health_records

id (UUID, PK)

asset_id (FK)

health_score

failure_risk

evidence_type

observed_at

source_system

model_version

corridor_windows

id (UUID, PK)

corridor_id

start_time

end_time

availability_status

source_system

source_updated_at

train_movements

id (UUID, PK)

train_number

train_type

service_date

corridor_id

planned_start

planned_end

actual_start

actual_end

status

source_updated_at

planning_runs

id (UUID, PK)

horizon_type (WEEKLY / MONTHLY / AD_HOC)

requested_at

requested_by

optimizer_version

model_version

input_snapshot_id

status

objective_value

created_at

block_plans

id (UUID, PK)

planning_run_id (FK)

corridor_id

start_time

end_time

status

train_impact_score

asset_availability_effect

explanation_status

created_at

block_plan_tasks

block_plan_id (FK)

maintenance_task_id (FK)

sequence_no

combined_group_id

conflicts

id (UUID, PK)

planning_run_id (FK)

conflict_type

severity

description

related_task_id

related_train_id

resolved

approvals

id (UUID, PK)

block_plan_id (FK)

reviewer_id (FK users)

action (APPROVE / MODIFY / REJECT / CANCEL)

comments

created_at

audit_events

id (UUID, PK)

actor_id (FK users)

entity_type

entity_id

action

before_json

after_json

metadata_json

created_at

Indexes

Recommended indexes:

maintenance_tasks(asset_id, status)

maintenance_tasks(due_at)

maintenance_tasks(source_updated_at)

corridor_windows(corridor_id, start_time, end_time)

train_movements(corridor_id, planned_start, planned_end)

block_plans(planning_run_id)

audit_events(entity_type, entity_id, created_at)

Data rules

Use UUIDs internally.

Preserve source_system + source_record_id.

Never overwrite source timestamps with local timestamps.

Store UTC timestamps; convert to user timezone in the UI.

Treat canonical asset IDs as stable identifiers.

Do not silently merge assets when identity is uncertain.