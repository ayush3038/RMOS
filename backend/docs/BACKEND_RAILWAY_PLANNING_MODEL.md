# Railway Planning Model & Operational Constraints Foundation (Milestone 3.9)

## Overview
This documentation describes the foundational integration of railway-specific planning logic into the RMOS domain model. It ensures the existing CP-SAT optimizer can work with scalable, operational realities (like section logic and block-planning closures) without encoding unsupported, arbitrary Indian Railways operational procedures into core logic directly.

## New Domain Contracts
The following lightweight planning-domain contracts were introduced to support these constraints predictably:

- **RailwaySection**: Identifies a railway section via `sectionId` and associates it with a specific `corridorId`. The entity tracks `active` state but explicitly refrains from establishing premature GIS persistence in this stage to adhere to non-invention boundaries.
- **MaintenanceBlockWindow**: Predicts a scheduling block bound by `windowStart` and `windowEnd` on a given `sectionId`. It leverages the `BlockWindowStatus` (`AVAILABLE`, `RESERVED`, `UNAVAILABLE`) to indicate if it can be securely utilized by our execution model.
- **TrainImpactProfile**: Models abstract train flow delays including `impactLevel`, `affectedTrainCount`, and `affectedServiceMinutes`. Currently reserved for future non-hardcast constraints and result explanations.
- **WorkforceProfile**: Allows CP-SAT injection of team density distribution against the given planning task by distinguishing `requiredTeams` vs `availableTeams`.

### Contextual Structuring
- **RailwayPlanningTask**: A derived sub-class of generic `PlanningTask` to encapsulate targeted associations to segments (`sectionId`), resource prerequisites (`workforceProfile`, `maintenanceBlockWindow`), and flow disruptions (`trainImpactProfile`).
- **RailwayPlanningContext**: Group boundaries aggregating valid properties (`sections`, `blockWindows`, general runtime scopes) necessary to structurally validate inbound API requests prior to invoking the CP-SAT engine.

## Validation & Evaluation Rules
The **RailwayPlanningValidationService** checks runtime contextual bindings strictly. For example, ensuring `blockWindows` do not overlap bounds provided by the parent request context, and rejecting task profiles matching an unknown `sectionId`.

**OperationalConstraintEvaluation** was introduced to act as an explainable artifact reporting why CP-SAT solver rejected scenarios. Handled scenarios map to **PlanningConstraintType**, tracking newly extended types: `SECTION_AVAILABILITY`, `BLOCK_WINDOW`, `WORKFORCE_CAPACITY`.

### Deferrals & Boundaries (Important)
This system extends CP-SAT optimization context deterministically. Constraints deferred to future integration or manual user authorization:
- Arbitrary maximum block durations.
- Unsubstantiated train-frequency constraints or automated dispatch models.
- Fabricated staffing models (distribution defaults must be supplied by API consumers directly matching real operations vs algorithms). 
- Automated clearance bindings to real TMS boundaries.

CP-SAT handles combinations safely; it does NOT replace ultimate human operating approvals structure.
