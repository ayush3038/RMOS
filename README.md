RMOS — Railway Maintenance Optimization System

RMOS is a decision-support and optimization layer for Indian Railway maintenance block planning. It is designed to work alongside existing systems rather than replace them.

Smart India Hackathon

Problem Statement: 26027

Title: AI-Powered Automatic Block Planning to Maximize Asset Availability for Train Operations on Indian Railways

Organization: Ministry of Railways

Category: Software

Theme: Transportation & Logistics

Team: Team Paradox

College: VPP College

Team ID: SIH-DJBNLT

Core Idea

RMOS brings together maintenance demand, corridor/block availability and train-operation context; normalizes the data; prioritizes maintenance work; generates feasible and optimized block plans; explains the recommendation; and keeps final approval with authorized railway personnel.

Core pipeline

Existing Railway Data
→ Integration / Normalization / Freshness Checks
→ Asset Mapping
→ ML Priority / Risk Estimation
→ Safety & Rule Engine
→ OR-Tools CP-SAT Optimization
→ Recommended Block Plan
→ LLM Explanation / Summary
→ Human Review and Approval

Important system boundary

RMOS is not an autonomous railway block controller and is not a replacement for TMS, SMMS, TDMS, BDMS or COA. It is a coordination, visibility and optimization layer over existing workflows.

Development approach

Prototype first with synthetic but realistic structured data. Do not claim live railway integration unless an authorized integration is actually available and implemented.

Repository guide

PRD.md — product requirements and scope

ARCHITECTURE.md — system architecture and component boundaries

DATABASE.md — PostgreSQL schema and data model

API.md — REST API contract

TASKS.md — implementation roadmap

STYLEGUIDE.md — UI/UX and coding conventions

PROMPTS.md — prompts for AI-assisted development and structured LLM tasks

RULES.md — non-negotiable product, safety, data and engineering rules

Current baseline stack

Frontend: React + TypeScript + Tailwind CSS + shadcn/ui
Backend: Java + Spring Boot
Database: PostgreSQL + Spring Data JPA / Hibernate
Optimization: Google OR-Tools CP-SAT (Java)
AI/ML: pre-trained ML model + task-specific customization/tuning where supported
AI inference: NVIDIA NIM API
LLM: compatible NVIDIA-hosted model for explanation/summarization/operator queries
GIS: PostGIS + Leaflet
Realtime: WebSocket or Server-Sent Events
Security: Spring Security + JWT
Caching: Redis
Scheduling: Spring Scheduler
Observability: SLF4J + Logback + audit tables
Containerization: Docker
Testing: JUnit + Mockito + integration tests
Version control: Git + GitHub

Reference UI and Simulation Mode

The RMOS frontend must use the uploaded RAILSYNC prototype as the visual and interaction reference. Preserve its professional railway-operations character: dark navy sidebar, light neutral workspace, compact top bar, restrained status colours, dense operational tables, corridor/timeline visualizations, side drawers, filters, responsive navigation, and clear simulation labeling. Adapt the brand and domain content to RMOS; do not copy the RAILSYNC product identity. The reference prototype is explicitly a UI/UX reference, not a source of railway facts.

Required realtime demo behavior

The prototype must provide a realistic Simulation Mode when live railway integrations are unavailable. Synthetic data must change automatically at runtime instead of remaining static. Every update must follow the data-event format defined in API.md, including eventType, entityType, entityId, sourceSystem, sourceUpdatedAt, receivedAt, sequence, payload and freshness.

Recommended demo cadence: generate a simulation event every 5–10 seconds, with the interval configurable; recompute dependent dashboard values immediately; show the latest update time and freshness state; preserve a visible SIMULATION MODE badge; and keep all simulated values clearly labeled as synthetic. The simulation must be deterministic/reproducible when a seed is supplied, but time-varying during a live demo.

A simulator must produce realistic correlated changes rather than random independent numbers. For example, a train delay can update train status, affect corridor occupancy, create/revise a conflict, change block feasibility and trigger a replanning event. Maintenance task status, overdue days, corridor availability and block utilization should evolve within configured bounds. The simulator must never present generated values as real railway data.