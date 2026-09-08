# RMOS REST API Contract

Base path: `/api/v1`

## Authentication
`POST /auth/login`

Request:
```json
{"username":"planner","password":"***"}
```
Response:
```json
{"accessToken":"...","expiresIn":3600,"role":"PLANNER"}
```

## Dashboard
`GET /dashboard/summary`

Returns counts for active tasks, overdue tasks, available windows, planned blocks, conflicts and pending approvals.

## Maintenance
`GET /maintenance-tasks`
Query parameters:
- department
- status
- priority
- assetId
- from
- to
- overdue
- freshness

`GET /maintenance-tasks/{id}`

`POST /maintenance-tasks`

`PATCH /maintenance-tasks/{id}`

## Assets
`GET /assets`

`GET /assets/{id}`

`GET /assets/{id}/health`

`GET /assets/{id}/name-mappings`

## Corridor availability
`GET /corridors`

`GET /corridors/{corridorId}/windows?from=&to=`

## Train movement
`GET /train-movements?corridorId=&from=&to=`

## Planning
`POST /planning/runs`

Request:
```json
{
  "horizon":"WEEKLY",
  "from":"2026-09-14T00:00:00Z",
  "to":"2026-09-20T23:59:59Z",
  "departments":["ENGINEERING","SIGNALLING","TRACTION"]
}
```

`GET /planning/runs/{runId}`

`GET /planning/runs/{runId}/blocks`

`GET /planning/runs/{runId}/conflicts`

`POST /planning/runs/{runId}/replan`

## Block plans
`GET /block-plans/{id}`

`PATCH /block-plans/{id}` — only authorized editable fields.

## Approval
`POST /block-plans/{id}/approval`

Request:
```json
{"action":"APPROVE","comments":"Reviewed operational impact."}
```
Allowed actions depend on role and plan status.

## Explanations
`POST /block-plans/{id}/explanation`

Returns a structured summary, for example:
```json
{
  "summary":"Two compatible maintenance tasks were grouped in one available window.",
  "drivers":["safety","train impact","criticality"],
  "constraintsSatisfied":true,
  "warnings":[]
}
```

## Realtime
WebSocket: `/ws/planning`

Events:
- `TRAIN_MOVEMENT_UPDATED`
- `TASK_UPDATED`
- `BLOCK_AVAILABILITY_UPDATED`
- `PLAN_RECALC_STARTED`
- `PLAN_RECALC_COMPLETED`
- `APPROVAL_REQUIRED`

## Error format
```json
{
  "timestamp":"...",
  "status":400,
  "code":"VALIDATION_ERROR",
  "message":"Invalid maintenance duration",
  "details":[]
}
```

## API principles
- Version APIs under `/api/v1`.
- Validate all input server-side.
- Return stable error codes.
- Never let client-side role checks replace server authorization.
- Record audit events for planning and approval operations.


## Realtime simulation API
The demo environment exposes the same event contract that the frontend will consume when live integrations are unavailable.

### Simulation control
`POST /simulation/start`

Request:
```json
{
  "scenario":"DELAY_REPLAN",
  "seed":42,
  "intervalSeconds":8
}
```

`POST /simulation/stop`

`POST /simulation/reset`

`GET /simulation/status`

Returns the active scenario, seed, last emitted sequence, last update timestamp and current mode.

### WebSocket event envelope
WebSocket: `/ws/planning`

All realtime simulation updates should follow this envelope:
```json
{
  "eventType":"TRAIN_MOVEMENT_UPDATED",
  "entityType":"TRAIN_MOVEMENT",
  "entityId":"TR-2048",
  "sourceSystem":"SIMULATOR",
  "sourceUpdatedAt":"2026-09-08T09:30:05Z",
  "receivedAt":"2026-09-08T09:30:05Z",
  "sequence":1842,
  "freshness":"FRESH",
  "payload":{
    "status":"DELAYED",
    "delayMinutes":18
  }
}
```

Supported simulation event types should include at minimum:
- `TASK_UPDATED`
- `TRAIN_MOVEMENT_UPDATED`
- `BLOCK_AVAILABILITY_UPDATED`
- `CONFLICT_CREATED`
- `CONFLICT_RESOLVED`
- `PLAN_RECALC_STARTED`
- `PLAN_RECALC_COMPLETED`
- `APPROVAL_REQUIRED`

### Client behavior
- Apply events by `sequence`.
- Update visible tables/KPIs/timelines without a full-page reload.
- Display last-update time and freshness.
- Show a subtle live-update indicator, but do not use flashy animations.
- If an update invalidates a recommendation, mark it as affected and require recalculation/review rather than silently replacing an approved plan.
