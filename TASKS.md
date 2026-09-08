# RMOS Implementation Tasks

## Phase 0 — Repository setup
- [ ] Create Git repository.
- [ ] Add README, PRD, architecture, database, API, styleguide, prompts and rules.
- [ ] Create `/frontend`, `/backend`, `/data`, `/docs` directories.
- [ ] Add `.gitignore`, `.env.example`, Docker files and basic CI.

## Phase 1 — Backend foundation
- [ ] Create Spring Boot application.
- [ ] Configure PostgreSQL.
- [ ] Add Spring Data JPA / Hibernate.
- [ ] Add Flyway/Liquibase migrations.
- [ ] Add validation and global error handling.
- [ ] Add structured logging and audit service.

## Phase 2 — Data model
- [ ] Implement departments.
- [ ] Implement users/roles.
- [ ] Implement assets.
- [ ] Implement canonical asset-name mapping.
- [ ] Implement maintenance tasks.
- [ ] Implement asset health records.
- [ ] Implement corridor windows.
- [ ] Implement train movements.
- [ ] Implement planning runs and block plans.

## Phase 3 — Synthetic data
- [ ] Build realistic sample data for Engineering, Signalling and Traction.
- [ ] Add corridor availability.
- [ ] Add planned and delayed train movements.
- [ ] Add overdue and critical tasks.
- [ ] Add cross-division naming examples.
- [ ] Add stale-record examples.

## Phase 4 — Data integration layer
- [ ] Build source adapters.
- [ ] Normalize source records.
- [ ] Track source timestamps.
- [ ] Add freshness classification: FRESH / STALE / UNKNOWN.
- [ ] Implement canonical asset mapping.
- [ ] Add integration tests.

## Phase 5 — Frontend foundation
- [ ] Create React + TypeScript app.
- [ ] Configure Tailwind CSS + shadcn/ui.
- [ ] Build responsive shell/navigation.
- [ ] Build dashboard.
- [ ] Build dense data tables.
- [ ] Build maintenance detail.
- [ ] Build corridor visualization.
- [ ] Build asset map.

## Phase 6 — Prioritization
- [ ] Define validated feature schema.
- [ ] Build baseline rules-based score for deterministic fallback.
- [ ] Integrate ML priority/risk model.
- [ ] Display score drivers and data freshness.
- [ ] Test ranking stability.

## Phase 7 — Optimization
- [ ] Model hard safety constraints.
- [ ] Model train-conflict constraints.
- [ ] Model corridor availability.
- [ ] Model duration and workforce constraints.
- [ ] Model multi-department compatibility.
- [ ] Implement CP-SAT solver service.
- [ ] Return conflicts/infeasibility explanations.
- [ ] Add objective metrics.

## Phase 8 — Approval workflow
- [ ] Build review page.
- [ ] Add Approve / Modify / Reject / Cancel actions according to role.
- [ ] Persist approval decisions.
- [ ] Add audit history.

## Phase 9 — LLM / NIM
- [ ] Add provider abstraction.
- [ ] Add NVIDIA NIM configuration.
- [ ] Define JSON schema for explanation output.
- [ ] Add schedule summarization.
- [ ] Add maintenance report summarization.
- [ ] Add operator Q&A over authorized structured data.
- [ ] Validate and sanitize all model output.

## Phase 10 — Dynamic replanning
- [ ] Add event model.
- [ ] Detect material changes.
- [ ] Trigger affected-plan optimization.
- [ ] Preserve unaffected decisions when safe.
- [ ] Send revised plan to approval queue.

## Phase 11 — Testing
- [ ] Unit tests for services.
- [ ] Unit tests for scoring.
- [ ] Solver constraint tests.
- [ ] Integration tests for API + database.
- [ ] Frontend component tests.
- [ ] End-to-end demo scenarios.
- [ ] Regression scenarios with stale data and train delays.

## Phase 12 — Demo and evaluation
- [ ] Prepare one normal weekly planning scenario.
- [ ] Prepare one urgent-defect scenario.
- [ ] Prepare one multi-department combination scenario.
- [ ] Prepare one delayed-train replanning scenario.
- [ ] Capture metrics before/after optimization on the same synthetic dataset.
- [ ] Clearly label all metrics as prototype/simulation results.
- [ ] Prepare deployment documentation.


## Phase 13 — UI reference implementation and realtime simulation
- [ ] Recreate the RMOS app shell based on the uploaded RAILSYNC UI reference.
- [ ] Implement the dark navy sidebar, compact top bar, neutral workspace and status-chip system.
- [ ] Implement dashboard KPI strip, dense operational tables, filters, corridor/timeline views and right-side drawers.
- [ ] Ensure responsive behavior with mobile navigation and card fallback.
- [ ] Create typed simulator configuration and scenario schemas.
- [ ] Build stateful synthetic generators for maintenance tasks, trains, corridors, blocks and conflicts.
- [ ] Add deterministic seed support.
- [ ] Emit structured events every configurable 5–10 seconds by default.
- [ ] Add correlated scenario logic (e.g. train delay → corridor impact → conflict → replanning).
- [ ] Persist simulation event history separately from live/source records.
- [ ] Add WebSocket/SSE client handling with sequence ordering and stale-event protection.
- [ ] Add visible `SIMULATION MODE`, last-update timestamp and freshness indicators.
- [ ] Recalculate affected dashboard/planner projections without page reload.
- [ ] Add scenario controls: start, stop, reset and seed.
- [ ] Add demo scenarios for normal flow, urgent defect, delayed train and multi-department coordination.
- [ ] Verify that synthetic values never appear as field/live results.
