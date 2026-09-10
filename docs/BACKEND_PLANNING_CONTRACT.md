# Backend Optimization & Planning Contract (Milestone 3.7)

## Purpose
This milestone establishes the API contract, abstraction boundary, and domain mapping for all RMOS maintenance planning. It mathematically bridges incoming Priority requests from Milestone 3.6 to the future structural Optimization engine (like Google OR-Tools CP-SAT).

**Important**: This establishes the *boundary*. It does NOT perform mathematically optimized block solving yet.

## The `PlanningOptimizer` Interface
The architecture revolves around the `PlanningOptimizer` interface.
- It accepts a `PlanningRequest` containing candidate tasks, constraints, and operational windows.
- It returns a `PlanningResult` with deterministic `PlanAssignment` layouts.

### Why Is OR-Tools Absent?
Engineering constraints require separating the business logic interface from strict external CP-SAT models. By delaying the concrete OR-Tools coupling:
1. We enforce an abstraction barrier preventing REST controllers from binding to Google tooling formats directly.
2. The testing pipeline (`PlanningArchitectureTest`) asserts zero specific optimization library dependencies leak into the generic Planning domains.

## Baseline Optimizer Strategy
Currently, requests route through a `DeterministicPlanningOptimizer`.
- It performs a simple pass evaluating priority scores descendingly.
- It attempts sequentially fitting blocks into the unconsumed window.
- It assigns `UNSCHEDULED` if parameters overflow capacity sequentially.
- **It asserts no optimality.** It is purely a fallback validation stub.

## Hard vs Soft Constraints
`PlanningConstraint` captures limits requested by operators.
- `hard = true`: Violating this constraint immediately marks the `PlanningResult` as `INFEASIBLE`. (E.g., 2 trains physically cannot occupy the identical corridor at `08:00`).
- `hard = false`: This introduces a penalty into the `objectiveScore` (once implemented in future milestones), preferring better alternatives but acting permissibly.

## Distinctions Across Milestones
- **Safety Validation (3.5)**: Are parameters structurally intact? 
- **Priority Assessment (3.6)**: Numerically scoring safe tasks against one another natively.
- **Planning (3.7 - Current)**: The contract describing *how* we interface mathematically with optimization bounds using prioritized inputs.
- **Optimization (Future)**: Activating CP-SAT math solving arrays under constraint.
- **AI Explanation (Future)**: Consuming the generated explanation from `PlanningResult` through generative LLMs to present user paths. 
- **Approval**: End user authorization executing back to physical railway endpoints.

No mock operational limits have been hallucinated. All defaults exist merely to facilitate software test bounds.
