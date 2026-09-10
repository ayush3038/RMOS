# Backend Safety & Rules Engine Foundation (Milestone 3.5)

## Overview

The Safety & Rules Engine acts as an uncompromising deterministic gatekeeper. Before any optimization AI (such as CP-SAT or NVIDIA NIM logic) assesses schedules, or before an operator creates a block request, this local validation engine proves the request is structurally safe and properly defined. 

This engine is expressly **deterministic and explainable**. No LLMs, black boxes, or opaque heuristic searches execute here. It relies purely on the facts presented in a `SafetyValidationRequest`.

## HARD vs WARNING Semantics

The engine evaluates rules collectively but handles failures differently depending on severity.

### **HARD** Rules
These are inviolable constraints. 
- Example: "A maintenance block request must include a target Asset ID."
- If any HARD rule evaluates to a `FAILED` status, the entire `SafetyValidationResult` returns `valid = false`. The request cannot proceed to planning or execution.

### **WARNING** Rules
These flags are informational and contextual.
- Example: "The tracking dataset for the required asset was updated 25 hours ago."
- A WARNING notifies operators or downstream planners about degraded confidence or anomalies but does not inherently invalidate a request (`valid` remains `true` if all HARD rules pass).

## Why No Hardcoded Operational Thresholds?

In this milestone, the engine deliberately avoids encoding hard limits like "Blocks cannot exceed 30 minutes". Such thresholds belong to operational policy configurable per division/route by Indian Railways, and will likely be governed by data from the overarching constraints model. The backend framework validates structural presence rather than attempting to enforce dynamic physical limits prematurely.

## Relationship to Future Optimization & AI

1. **Safety Engine (Current Stage)**: Deterministic, programmatic validation. If it fails here, it is undeniably invalid.
2. **CP-SAT Optimizer (Future Stage)**: A mathematical scheduler. It will not attempt to optimize paths that the Safety Engine has already flagged as dangerously flawed or malformed.
3. **LLM/Generative Components (Future Stage)**: Will provide human-readable summaries or suggested mitigations for failing plans, but they *will not compute the pass/fail result*.
4. **Human Approval**: Remains purely out-of-band and subsequent to automated checks. An operator reviews everything even if the systems claim valid paths exist.

## Current Rules Implementations

The following initial strategy beams demonstrate the capability of the extensible `SafetyRule` interface:
1. `RequiredAssetRule`: (HARD) Validates asset identification.
2. `ValidMaintenanceDurationRule`: (HARD) Validates positive numerical durations.
3. `RequiredDepartmentRule`: (HARD) Validates responsible department declaration.
4. `DataFreshnessRule`: (WARNING) Leverages `DataFreshnessService` (from Milestone 3.4) to flag requests planned against outdated asset condition metrics.
