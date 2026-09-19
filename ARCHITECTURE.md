RMOS System Architecture

1. Architecture principle

Separate prediction, optimization, explanation and approval.

ML estimates priority/risk.

Rules enforce hard constraints.

OR-Tools schedules work.

LLM explains and summarizes.

Humans approve.

2. High-level flow

[TMS] [SMMS] [TDMS] [BDMS] [COA]
        + timetable / train movement / goods forecast
                         |
                         v
              [Integration Layer]
                         |
                         v
         [Normalization + Validation]
                         |
             +-----------+-----------+
             |                       |
             v                       v
   [Canonical Asset Mapping]   [Freshness / Timestamp]
             |                       |
             +-----------+-----------+
                         |
                         v
               [RMOS Planning Core]
                         |
             +-----------+-----------+
             |                       |
             v                       v
       [ML Priority/Risk]      [Safety/Rules]
             |                       |
             +-----------+-----------+
                         v
                [OR-Tools CP-SAT]
                         |
                         v
             [Candidate Block Plan]
                         |
                         v
              [LLM Explanation]
                         |
                         v
               [Human Approval]
                         |
              +----------+---------+
              |                    |
              v                    v
       [Approved Plan]       [Modify / Reject]
              |
              v
      [Dashboard / Reports / Audit]

3. Backend

Java + Spring Boot.

Suggested modules/packages:

com.rmos
  auth/
  asset/
  maintenance/
  corridor/
  train/
  planning/
  optimization/
  prioritization/
  explanation/
  approval/
  audit/
  integration/
  common/

Use controller → service → repository boundaries. Keep optimization logic isolated from HTTP and persistence concerns.

4. Frontend

React + TypeScript.

Responsive dashboard shell.

Tables for dense operational data.

Map view for assets and maintenance activity.

Timeline/calendar for blocks.

Approval and explanation views.

No horizontal overflow on normal viewport sizes.

5. Optimization architecture

Input:

candidate tasks

available corridor windows

train movement constraints

task duration

workforce/resource availability

compatibility rules

mandatory safety rules

Output:

scheduled tasks

grouped/joint tasks

chosen block windows

conflicts that cannot be resolved

objective values / explanation metadata

The optimizer must return infeasibility details when no feasible plan exists.

6. AI architecture

ML

Priority/risk model consumes structured features. It should produce a bounded score/ranking plus feature metadata where possible.

NIM / model inference

Use NVIDIA NIM or another approved inference endpoint for compatible models.

LLM

Use for:

schedule explanation

summarization

maintenance report summaries

operator question answering over authorized RMOS data

LLM output should use JSON schemas when consumed programmatically.

7. Replanning

Use event-driven or scheduled triggers. On a material change:

Validate latest data.

Detect affected plan entries.

Lock/retain unaffected decisions where safe.

Re-run optimizer for affected horizon.

Generate explanation.

Send revised plan for human approval.

8. Security

Spring Security + JWT for prototype authorization. Roles should be explicit, for example:

VIEWER

PLANNER

REVIEWER

ADMIN

Authorization must be enforced server-side.

9. Auditability

Record:

who created/changed/approved/rejected a recommendation

input snapshot/version

optimizer version

model/version identifiers

constraint set/version

timestamp

resulting schedule

explanation payload where appropriate