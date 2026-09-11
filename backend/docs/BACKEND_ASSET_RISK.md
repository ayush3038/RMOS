# AI/ML Asset Health & Risk Estimation Foundation (Milestone 3.11)

## Overview
The Asset Risk abstraction layer provides a formal inference boundary for estimating asset health and maintenance risk. This layer is designed as a foundational decision-support input for the priority assessment framework.

> [!IMPORTANT]
> RMOS does not claim to invent or replace official failure prediction algorithms. Project research confirms that systems like SMMS already utilize trained failure-prediction capabilities. *This specific layer serves purely as a structured RMOS planning heuristic* to bridge raw asset inputs toward future integration with either real native ML runtimes (such as PyTorch/TensorFlow backed microservices), NVIDIA NIM endpoints, or proprietary IR decision components.

## Core Implementations

### Input Features
The `AssetRiskInput` DTO normalizes data feeds, mandating the following non-negative, structured telemetry dimensions:
- `maintenanceEventCount`
- `overdueTaskCount`
- `failureCount`
- `recentFailureCount`
- `recentMaintenanceCount`
- `daysSinceLastMaintenance`
- `criticalMaintenanceCount`

### Deterministic Baseline
During Milestone 3.11, the `AssetRiskModel` contract is backed by a `DeterministicAssetRiskModel`. This baseline evaluates feature proportions deterministically weighted by normalized `min(val, 1.0)` ratios.

**Formula Weights (Configurable in `application.yml` via `AssetRiskProperties`):**
- Overdue maintenance: `0.20`
- Lifetime failure history: `0.25`
- Recent failure velocity: `0.25`
- Maintenance recency (staleness): `0.15`
- Critical maintenance counts: `0.15`

### Bounded Outputs
Responses manifest as structured `AssetRiskPrediction` units:
- **Risk Score:** Clamped rigorously to `[0.0, 1.0]`.
- **Health Score:** Explicitly mapped as `1.0 - riskScore`.
- **Confidence Layer:** For the baseline model, this evaluates *feature completeness* (e.g. 5/5 variables populated = 1.0). When real probabilistic models are deployed, this may pivot to latent statistical entropy definitions.
- **Risk Bands:** A standard `[VERY_LOW, LOW, MEDIUM, HIGH, VERY_HIGH]` categorization layer evaluated off RMOS thresholds.

## Decoupling Constraints
- The `AssetRiskAssessmentService` isolates all backend subsystems from model implementation awareness.
- A programmatic architectural contract (`AssetRiskArchitectureTest`) actively prevents ML abstraction logic from adopting web layer hooks, persistence mapping (`@Entity`), or OR-Tools logic. 
- Execution endpoints (e.g., `POST /api/asset-risk/assess`) remain stateless analytical channels devoid of operational side effects.
