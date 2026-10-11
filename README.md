RMOS — Railway Maintenance Optimization System

«AI-assisted maintenance planning, coordination and block optimization for Indian Railway operations»

Smart India Hackathon 2026 · Problem Statement 26027
Organization: Ministry of Railways
Theme: Transportation & Logistics
Team: Team Paradox
College: VPP College of Engineering
Team ID: 121356

---

📌 Overview

RMOS (Railway Maintenance Optimization System) is a railway-domain-aware decision-support and optimization platform for maintenance block planning.

RMOS brings together:

- Maintenance requirements
- Asset information
- Corridor and block availability
- Train-operation context
- Departmental requirements
- Safety and operational constraints

The platform transforms these inputs into validated and optimized maintenance block plans while preserving human authority over final decisions.

Core Pipeline

Existing Railway Data
        ↓
Integration & Normalization
        ↓
Freshness & Validation Checks
        ↓
Canonical Asset Mapping
        ↓
Risk & Priority Estimation
        ↓
Safety & Rule Validation
        ↓
CP-SAT Block Optimization
        ↓
Recommended Block Plan
        ↓
AI Explanation & Summary
        ↓
Human Review & Approval
        ↓
Execution & Monitoring

---

🎯 Problem Statement

Railway maintenance planning requires coordination between maintenance activities, available block windows, train operations, asset conditions, departmental requirements and safety constraints.

Fragmented information and changing operational conditions can make it difficult to identify suitable maintenance windows while minimizing conflicts with railway operations.

RMOS provides a unified planning layer that combines these inputs and generates feasible maintenance block recommendations through constraint-based optimization.

---

🚀 Objectives

- Consolidate maintenance and operational information into a unified planning context.
- Normalize asset identifiers across source systems.
- Assess maintenance priority and asset-related risk.
- Apply safety and operational constraints before optimization.
- Generate feasible maintenance block combinations.
- Support dynamic replanning when operational conditions change.
- Provide structured explanations for recommended plans.
- Preserve human authority over final approval.
- Maintain traceability through planning and decision audits.
- Provide a deterministic simulation environment for demonstrations and testing.

---

🧩 Core Modules

1. Data Integration & Normalization

Handles structured data received from existing railway systems and authorized sources.

Capabilities:

- API and file-based integration
- Data validation
- Freshness checks
- Data normalization
- Source-system tracking
- Provenance tracking

---

2. Canonical Asset Mapping

Different railway systems may use different identifiers for the same physical asset.

RMOS establishes a canonical asset identity:

Source System Asset ID
          ↓
Canonical Asset Mapping
          ↓
RMOS Asset ID
          ↓
Unified Planning Context

Uncertain mappings are retained for review rather than silently merged.

---

3. Risk & Priority Assessment

Maintenance activities are evaluated using available asset and task information.

The assessment layer provides:

- Asset health indicators
- Maintenance priority
- Failure-risk estimation
- Urgency assessment
- Criticality information

These outputs provide inputs to the downstream planning process.

---

4. Safety & Rule Engine

Safety and operational constraints are evaluated before optimization.

The rule layer handles:

- Safety constraints
- Operational restrictions
- Maintenance dependencies
- Resource constraints
- Time-window validation
- Conflict detection
- Feasibility validation

---

5. Block Optimization

The optimization engine uses Google OR-Tools CP-SAT to generate candidate maintenance block plans.

Optimization considers:

- Maintenance requirements
- Available block windows
- Train operations
- Asset constraints
- Resource requirements
- Departmental requests
- Conflicting activities

---

6. AI Explanation Layer

The AI layer provides structured explanations and summaries for planning results.

Supported capabilities include:

- Recommended-plan explanations
- Planning summaries
- Operator-oriented information
- Structured AI responses
- Explanation of planning outcomes

AI-generated explanations do not override safety constraints or authorization decisions.

---

7. Human Review & Approval

RMOS keeps final planning authority with authorized personnel.

Recommended Plan
       ↓
     Review
       ↓
 ┌─────┼─────────┐
 ↓     ↓         ↓
Approve Modify  Reject
       ↓
     Audit

---

8. Monitoring & Replanning

Operational changes can trigger updates to the planning context.

Examples include:

- Train delays
- Maintenance overruns
- Block unavailability
- Urgent maintenance requirements
- Multi-department coordination events

The updated context can be used for dynamic replanning.

---

🏗️ System Architecture

┌──────────────────────────────────────────────────┐
│                  RMOS Frontend                   │
│          React + TypeScript + Tailwind           │
│                    shadcn/ui                     │
└───────────────────────┬──────────────────────────┘
                        │
                 REST / Realtime
                        │
                        ▼
┌──────────────────────────────────────────────────┐
│               Spring Boot Backend                │
├──────────────────────────────────────────────────┤
│ Integration & Normalization                      │
│ Canonical Asset Mapping                          │
│ Risk & Priority Assessment                        │
│ Safety & Rule Validation                          │
│ CP-SAT Optimization                               │
│ Dynamic Replanning                                │
│ AI Explanation                                    │
│ Authentication & Authorization                    │
│ Audit & Decision Tracking                         │
│ Simulation Engine                                 │
└───────────────┬──────────────────┬───────────────┘
                │                  │
                ▼                  ▼
       ┌────────────────┐  ┌─────────────────────┐
       │   PostgreSQL   │  │   NVIDIA NIM / LLM  │
       │                │  │                     │
       │ pgvector       │  │ Explanation         │
       │ PostGIS        │  │ Summarization       │
       └────────────────┘  └─────────────────────┘

---

🔄 Planning Workflow

┌──────────────────────┐
│ Railway Data Sources │
└──────────┬───────────┘
           ↓
┌──────────────────────┐
│ Integration Layer    │
│ Validation + Freshness│
└──────────┬───────────┘
           ↓
┌──────────────────────┐
│ Canonical Asset      │
│ Mapping               │
└──────────┬───────────┘
           ↓
┌──────────────────────┐
│ Risk & Priority      │
│ Assessment            │
└──────────┬───────────┘
           ↓
┌──────────────────────┐
│ Safety & Rule Engine │
└──────────┬───────────┘
           ↓
┌──────────────────────┐
│ OR-Tools CP-SAT      │
│ Optimization          │
└──────────┬───────────┘
           ↓
┌──────────────────────┐
│ Candidate Block Plan │
└──────────┬───────────┘
           ↓
┌──────────────────────┐
│ AI Explanation       │
└──────────┬───────────┘
           ↓
┌──────────────────────┐
│ Human Review         │
│ Approve / Modify     │
└──────────┬───────────┘
           ↓
┌──────────────────────┐
│ Execution & Audit    │
└──────────────────────┘

---

🛡️ System Boundary

RMOS is a coordination, visibility, optimization and decision-support layer.

RMOS does not replace or autonomously control:

- TMS
- SMMS
- TDMS
- BDMS
- COA
- Existing railway operating systems
- Existing railway safety procedures
- Authorized railway personnel

Safety Principle

«ML estimates → Rules constrain → CP-SAT optimizes → AI explains → Humans authorize»

Risk estimation, safety validation, optimization and authorization remain separate stages.

---

🧪 Simulation Mode

RMOS includes a simulation environment for demonstrations, development and testing when live railway integrations are unavailable.

Simulation data is synthetic and remains explicitly separated from live railway data.

Simulation Characteristics

- Deterministic seed support
- Configurable event interval
- Runtime-changing operational state
- Scenario-based event generation
- Correlated operational changes
- Reproducible demonstrations
- Explicit simulation labeling

Supported Scenarios

NORMAL_OPERATIONS
TRAIN_DELAY
URGENT_MAINTENANCE
BLOCK_UNAVAILABLE
MAINTENANCE_OVERRUN
MULTI_DEPARTMENT_COORDINATION

Simulation Event Structure

Simulation events follow the RMOS integration event model:

eventType
entityType
entityId
sourceSystem
sourceUpdatedAt
receivedAt
sequence
payload
freshness

Example event flow:

Train Delay
    ↓
Train State Update
    ↓
Corridor Impact
    ↓
Conflict Detection
    ↓
Block Feasibility Change
    ↓
Replanning

---

🤖 AI & Optimization Separation

RMOS maintains clear boundaries between AI inference, optimization and authorization.

                    Operational Data
                          │
                          ▼
                 ┌─────────────────┐
                 │ Risk / Priority  │
                 │    Assessment    │
                 └────────┬────────┘
                          ↓
                 ┌─────────────────┐
                 │ Safety & Rules  │
                 │    Validation   │
                 └────────┬────────┘
                          ↓
                 ┌─────────────────┐
                 │  CP-SAT Solver  │
                 │   Optimization  │
                 └────────┬────────┘
                          ↓
                 ┌─────────────────┐
                 │ Candidate Plan  │
                 └────────┬────────┘
                          ↓
                 ┌─────────────────┐
                 │ AI Explanation  │
                 └────────┬────────┘
                          ↓
                 ┌─────────────────┐
                 │ Human Decision  │
                 └─────────────────┘

The optimization engine determines feasible candidate schedules under defined constraints. The AI layer provides explanation and summarization around supported planning outputs.

---

🛠️ Technology Stack

Layer| Technology
Frontend| React, TypeScript
UI| Tailwind CSS, shadcn/ui
Backend| Java, Spring Boot
API| REST
Database| PostgreSQL
ORM| Spring Data JPA, Hibernate
Migrations| Flyway
Vector Storage| pgvector
GIS| PostGIS, Leaflet
Optimization| Google OR-Tools CP-SAT
AI Inference| NVIDIA NIM API
Realtime| WebSocket / Server-Sent Events
Security| Spring Security, JWT, BCrypt
Scheduling| Spring Scheduler
Caching| Redis
Logging| SLF4J, Logback
Containerization| Docker
Testing| JUnit, Mockito, Integration Tests
Version Control| Git, GitHub

---

🔐 Security

RMOS incorporates application-level security through:

- Spring Security
- JWT authentication
- BCrypt password hashing
- Role-based authorization
- Audit logging
- Restricted CORS configuration
- Environment-based secrets
- Separation of credentials from source code

Authorization Roles

VIEWER
PLANNER
REVIEWER
ADMIN

Protected operations include planning, simulation control, decision recording and administrative functions.

---

🗄️ Database

RMOS uses PostgreSQL as its primary persistence layer.

Core database tables include:

assets
canonical_asset_mapping
maintenance_tasks
planning_runs
plan_assignments
plan_decision_audits
source_data_record
users
simulation_state
security_audit_events
knowledge_documents
knowledge_chunks
knowledge_embeddings
flyway_schema_history

Database migrations are managed through Flyway.

---

📡 API

Major API domains include:

/api/assets
/api/maintenance-tasks
/api/integration/asset-mappings
/api/integration/source-records
/api/v1/integration
/api/v1/auth
/api/planning
/api/priority
/api/asset-risk
/api/safety
/api/explanations
/api/plans/decisions
/api/v1/knowledge
/api/scenarios
/api/v1/simulation

Detailed API contracts are maintained in ""API.md"" (./API.md).

---

📊 Data Freshness & Provenance

RMOS associates source information with freshness and provenance metadata.

Supported freshness states include:

FRESH
STALE
UNKNOWN

Source identity, timestamps and mapping information are retained as part of the planning context.

Stale or invalid information is handled through validation and mapping rules rather than being silently treated as current operational data.

---

📁 Repository Structure

RMOS/
│
├── backend/
│   ├── src/
│   ├── pom.xml
│   ├── Dockerfile
│   └── .env.example
│
├── frontend/
│   ├── src/
│   ├── package.json
│   ├── Dockerfile
│   ├── nginx.conf
│   └── .env.example
│
├── data/
├── docs/
│
├── API.md
├── ARCHITECTURE.md
├── DATABASE.md
├── PRD.md
├── RULES.md
├── STAGING.md
├── STYLEGUIDE.md
├── TASKS.md
└── PROMPTS.md

---

🚀 Running the Project

Backend

cd backend
./mvnw clean test
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

Windows

cd backend
.\mvnw.cmd clean test
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev

Default backend endpoint:

http://localhost:8080

---

Frontend

cd frontend
npm install
npm run build
npm run dev

Default development endpoint:

http://localhost:5173

---

🐳 Docker

Backend

docker build -t rmos-backend ./backend

Frontend

docker build -t rmos-frontend ./frontend

Containerized deployment:

Frontend
   ↓
Nginx
   ↓
Spring Boot API
   ↓
PostgreSQL

---

⚙️ Configuration

Environment-specific configuration is maintained through environment variables.

Backend Configuration

SUPABASE_DB_URL
SUPABASE_DB_USERNAME
SUPABASE_DB_PASSWORD

JWT_SECRET
JWT_EXPIRATION_MS

RMOS_CORS_ALLOWED_ORIGINS

SERVER_PORT
SPRING_PROFILES_ACTIVE

SIMULATION_MODE
SIMULATION_INTERVAL_SEC

RMOS_NIM_BASE_URL
RMOS_NIM_MODEL
RMOS_NIM_API_KEY

Frontend Configuration

VITE_API_BASE_URL
VITE_REALTIME_ENDPOINT
VITE_APP_ENV
VITE_DATA_MODE
VITE_SIMULATION_MODE
VITE_SIMULATION_INTERVAL_SEC

Secrets and environment-specific credentials are excluded from source control.

---

🧪 Testing & Validation

Backend

cd backend
./mvnw clean test

Windows:

cd backend
.\mvnw.cmd clean test

Frontend

cd frontend
npm run build

Validation covers:

- REST API behavior
- Authentication
- Authorization
- Database persistence
- Flyway migrations
- Asset mapping
- Risk assessment
- Priority assessment
- Safety validation
- CP-SAT optimization
- Dynamic replanning
- Simulation scenarios
- Audit logging

---

📚 Documentation

Document| Description
""PRD.md"" (./PRD.md)| Product requirements and scope
""ARCHITECTURE.md"" (./ARCHITECTURE.md)| System architecture and component boundaries
""DATABASE.md"" (./DATABASE.md)| PostgreSQL schema and data model
""API.md"" (./API.md)| REST API contracts
""TASKS.md"" (./TASKS.md)| Development roadmap
""RULES.md"" (./RULES.md)| Product, safety, data and engineering rules
""STYLEGUIDE.md"" (./STYLEGUIDE.md)| UI/UX and coding conventions
""STAGING.md"" (./STAGING.md)| Staging and deployment configuration
""PROMPTS.md"" (./PROMPTS.md)| Structured AI-assisted development prompts

---

📌 Current Prototype Scope

The current RMOS implementation represents a research and demonstration prototype using synthetic railway data where authorized live railway integrations are unavailable.

The prototype covers:

- Railway maintenance planning
- Asset normalization
- Canonical asset mapping
- Risk and priority assessment
- Safety-first validation
- Constraint-based optimization
- Dynamic replanning architecture
- Human approval workflow
- Simulation-driven demonstrations
- Auditability and traceability
- AI-assisted explanation

Live railway-system integration requires authorized access to operational interfaces, validated data contracts and appropriate deployment approvals.

---

🏆 Smart India Hackathon 2026

| 
Problem Statement| 26027
Organization| Ministry of Railways
Theme| Transportation & Logistics
Project| Railway Maintenance Optimization System
Team| Team Paradox
College| VPP College of Engineering
Team ID| SIH-DJBNLT

---

👥 Team Paradox

Team Paradox
VPP College of Engineering
Smart India Hackathon 2026

---

License

This project is developed as part of the Smart India Hackathon 2026 project submission by Team Paradox, VPP College of Engineering.
