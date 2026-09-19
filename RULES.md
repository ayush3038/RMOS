RMOS Project Rules

Safety and product boundaries

Safety is the first planning priority.

Safety and applicable operational rules are hard constraints, not trade-off weights.

RMOS recommendations require human review/approval before execution.

RMOS must never be described as an autonomous railway block controller.

RMOS does not replace TMS, SMMS, TDMS, BDMS or COA.

Never claim a railway rule or workflow that has not been validated from an authoritative source or field evidence.

AI rules

ML predicts/prioritizes; it does not own the final schedule.

OR-Tools/constraint optimization is responsible for scheduling.

LLMs are for explanation, summarization and authorized conversational access.

LLM output must not silently alter the schedule.

Structured LLM output must be schema-validated before downstream use.

Never claim custom LLM training when the implementation only uses inference or supported task-specific tuning/customization.

Data rules

Prototype data may be synthetic but must be clearly labeled.

Never represent synthetic results as field results.

Preserve source_system and source_record_id.

Preserve source timestamps; do not replace them with processing timestamps.

Mark freshness explicitly.

Do not silently merge uncertain cross-division asset identities.

Use canonical asset IDs for internal joins.

Treat live integration as unavailable unless it is actually authorized and implemented.

Research fidelity rules

Distinguish official PS requirements, online research, field findings, inference and proposed RMOS features.

The 25–30 minute Mumbai observation is context-specific field feedback, not a railway-wide threshold.

“Shadow block” remains a field-reported term requiring validation unless an authoritative definition is later obtained.

SMMS already has an AI-based failure-prediction feature described as in testing/learning mode; RMOS must not claim to introduce that capability as new.

The field-reported priority order is Safety → Train Impact → Criticality → Urgency → Duration → Workforce → remaining/other factors; it is not an official Ministry-wide weighting without further validation.

Engineering rules

Controllers stay thin.

Business rules belong in services/domain components.

Optimization code stays isolated and testable.

Every meaningful planning/approval action is auditable.

Authorization is enforced server-side.

Use UTC timestamps internally and explicit timezone conversion in presentation.

Validate all API inputs.

Write tests for critical scheduling and approval logic.

Do not hard-code production optimization weights without validation.

Do not hide solver infeasibility; surface it clearly.

UI rules

Professional operations-software aesthetic.

No excessive glow/neon effects.

No emoji as primary operational icons.

Avoid information-hiding card overload.

No unnecessary horizontal scrolling.