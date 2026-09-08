# RMOS Design System

This document outlines the visual principles extracted from the RAILSYNC prototype to ensure a consistent, professional railway operations experience in RMOS.

## Color System

The RMOS color palette is rooted in a professional, enterprise navy theme with deliberate, consistent semantic states. Neon, glowing, and arbitrary colors are strictly prohibited.

### Core Brands & Surfaces
- **Navy (Primary Brand):** `navy-900` (#0A1420), `navy-800` (#0F2138), `navy-700` (#173352), `navy-600` (#1F4570)
- **Backgrounds:** `paper` (#EEF1F4) for the main application workspace, `surface` (#FFFFFF) for cards, panels, and topbars.
- **Borders:** `border` (#DCE2E8), `border-soft` (#E7EBEF) for internal dividers.
- **Text:** `ink` (#111B26) for primary headings, `ink-soft` (#3B4856) for body text and table rows, `muted` (#6B7686) for labels, `faint` (#98A2AE) for disabled or subtle data.

### Semantic Status Colors
RMOS enforces a strict operational traffic-light status system. Colors must be paired with clear text labels to avoid conveying information by color alone.
- **Red (Critical/Danger/Delayed/Rejected):** `red` (#A6332C), `red-bg` (#FAEAE8), `red-line` (#E3B4AF).
- **Amber (High/Warning/Pending/Minor Delay/Modified):** `amber` (#A9661D), `amber-bg` (#FBF0DF), `amber-line` (#E7C48F).
- **Green (Completed/On Time/Approved):** `green` (#2A6E4E), `green-bg` (#E7F3EC), `green-line` (#AFD8C3).
- **Blue (Medium/Scheduled/Informational):** `blue` (#2A5C8A), `blue-bg` (#E9F1F8), `blue-line` (#B9D2E8).
- **Grey (Low/Postponed/Proposed):** `muted` text on `paper` background.

## Typography

- **Primary UI Font (Inter):** Used for all headings, body text, buttons, and navigation. Dense, highly legible.
- **Monospace Font (IBM Plex Mono):** Strictly used for Task IDs, block durations, timestamps (e.g., 02:00–04:00), and operational values (e.g., KPIs) to ensure alignment and emphasize data density.

### Hierarchy
- App font scale stays relatively small (`14px` base) to preserve the compact operational feel. Table headers and sub-labels often fall to `11px` or `12px` to fit dense data rows.

## Spacing, Borders, & Geometry

- **Radius:** Restrained. Standard component radius is `6px`. Avoid pills (unless specifically using semantic status chips) and excessive rounding.
- **Shadows:** Minimal. `shadow-sm` for cards, `shadow-md` for popovers and toasts. 
- **Application Shell:** Uses a dark `navy-900` sidebar for persistent navigation, contrasting heavily with the light `paper` operational workspace and `surface` topbar.

## Components & Conventions

### StatusChip
Status indicators use small, dense `Badge`-like chips (`padding: 2.5px 8px`, `border-radius: 20px`, `11px` font). A `5px` circular dot optionally prefixes the label to reinforce the status visually.

### Loading & Empty States
Empty states are centrally aligned within `surface` panels with `muted` text.

## Accessibility & Responsive Design

### Rules
- **No strict color dependence:** Provide badges/labels alongside colors (e.g., "Critical" text explicitly visible, not just a red dot).
- **Collapse Gracefully:** Dense desktop tables must collapse into stacking `record-cards` on mobile viewports.
- **No Unused Scrolling:** Panels should fit neatly into grid partitions (`grid-cols-6` for top-level KPIs, `grid-cols-2` for panels) without breaking out of viewport constraints horizontally.
