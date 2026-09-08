# RMOS Project Rules

## Safety and product boundaries
1. Safety is the first planning priority.
2. Safety and applicable operational rules are hard constraints, not trade-off weights.
3. RMOS recommendations require human review/approval before execution.
4. RMOS must never be described as an autonomous railway block controller.
5. RMOS does not replace TMS, SMMS, TDMS, BDMS or COA.
6. Never claim a railway rule or workflow that has not been validated from an authoritative source or field evidence.

## AI rules
7. ML predicts/prioritizes; it does not own the final schedule.
8. OR-Tools/constraint optimization is responsible for scheduling.
9. LLMs are for explanation, summarization and authorized conversational access.
10. LLM output must not silently alter the schedule.
11. Structured LLM output must be schema-validated before downstream use.
12. Never claim custom LLM training when the implementation only uses inference or supported task-specific tuning/customization.

## Data rules
13. Prototype data may be synthetic but must be clearly labeled.
14. Never represent synthetic results as field results.
15. Preserve source_system and source_record_id.
16. Preserve source timestamps; do not replace them with processing timestamps.
17. Mark freshness explicitly.
18. Do not silently merge uncertain cross-division asset identities.
19. Use canonical asset IDs for internal joins.
20. Treat live integration as unavailable unless it is actually authorized and implemented.

## Research fidelity rules
21. Distinguish official PS requirements, online research, field findings, inference and proposed RMOS features.
22. The 25–30 minute Mumbai observation is context-specific field feedback, not a railway-wide threshold.
23. “Shadow block” remains a field-reported term requiring validation unless an authoritative definition is later obtained.
24. SMMS already has an AI-based failure-prediction feature described as in testing/learning mode; RMOS must not claim to introduce that capability as new.
25. The field-reported priority order is Safety → Train Impact → Criticality → Urgency → Duration → Workforce → remaining/other factors; it is not an official Ministry-wide weighting without further validation.

## Engineering rules
26. Controllers stay thin.
27. Business rules belong in services/domain components.
28. Optimization code stays isolated and testable.
29. Every meaningful planning/approval action is auditable.
30. Authorization is enforced server-side.
31. Use UTC timestamps internally and explicit timezone conversion in presentation.
32. Validate all API inputs.
33. Write tests for critical scheduling and approval logic.
34. Do not hard-code production optimization weights without validation.
35. Do not hide solver infeasibility; surface it clearly.

## UI rules
36. Professional operations-software aesthetic.
37. No excessive glow/neon effects.
38. No emoji as primary operational icons.
39. Avoid information-hiding card overload.
40. No unnecessary horizontal scrolling.


## Simulation/UI rules
41. The uploaded RAILSYNC prototype is the visual reference for RMOS UI structure and interaction patterns.
42. RMOS must adapt the reference UI to RMOS branding and validated domain terminology; do not claim the reference's sample data is real.
43. Synthetic realtime data must be explicitly labeled `SIMULATION MODE` / `SIMULATOR`.
44. Every simulation update must include event type, entity identity, source system, source timestamp, received timestamp, sequence and freshness.
45. Simulation events must be internally consistent and correlated across affected entities.
46. Synthetic updates must not bypass safety rules, authorization, approval or audit requirements.
47. Approved plans must never be silently altered by a realtime event; affected plans require recalculation and human review.
48. Default simulation cadence should be configurable and normally fall in the 5–10 second range for demos.
49. Realtime UI updates should be subtle and must not introduce distracting animation or horizontal overflow.
50. Do not persist simulation output as live source-of-truth data in a production environment.
