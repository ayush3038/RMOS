# Frontend Data Architecture

This document describes the design of the generic data boundaries in the RMOS Dashboard frontend. It acts as the source of truth for understanding how frontend components decouple from UI implementation and mock state, gracefully upgrading to a real backend in specific simulation modes.

## 1. Domain Models
Shared application models (e.g. `MaintenanceTask`, `CorridorSection`, `OperationalEvent`) reside in `frontend/src/types/models.ts`. 
- They contain exact semantic types needed by the domain without unnecessary backend specifics.
- All domain records maintain strongly typed status fields (`TaskStatus`, `AvailabilityStatus`, etc.).

## 2. API Contract & Repository Boundary
We employ the Repository pattern via a clean API abstraction:
- **`DataRepository`** (`frontend/src/services/api/repository.ts`): The generalized interface dictating exactly what UI structures need (e.g. `getDashboardKpis()`). 
- **`DemoDataRepository`**: Provides asynchronous `mockData` fetches simulating a real network round trip. This lets UI components react natively to Loading, Error, and Success states smoothly.

## 3. Realtime Boundary
Client-side integration for WebSockets / Server-Sent Events (SSE) exists in `frontend/src/services/realtime/client.ts`.
- Subscribes via typed semantic topics defined in `frontend/src/types/events.ts`.
- The `RealtimeEvent<T>` envelope tracks crucial fields like `eventId`, `timestamp`, `sourceSystem` and versioning parameters to process chronological messages effectively.

## 4. Simulation Mode & Data Environments
The frontend enforces a hardcoded context parameter defined in `frontend/src/config/dataMode.ts`: `DATA_MODE`.
- **`SIMULATION`**: Active default mode. Returns mock data explicitly and strictly labels the frontend environment to avoid operational confusion. 
- **`INTEGRATED`**: (Future) Activates `ApiClient` mappings targeting the OpenAPI specifications established in `API.md`. 

## 5. UI Fetching 
React components utilize lightweight generic fetching behaviors via `useData` hooks (`frontend/src/hooks/useData.ts`). This forces UI engineers to consider `isLoading` and `error` states automatically, preserving optimal user experiences without installing heavily opinionated caching libraries (like TanStack Query/Redux).
