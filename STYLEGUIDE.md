RMOS Style Guide

1. Product visual direction

RMOS should feel like professional railway operations software, not a futuristic AI demo.

Preferred:

clean white/light neutral surfaces

restrained navy/blue accents

clear typography

strong information hierarchy

functional tables, timelines and maps

subtle railway visual language

Avoid:

neon/glowing UI

excessive gradients

oversized rounded-card grids

emoji as primary icons

decorative AI/robot imagery

unnecessary animation

2. UI principles

Desktop-first for operational dashboards, but responsive.

Never introduce horizontal scrolling on normal pages.

Tables must remain readable and support filtering.

Use consistent spacing and alignment.

Critical statuses should be understandable by text plus icon, not color alone.

Show timestamps and freshness where data quality matters.

Make current/selected states obvious.

3. Typography

Use a modern sans-serif UI font. Prefer Inter or system sans-serif stacks.
Headings should be concise. Avoid all-caps body text.

4. Components

Use shadcn/ui primitives where possible. Prefer reusable components:

DataTable

StatusBadge

PriorityIndicator

FreshnessIndicator

Timeline

ApprovalPanel

ConflictList

AssetHealthCard

CorridorAvailabilityView

MapLayer

5. Icons

Use a consistent professional icon set such as Lucide. Do not mix unrelated icon styles.

6. Status language

Examples:

Fresh

Stale

Unknown

Critical

High

Medium

Low

Feasible

Conflict

Pending Approval

Approved

Modified

Rejected

7. Accessibility

Keyboard navigable controls.

Adequate contrast.

Visible focus states.

Labels for form fields.

Do not encode meaning only with color.

Provide readable explanations for optimization results.

8. Code conventions

TypeScript

Strict TypeScript.

Prefer typed API models.

Keep components small.

Avoid any unless explicitly justified.

Java

Java records/value objects where suitable.

Constructor injection.

Validate DTOs at API boundaries.

Keep controllers thin.

Put business logic in services/domain components.

Keep repositories focused on persistence.

Do not place solver logic in controllers.

9. Naming

Use domain terminology consistently: maintenance task, corridor, block, asset, planning run, recommendation, approval, conflict.

10. Empty/error/loading states

Every major data view must have loading, empty, stale-data and error states.

11. Reference UI fidelity

The uploaded RAILSYNC HTML is the visual reference for RMOS. Match its overall proportions, component density, hierarchy and interaction patterns rather than introducing a new visual concept. Reuse the same principles:

236px-class desktop sidebar with compact navigation and responsive collapse.

56px-class top bar with context, freshness/simulation status and utility actions.

Light grey page background with white panels and 1px borders.

Compact KPI cards, operational tables, timeline/corridor strips and drawers.

Small 5–6px radii, subtle shadows and minimal animation.

Navy primary actions, restrained green/amber/red status colours.

Professional inline SVG/Lucide-style icons; no emoji.

Do not copy RAILSYNC branding, wording or fake product identity; use RMOS names, domain terms and approved content.

12. Realtime simulation presentation

Always show SIMULATION MODE when synthetic data is active.

Show Updated <relative time> and, where useful, the exact event/source timestamp.

Use freshness labels: Fresh, Stale, Unknown.

Live changes should update in place with subtle transitions only.

Avoid flashing, pulsing, glowing or distracting motion.

Use compact event/update indicators rather than large live-feed panels.

When a change affects a recommendation, use a clear affected/pending-review state.

13. Simulation data display

Synthetic records should visibly identify their source as SIMULATOR or equivalent demo metadata. Example compact metadata:
Source: Simulator · Updated: 14:57:08 · Fresh