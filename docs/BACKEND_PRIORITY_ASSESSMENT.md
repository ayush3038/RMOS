# Backend Priority & Risk Assessment Foundation (Milestone 3.6)

## Purpose
The Maintenance Priority layer translates non-uniform domain attributes of a given engineering blocking request into a structured, universally comparable scalar (`priorityScore`) bounded between `[0.0, 1.0]`. 
It also outputs a human-readable categorical string representing this risk via `PriorityBand` (CRITICAL, HIGH, MEDIUM, LOW) to visually queue attention during scheduling workflows.

## Field Research Driven Factor Hierarchy
Field insights reveal that engineers roughly prioritize tasks using these factors in descending order:
1. **SAFETY** (E.g., derailed coach blocking main line)
2. **TRAIN_IMPACT** (E.g., number of passenger express trains halted)
3. **CRITICALITY** (E.g., high-speed diverging junction vs isolated siding)
4. **URGENCY** (E.g., immediate repair required vs 30-day scheduled maintenance)
5. **DURATION** (E.g., blocking the corridor for 12 hours vs 30 minutes)
6. **WORKFORCE** (E.g., heavy machinery and hundreds of trackmen vs singular inspection team)

**CRITICAL NOTE**: The exact descending hierarchy implemented here, and particularly the static weights mapping (e.g. `0.30` or `0.25`) are explicitly **baseline application defaults**. They are not sanctioned policy constants enforced by Indian Railways. Policy configurations can be safely overridden via `application.yml` without code modifications depending on division-specific policies.

## Deterministic Scoring Approach vs AI Evaluators
This is a mathematically deterministic, rule-based normalizer. 
- **WHY NOT ML?** Machine learning black-boxes are notorious for discarding predictability, breaking reproducibility mandates standard to safety-certified infrastructure. 
- **WHY NOT OR-TOOLS?** OR-Tools handles Constraint Programming for solving temporal/path optimization conflicts out of hundreds of already-scored tasks. It does not calculate the base score of a single isolated task.
- **HOW LLMS HELP**: Large language models perform textual summaries on constraint resolutions; they *should not act as the engine for hard priority calculation* due to hallucinations.

### The Algorithm
The backend calculates scoring linearly:
- Enumerated string values maps onto `[0.0, 1.0]` multiplier arrays (`normalizeImpact`, `normalizeSafety`, `normalizeWorkforce`).
- Integers like duration normalize predictably. `Math.min(duration/240.0, 1.0)`. (This does not mean maximum durations are 4 hours, only that blocks longer than 4 hours fully max out the duration risk penalty.)
- Multipliers are summed via dot product against their respectively defined configurable weights.
- The outcome is a sum, scaled between `[0.0, 1.0]`.

## Distinction from Safety Validation (Milestone 3.5)
- Safety validation dictates "Is this request structural and safe enough to even consider planning?" (Yes/No).
- Priority Assessment dictates "Given this request is safe, how critical is resolving it compared to others?" (Scalar priority).

Human approval remains firmly outside of this phase. Humans visually consume this priority outcome before approving or reorganizing final scheduling runs.
