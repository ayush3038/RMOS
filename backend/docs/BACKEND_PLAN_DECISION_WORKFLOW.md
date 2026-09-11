# Plan Decision Workflow

The RMOS Plan Decision Workflow ensures explicit human-in-the-loop oversight across generated CP-SAT optimization schedules. This protocol introduces a strict audit barrier protecting external systems from autonomous mutation.

## Human-in-the-Loop Protocol
Railway operations rely heavily upon deterministic safety modeling, risk assessments, and schedule optimization. However, before a generated `PlanningResult` enters the railway operational domain, a certified operator must actively `APPROVE`, `MODIFY`, or `REJECT` the schedule.

1. **NO Autonomous Execution**: 
   The RMOS engine *does not* distribute, command, or issue signals directly to TMS (Train Management System) or field devices. RMOS acts as an auxiliary decision-support apparatus. Operations are executed outside this system manually by the operator following approval.
2. **Current Identity Limiter**:
   For Milestone 3.13, API interactions provide a user-supplied `reviewerId`. Future scopes introduce full Spring Security parsing JWT definitions guaranteeing robust cryptographic identity tracing. 
3. **Plan Reference Versioning**:
   Every plan request binds strictly to `<planningId, planVersion>`. Modifications are not injected mutably into historical instances.

## Endpoints

### Create a Decision Request
`POST /api/plans/decisions`

Accepts `PlanReviewRequest`:
```json
{
  "planningId": "P-456",
  "planReference": { "planningId": "P-456", "planVersion": 1 },
  "reviewerId": "UID-101",
  "reviewerRole": "ADMIN",
  "action": "APPROVE",
  "comment": "Safe margins identified."
}
```
**Outcome**: If valid, logs an `PlanDecisionAudit` and returns `PlanDecisionResult`. 
*Note*: `REJECT` and `MODIFY` actions demand explicit structural justifications via comments.

### Fetch Decision Records
`GET /api/plans/decisions/{decisionId}` - Fetches atomic log details.

## Future Context
Modifications requested via RMOS will be routed to optimization loopback handlers generating incremented `planVersion` variations (Milestone 3.14). NIM explanations generated (Milestone 3.12) are directly coupled as the support literature advising this exact decision layer.
