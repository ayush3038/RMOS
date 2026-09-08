# RMOS AI-Assisted Development Prompts

These prompts are intended for coding agents/LLMs. They do not authorize the model to invent railway rules, APIs or operational facts.

## 1. Build a feature
```text
You are working on RMOS, a Railway Maintenance Optimization System.
Read PRD.md, ARCHITECTURE.md, DATABASE.md, API.md, STYLEGUIDE.md and RULES.md before changing code.

Feature: <FEATURE>

Requirements:
1. Preserve existing domain boundaries.
2. Do not replace railway source systems; RMOS is a decision-support layer.
3. Keep safety constraints deterministic and server-side.
4. Write tests for the changed business logic.
5. Do not invent external railway API details.
6. Update API/database documentation if the contract changes.
7. Report files changed and any assumptions.
```

## 2. Build an optimization scenario
```text
Implement one RMOS block-planning scenario using synthetic data.

Inputs:
- maintenance tasks
- corridor windows
- train movements
- task duration
- workforce limits
- compatibility rules
- safety constraints

Use OR-Tools CP-SAT in Java.
Hard constraints must not be violated.
Objective should conceptually minimize train disruption, maintenance lateness, asset downtime, number of separate blocks and resource conflicts.
Do not invent final production weights; make them configurable.
Return infeasibility/conflict details when no feasible plan exists.
```

## 3. Generate an explanation
```text
You are an explanation layer for an already-computed RMOS schedule.
Never create or modify schedule decisions.
Summarize only the supplied structured facts.
Explain:
- selected window
- included tasks
- important constraints satisfied
- major trade-offs
- conflicts avoided
- data freshness warnings
Return strict JSON matching the supplied schema.
```

## 4. Summarize maintenance data
```text
Summarize the supplied maintenance records for an authorized railway reviewer.
Do not diagnose equipment beyond the provided data.
Do not invent asset health, urgency or failure causes.
Separate observed facts from model-derived scores.
Mention stale/unknown data explicitly.
```

## 5. Code review
```text
Review this RMOS change for:
- safety boundary violations
- scheduling decisions made by the LLM
- missing server-side authorization
- invalid assumptions about railway workflows
- incorrect timezone handling
- loss of source timestamps
- missing audit events
- poor handling of stale data
- missing tests
Return findings by severity and exact file/line when available.
```

## 6. Frontend refinement
```text
Refine the RMOS UI without changing functionality.
Maintain professional railway-operations styling.
Remove neon/glow styling and decorative AI effects.
Keep information dense but readable.
Ensure responsive behavior and zero unnecessary horizontal scrolling.
Use consistent Lucide icons and existing design tokens.
```


## 7. Frontend build prompt using the reference UI
```text
Build/refactor the RMOS frontend using the uploaded RAILSYNC HTML prototype as the visual and interaction reference.

Preserve its overall layout language:
- dark navy left sidebar
- compact white top bar
- light neutral page background
- white bordered operational panels
- dense KPI strip
- data tables with filters
- corridor/timeline visualizations
- right-side detail drawers
- restrained status chips and professional icons
- responsive navigation and mobile fallbacks

Adapt all branding and domain terminology to RMOS. Do not copy unsupported railway facts from the reference. Do not introduce neon, glow, excessive gradients or decorative AI imagery. Preserve existing functionality while improving maintainability through reusable typed components.
```

## 8. Realtime synthetic-data prompt
```text
Implement a realistic RMOS Simulation Mode for the prototype. Live railway integrations are unavailable, so synthetic data must evolve automatically during a demo.

Requirements:
1. Use a deterministic seed, but advance the simulated state over time.
2. Emit one structured event every configurable 5–10 seconds by default.
3. Use this exact envelope:
{
  eventType, entityType, entityId, sourceSystem, sourceUpdatedAt, receivedAt, sequence, freshness, payload
}
4. sourceSystem must be SIMULATOR for synthetic data.
5. Changes must be correlated across entities. Example: train delay → corridor occupancy change → conflict → planner impact → recalculation.
6. Update visible KPIs/tables/timelines in place without a page reload.
7. Recalculate only the affected projections where practical.
8. Show SIMULATION MODE, last update time and freshness.
9. Never make synthetic values look like live railway values.
10. Never let the simulator directly approve or execute a railway block.

Create scenario presets for normal operations, delayed train, urgent maintenance and multi-department coordination.
```

## 9. Simulation event generator review prompt
```text
Review the simulator for realism and consistency. Check that events are bounded, time-aware, correlated, reproducible with a seed, ordered by sequence, explicit about freshness, and unable to create impossible domain states. Check that a simulated event cannot bypass safety constraints, authorization, human approval or audit logging.
```
