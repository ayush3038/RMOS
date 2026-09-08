# RMOS System Architecture

## 1. Architecture principle
Separate prediction, optimization, explanation and approval.

- ML estimates priority/risk.
- Rules enforce hard constraints.
- OR-Tools schedules work.
- LLM explains and summarizes.
- Humans approve.

## 2. High-level flow
```text
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
```

## 3. Backend
Java + Spring Boot.

Suggested modules/packages:
```text
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
```

Use controller → service → repository boundaries. Keep optimization logic isolated from HTTP and persistence concerns.

## 4. Frontend
React + TypeScript.
- Responsive dashboard shell.
- Tables for dense operational data.
- Map view for assets and maintenance activity.
- Timeline/calendar for blocks.
- Approval and explanation views.
- No horizontal overflow on normal viewport sizes.

## 5. Optimization architecture
Input:
- candidate tasks
- available corridor windows
- train movement constraints
- task duration
- workforce/resource availability
- compatibility rules
- mandatory safety rules

Output:
- scheduled tasks
- grouped/joint tasks
- chosen block windows
- conflicts that cannot be resolved
- objective values / explanation metadata

The optimizer must return infeasibility details when no feasible plan exists.

## 6. AI architecture
### ML
Priority/risk model consumes structured features. It should produce a bounded score/ranking plus feature metadata where possible.

### NIM / model inference
Use NVIDIA NIM or another approved inference endpoint for compatible models.

### LLM
Use for:
- schedule explanation
- summarization
- maintenance report summaries
- operator question answering over authorized RMOS data

LLM output should use JSON schemas when consumed programmatically.

## 7. Replanning
Use event-driven or scheduled triggers. On a material change:
1. Validate latest data.
2. Detect affected plan entries.
3. Lock/retain unaffected decisions where safe.
4. Re-run optimizer for affected horizon.
5. Generate explanation.
6. Send revised plan for human approval.

## 8. Security
Spring Security + JWT for prototype authorization. Roles should be explicit, for example:
- VIEWER
- PLANNER
- REVIEWER
- ADMIN

Authorization must be enforced server-side.

## 9. Auditability
Record:
- who created/changed/approved/rejected a recommendation
- input snapshot/version
- optimizer version
- model/version identifiers
- constraint set/version
- timestamp
- resulting schedule
- explanation payload where appropriate


## 10. Frontend visual/interaction architecture
Use the uploaded RAILSYNC prototype as the frontend reference implementation for layout and interaction patterns. RMOS should reproduce the same overall shell and component rhythm while changing product branding and railway domain content to RMOS. Key reusable regions:
- App shell: collapsible navy sidebar + top context bar + scrollable content area.
- Dashboard: compact KPI strip + operational panels + queue/table views.
- Operational visualizations: corridor occupancy/timeline, movement timeline, department coordination, analytics.
- Detail interaction: right-side drawer/overlay for record details, explanation and approval actions.
- Status system: text + icon + restrained colour chips.

## 11. Realtime simulation architecture
Add a `simulation/` module that behaves as a synthetic source adapter and event producer.
```text
[Scenario Config / Seed]
          |
          v
 [Simulation Engine]
          |
     emits events
          v
 [Event Bus / WebSocket]
          |
    +-----+-------------------+
    |                         |
    v                         v
[Domain State]           [Audit/Telemetry]
    |
    v
[Recompute Dependencies]
    |
    v
[Dashboard / Planner / Conflicts]
```

The simulator should maintain stateful entities for tasks, trains, corridor windows, blocks and conflicts. Events must carry source and freshness metadata. Related changes must be generated through domain rules, for example a train delay may alter corridor occupancy and trigger conflict detection, while a maintenance task becoming overdue may change prioritization inputs.

## 12. Event processing rules
- Use monotonic `sequence` numbers per simulation stream.
- Keep `sourceUpdatedAt` distinct from `receivedAt`.
- Classify freshness based on configurable thresholds.
- Make updates idempotent where possible.
- Reject out-of-order updates that would roll entity state backward unless explicitly allowed.
- Recompute only affected projections where practical.
- Never let a simulation event directly approve or execute a block.
