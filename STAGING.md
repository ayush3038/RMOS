# RMOS Release Candidate & Staging Deployment Guide

## 1. Staging Environment Template
Deployments must provision the following environment parameters (No real secrets should ever be committed):

### DATABASE
- `SUPABASE_DB_URL`: `jdbc:postgresql://<db.id>.supabase.co:5432/postgres`
- `SUPABASE_DB_USERNAME`: `postgres`
- `SUPABASE_DB_PASSWORD`: `your-secure-password`

### SECURITY
- `JWT_SECRET`: `your-staging-secure-secret-key-32+bytes`
- `RMOS_CORS_ALLOWED_ORIGINS`: `https://staging.yourdomain.com`

### NVIDIA
- `RMOS_NIM_BASE_URL`: `https://integrate.api.nvidia.com/v1`
- `RMOS_NIM_MODEL`: `meta/llama-3.1-8b-instruct`
- `RMOS_NIM_API_KEY`: `nvapi-example-staging-key`

### FRONTEND
- `VITE_API_BASE_URL`: `https://api.staging.yourdomain.com/api/v1`

### APPLICATION
- `SPRING_PROFILES_ACTIVE`: `prod`
- `SERVER_PORT`: `8080`

## 2. Startup Procedure

**Database Startup Order:**
1. Supabase PostgreSQL accepts connections.
2. Backend starts up connecting to DB.
3. Flyway migrations evaluate and apply explicitly.
4. Hibernate validates the schema (`ddl-auto=validate`) silently preventing destructive overwrites.
5. Application healthy (`/actuator/health`).
6. Frontend connects.

**Backend Execution (Local validation):**
```bash
cd backend
.\mvnw.cmd clean test
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=prod
```

**Frontend Execution:**
```bash
cd frontend
npm run build
```

## 3. Health & Readiness
- **Endpoint**: `http://localhost:8080/actuator/health`
- **Output**: Stripped explicitly to basic `{ "status": "UP" }` suppressing environment mappings or credential disclosures securely.

## 4. Staging Smoke Test Plan
1. **Frontend Loads**: Navigate to staging UI; visual elements load.
2. **Backend Health**: `GET /actuator/health` returns `UP`.
3. **Authentication**: Form logs in successfully with valid credentials returning JWT.
4. **Authorized Map**: Dashboard elements load via APIs natively.
5. **Unauthorized Reject**: Attempted Admin call out of scope returns 403 Forbidden structurally natively.
6. **Integration Event Accept**: System posts Incident Map cleanly without exception.
7. **Incident Mapping Works**: AI/Domain logic merges details successfully forming contexts safely.
8. **Safety Validation**: Pre-checks evaluate block constraints structurally safely.
9. **Planning Engine**: API responds to CP-SAT constraint building seamlessly.
10. **CP-SAT Produces Candidate**: Optimization output candidate schedule successfully.
11. **Dynamic Replanning**: Subsequent overlapping events trigger distinct secondary resolution sets naturally.
12. **Human Decision**: Manual approval via specific endpoint executes updating Plan entity strictly.
13. **Audit**: Plan creation logs `PlanDecisionResult` structurally correctly securely.
14. **Simulator**: Marked clearly reading "Demo Injector".
15. **Data Display**: Frontend map pulls assets displaying real-time coordinate rendering structurally securely.

## 5. Canonical Staging Demo
- **Event**: `TRAIN_DELAY`
- **Seed**: `42L`
- **Flow**: `TRAIN_DELAY -> SIMULATOR -> IntegrationController -> Incident Mapping -> Planning Context -> CP-SAT / replanning -> Candidate plan -> Human review -> Audit` (Final schedules are naturally dynamic depending on DB bounds).

## 6. Authorization Smoke Test
- **VIEWER**: Read-only tracking. Attempting `POST /api/v1/plans` drops 403 securely natively.
- **PLANNER**: Can invoke `POST /api/v1/plans` retrieving predictions. Attempting `POST /api/v1/plans/decision` fails effectively natively.
- **REVIEWER**: Accesses `POST /api/v1/plans/decision` executing authorizations safely.
- **ADMIN**: Admin levels execute all standard overrides. Role model is untouched natively.

## 7. Simulator Deployment Safety
- Events triggered via UI always define `sourceSystem = SIMULATOR`. 
- Synthetic bounds do NOT cross-pollinate live Indian Railways systems natively organically cleanly. Real integrations must operate explicit authenticated non-Simulator boundaries.

## 8. NIM Staging Behavior
- **Available**: `IntegrationController` successfully categorizes text returning precise RAG domain mapping smoothly.
- **Unavailable/Timeout**: The engine executes standard explicit fallback returning default "High priority unclassified incident" mapping structures without fabricating or hallucinating LLM syntax explicitly.

## 9. Observability
- All standard Logs trace:
  - Startup successes, Flyway checks, Planning invocations organically visually tracing standard paths.
- Logs explicitly **do NOT contain**:
  - `application-prod.yml` variables, JWT payloads, DB Credentials, Headers explicitly.

## 10. Production / Staging Boundary
- **LOCAL**: H2 DB, `dev` profile, `VITE_API_BASE_URL=http://localhost:8080`, Simulator heavily used.
- **STAGING**: Supabase Postgres (isolated non-live DB instance), `prod` profile, CORS restricted exclusively to staging URLs. Simulator enabled to test functionality.
- **PRODUCTION**: Secure PostgreSQL clustered DB. True production traffic domain CORS matching. Simulator boundaries explicitly masked. No test APIs exposed.
