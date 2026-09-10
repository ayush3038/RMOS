# Backend RMOS CP-SAT Optimization Setup (Milestone 3.8)

## Purpose
This introduces the foundational prototype engine that computes concrete layout paths across disjoint block bounds targeting independent assets. The `ORToolsCpSatOptimizer` utilizes the official Google OR-Tools dependency configured explicitly strictly executing mathematically bound integer scaling mapping logic to avoid relying implicitly on LLMs for quantitative assertions.

## Model Bounds
- **Abstractions**: All parameters are bound strictly under `PlanningOptimizer` preventing controller/domain leaks. The service natively favors executing CP-SAT bindings over the structural fallback arrays originally integrated into `DeterministicPlanningOptimizer`.
- **IntVars and Variables**: `LocalDateTime` formats functionally decode sequentially across base `[0, MaxMinuteBound]` integer limits natively mapped to individual scheduling models maximizing scale limits deterministically.

## Constraints Implementations
This milestone only targets constraints mathematically inferable natively without executing hallucinated operational rules (e.g. tracking physical limits unknown structurally yet).
1. `addNoOverlap`: All assignments explicitly resolving the identical target `assetId` natively array exclusively to enforce sequential block intervals structurally.
2. Distinct configurations naturally optimize freely (e.g. Asset 1 and Asset 2 will optimize resolving concurrent identical start offsets safely since neither occupies matching physical resources relative to current dataset constraints).
3. Window clipping bounds reject unmapped parameters ensuring max task duration resolves within bounded request constraints. Unscheduled outputs execute gracefully natively dropping individual assignments instead of discarding overarching requests completely.

## Objective Hierarchies
Optimization paths sequentially value the following integer arrays across maximizing targets recursively:
1. Maximize Total Active Schedules (Presence * > 1,000,000 scaling)
2. Maximize High Initial Priorities 
3. Minimize Target Minute Offsets (Favoring earlier time allocation relative to constraints bounding identical scopes)

## Configuration
Application bounds explicitly isolate deep Google configs spanning natively across property defaults resolving seamlessly across Spring dependencies. 
`rmos.optimization.max-seconds: 5` limits search times returning closest optimized limits while searches fail evaluating optimal paths quickly avoiding API hang limits. `workers` executes parallel configurations successfully across JVM environments out of the box dynamically via `Loader.loadNativeLibraries()`.

## Core Takeaway Limits
This establishes the **first executable optimization prototype.** 
- It evaluates basic bounds, non-overlaps, earliest/latest finishes, mapping linear arrays seamlessly translating CP-SAT statuses cleanly (`OPTIMAL / FEASIBLE / INFEASIBLE`) directly bounding results.
- It is NOT an autonomous railway control tool.
- It explicitly dictates it DOES NOT modify scheduling endpoints or physically map database constraints. It expects approval and validations downstream across final implementations.
