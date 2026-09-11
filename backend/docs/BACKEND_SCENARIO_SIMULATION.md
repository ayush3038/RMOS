# Scenario Simulation Engine (Milestone 3.10)

## Overview
The RMOS Scenario Simulation Engine provides a "WHAT-IF" analysis layer. It enables planners to deterministically test multiple configurations (e.g., alternative block windows, adjusted asset priorities, modified workforce constraints) and evaluate the projected outcomes without modifying live railway systems or production records.

This module acts exclusively as an analytical sandbox.

## Key Concepts

### Scenario Model
A `ScenarioRequest` encapsulates a full standard `PlanningRequest` alongside metadata (`ScenarioObjectiveProfile`). It represents a discrete "What-If" boundary.
- **Independence:** The simulation encapsulates and evaluates contexts completely isolated from production state.
- **Evaluation:** The simulation explicitly reuses the `ORToolsCpSatOptimizer`. **No new optimization solvers were introduced**. We evaluate alternatives against the same reliable, bounded constraint engine.

### Scenario Comparison Logic
To choose a "Best Scenario" systematically among multiple options, we employ a **strictly deterministic, default, multi-stage fallback ranking pipeline**:
1. Maximize Scheduled Tasks
2. Maximize Scheduled Priority Sum (where task counts are equal)
3. Minimize Unscheduled Tasks
4. Minimize Total Train Impact
5. Minimize Total Workforce Load
6. Lexicographical default on `scenarioId`

*Note on Indian Railways Policy*: This comparison heuristic is a functional software default for dashboard comparisons. It does not codify specific, unalterable commercial rules or operations policies.

## Architectural Boundaries (CRITICAL STRICTURES)
- **No Persistence:** Scenarios are computationally evaluated dynamically. Saving and auditing scenarios can be implemented independently when lifecycle workflows are formalized.
- **Non-Execution:** The Scenario API (`/api/scenarios/simulate` and `/api/scenarios/compare`) NEVER issues maintenance blocks, mutates underlying DB records, or interfaces with external systems.
- **No AI / ML:** "Best scenario" determinations are exact, transparent algorithms avoiding arbitrary black-box LLM estimations or hallucinations.

## Future Potential
Currently, the `ScenarioObjectiveProfile` acts as descriptive metadata. Later enhancements may inject these boolean properties organically into the CP-SAT's native objective function, allowing optimized permutations to favor specific domains (e.g. maximizing priority over maximizing task density natively).
