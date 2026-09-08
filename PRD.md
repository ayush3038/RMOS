# RMOS Product Requirements Document

## 1. Product purpose
Provide a single decision-support workspace that helps authorized railway personnel coordinate maintenance demand with available block/corridor windows and train-operation constraints.

## 2. Problem
Relevant maintenance and operations information is distributed across systems and departmental workflows. Field research at CSMT found no common cross-department view, delayed data visibility can occur, signal/asset naming can vary by division, and manual coordination remains necessary.

## 3. Users / stakeholders
1. Maintenance teams — Engineering, S&T/Signalling, Traction.
2. Operating / Control Office personnel.
3. Departmental officers and planners.
4. Authorized administrative/safety reviewers.
5. RMOS administrators / technical support users.

## 4. MVP scope
### A. Unified planning view
- Consolidate candidate maintenance tasks from synthetic representations of TMS/SMMS/TDMS.
- Show block/corridor availability and train-operation context.
- Show task status, priority, duration, asset, department and data freshness.

### B. Data normalization
- Normalize records into a common internal model.
- Map division-local asset names to canonical asset IDs.
- Track source system and data timestamp.

### C. Prioritization
Rank candidate work using:
1. Safety
2. Train impact
3. Criticality
4. Urgency
5. Duration
6. Workforce
7. Remaining/other validated factors

This ordering is field-reported from the 1 September 2026 CSMT interaction and is not treated as an official Ministry-wide weighting.

### D. Optimization
Generate feasible block plans with OR-Tools CP-SAT. Hard constraints must include applicable safety, train movement, block availability, duration and resource constraints. The exact formulation/weights remain subject to validation with Operating and Engineering.

### E. Human approval
Every generated recommendation remains reviewable. User actions: Review → Modify if needed → Approve / Reject / Cancel according to role.

### F. Explanation and summary
Use an LLM for readable explanations, summaries and operator queries. LLM output must never become the authoritative scheduling decision.

### G. Dynamic replanning
When material inputs change (train delay, new urgent defect, cancelled block, work overrun), identify affected plan segments and re-run optimization for human review.

### H. Planning horizons
- Weekly plan: near-term work against current/confirmed operating conditions.
- Monthly plan: forward-looking coordination and preventive workload planning.

## 5. Prototype screens
1. Login / role selection (mocked authentication for early prototype if needed).
2. Operations dashboard.
3. Cross-department maintenance demand.
4. Corridor availability view.
5. Maintenance task detail.
6. Asset health / risk view.
7. Block planner / optimizer workspace.
8. Recommendation explanation.
9. Approval queue.
10. Weekly plan.
11. Monthly plan.
12. Audit / history.
13. Asset map.

## 6. Non-goals
- Do not replace SMMS/TMS/TDMS/BDMS/COA.
- Do not autonomously execute a railway block.
- Do not claim a live connection to railway systems without authorized implementation.
- Do not present synthetic prototype metrics as real-world railway results.
- Do not use an LLM as the scheduling engine.
- Do not define “shadow block” beyond what has actually been validated.

## 7. Key success metrics
- Asset downtime
- Maintenance lateness
- Train disruption / conflict score
- Block utilization
- Number of separate blocks required
- Number of compatible multi-department tasks combined
- Re-planning frequency / effort
- Recommendation acceptance or modification rate during controlled evaluation

## 11. Acceptance criteria for MVP
A test scenario should be able to:
1. Load multi-department maintenance tasks.
2. Mark source timestamp/freshness.
3. Resolve canonical asset IDs.
4. Apply safety and operational hard constraints.
5. Produce a feasible block plan.
6. Show objective/constraint reasoning.
7. Allow human modification and approval.
8. Re-run after an input change.
9. Generate a readable summary without changing the underlying schedule.
10. Record the full decision/audit trail.


## 8. UI / UX reference requirements
The UI must closely follow the uploaded RAILSYNC prototype for structure and visual language, while using RMOS terminology and branding. Required patterns include:
- Professional operations dashboard with dark navy sidebar and light neutral content area.
- Compact top bar with division/context, update metadata, simulation badge and user controls.
- Dense KPI row, operational panels, data tables, filters, tabs, timelines/corridor occupancy views, status chips and right-side detail drawers.
- Restrained typography, borders and status colours; no neon/glow aesthetic, oversized decorative cards, or emoji-based operational icons.
- Responsive sidebar/navigation and mobile table-card fallback without unnecessary horizontal scrolling.
- Visible data freshness and simulation-state indicators wherever data is synthetic or time-sensitive.

## 9. Realtime simulation requirements
When live railway systems are not connected, the MVP must run a realtime synthetic simulation layer so the interface behaves like a live operations workspace.

The simulator shall:
1. Emit structured events on a configurable interval (default 5–10 seconds).
2. Update related entities consistently rather than changing isolated values.
3. Recalculate affected KPIs, conflicts, corridor occupancy and plan status immediately.
4. Display `SIMULATION MODE` and the latest update time at all times during simulated operation.
5. Preserve source timestamps and expose freshness as `FRESH`, `STALE` or `UNKNOWN`.
6. Support a deterministic seed for repeatable scenarios and scenario controls for demo flows.
7. Never imply that synthetic events are connected to live railway systems.

### Required simulation event shape
```json
{
  "eventType":"TRAIN_MOVEMENT_UPDATED",
  "entityType":"TRAIN_MOVEMENT",
  "entityId":"TR-2048",
  "sourceSystem":"SIMULATOR",
  "sourceUpdatedAt":"2026-09-08T09:30:05Z",
  "receivedAt":"2026-09-08T09:30:05Z",
  "sequence":1842,
  "freshness":"FRESH",
  "payload":{
    "status":"DELAYED",
    "delayMinutes":18
  }
}
```

## 10. Dynamic replanning UX
A material simulated change must visibly propagate through the workflow: update affected data → mark the affected plan/constraint → show recalculation state → produce a revised recommendation → route it to human review. The UI must not silently change an approved schedule.
