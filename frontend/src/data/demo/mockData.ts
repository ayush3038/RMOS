import type {
    Asset,
    BlockWindow,
    Conflict,
    CorridorSection,
    DashboardKpis,
    Department,
    DepartmentRequest,
    MaintenanceTask,
    OperationalEvent,
    ReportItem,
    Train
} from "@/types/models";

/* ------------------------------------------------------------------ *
 * Synthetic dataset for RMOS demonstration mode.
 * Everything below is simulated and clearly marked as such.
 * Generation is deterministic (seeded) so the demo stays stable.
 * ------------------------------------------------------------------ */

const NOW = Date.now();
const MIN = 60_000;
const HOUR = 3_600_000;
const DAY = 86_400_000;

const isoAt = (offsetMs: number) => new Date(NOW + offsetMs).toISOString();
const pad2 = (n: number) => String(n).padStart(2, "0");
const ymd = (date: Date) =>
    `${date.getFullYear()}-${pad2(date.getMonth() + 1)}-${pad2(date.getDate())}`;
const hhmm = (date: Date) => `${pad2(date.getHours())}:${pad2(date.getMinutes())}`;

/** Deterministic value selection keeps the synthetic dataset stable. */
const pick = <T,>(items: readonly T[], index: number): T =>
    items[Math.abs(index) % items.length];

/* ---------------------------- Corridor sections ---------------------------- */

interface SectionSeed {
    id: string;
    corridor: string;
    line: string;
    status: CorridorSection["availabilityStatus"];
    currentTrains: number;
    capacityTrains: number;
    lengthKm: number;
    speedLimitKph: number;
    maintenanceCode?: string;
}

const SECTION_SEEDS: SectionSeed[] = [
    { id: "A10", corridor: "Western Corridor", line: "Line W1", status: "available", currentTrains: 4, capacityTrains: 6, lengthKm: 12.4, speedLimitKph: 130 },
    { id: "A11", corridor: "Western Corridor", line: "Line W1", status: "restricted", currentTrains: 2, capacityTrains: 6, lengthKm: 8.7, speedLimitKph: 80, maintenanceCode: "ENG-095" },
    { id: "A12", corridor: "Western Corridor", line: "Line W1", status: "maintenance", currentTrains: 0, capacityTrains: 5, lengthKm: 6.2, speedLimitKph: 100, maintenanceCode: "ENG-092" },
    { id: "A13", corridor: "Western Corridor", line: "Line W1", status: "blocked", currentTrains: 0, capacityTrains: 5, lengthKm: 4.9, speedLimitKph: 100, maintenanceCode: "SNT-046" },
    { id: "A14", corridor: "Western Corridor", line: "Line W2", status: "available", currentTrains: 5, capacityTrains: 7, lengthKm: 15.1, speedLimitKph: 130 },
    { id: "A15", corridor: "Western Corridor", line: "Line W2", status: "available", currentTrains: 3, capacityTrains: 6, lengthKm: 9.8, speedLimitKph: 120 },
    { id: "B02", corridor: "Central Corridor", line: "Line C1", status: "maintenance", currentTrains: 0, capacityTrains: 5, lengthKm: 5.5, speedLimitKph: 90, maintenanceCode: "TRC-105" },
    { id: "B04", corridor: "Central Corridor", line: "Line C1", status: "available", currentTrains: 4, capacityTrains: 6, lengthKm: 11.2, speedLimitKph: 120 },
    { id: "B05", corridor: "Central Corridor", line: "Line C1", status: "restricted", currentTrains: 3, capacityTrains: 6, lengthKm: 7.4, speedLimitKph: 75, maintenanceCode: "ENG-096" },
    { id: "B06", corridor: "Central Corridor", line: "Line C2", status: "available", currentTrains: 6, capacityTrains: 7, lengthKm: 13.6, speedLimitKph: 130 },
    { id: "B07", corridor: "Central Corridor", line: "Line C2", status: "available", currentTrains: 2, capacityTrains: 5, lengthKm: 6.9, speedLimitKph: 110 },
    { id: "B09", corridor: "Central Corridor", line: "Line C2", status: "maintenance", currentTrains: 0, capacityTrains: 5, lengthKm: 4.2, speedLimitKph: 90, maintenanceCode: "SNT-047" },
    { id: "C18", corridor: "Harbour Corridor", line: "Line H1", status: "available", currentTrains: 3, capacityTrains: 5, lengthKm: 8.1, speedLimitKph: 100 },
    { id: "C19", corridor: "Harbour Corridor", line: "Line H1", status: "restricted", currentTrains: 2, capacityTrains: 5, lengthKm: 5.8, speedLimitKph: 70, maintenanceCode: "ENG-093" },
    { id: "C20", corridor: "Harbour Corridor", line: "Line H1", status: "available", currentTrains: 4, capacityTrains: 6, lengthKm: 10.3, speedLimitKph: 110 },
    { id: "C21", corridor: "Harbour Corridor", line: "Line H2", status: "blocked", currentTrains: 0, capacityTrains: 4, lengthKm: 3.7, speedLimitKph: 60, maintenanceCode: "SFY-018" },
    { id: "C22", corridor: "Harbour Corridor", line: "Line H2", status: "maintenance", currentTrains: 0, capacityTrains: 5, lengthKm: 6.6, speedLimitKph: 95, maintenanceCode: "TRC-106" },
    { id: "C23", corridor: "Harbour Corridor", line: "Line H2", status: "available", currentTrains: 5, capacityTrains: 6, lengthKm: 12.9, speedLimitKph: 125 }
];

export const DEMO_SECTIONS: CorridorSection[] = SECTION_SEEDS.map((seed, index) => ({
    id: seed.id,
    name: `Section ${seed.id}`,
    corridor: seed.corridor,
    line: seed.line,
    availabilityStatus: seed.status,
    currentTrains: seed.currentTrains,
    capacityTrains: seed.capacityTrains,
    utilizationPercent: Math.round((seed.currentTrains / seed.capacityTrains) * 100),
    lengthKm: seed.lengthKm,
    speedLimitKph: seed.speedLimitKph,
    upcomingMaintenanceCode: seed.maintenanceCode,
    nextWindowStart: seed.maintenanceCode
        ? new Date(NOW + (index % 5) * DAY + 6 * HOUR).toISOString()
        : undefined,
    lastInspectionDate: ymd(new Date(NOW - ((index % 9) + 1) * 6 * DAY)),
    sourceSystem: "CORRIDOR-OPS-DEMO",
    sourceRecordId: `SEC-${seed.id}`,
    sourceTimestamp: isoAt(-((index % 7) + 1) * 14 * MIN),
    isSimulation: true
}));

export const SECTION_IDS = SECTION_SEEDS.map((seed) => seed.id);

export const sectionLabel = (sectionId: string): string => {
    const section = DEMO_SECTIONS.find((entry) => entry.id === sectionId);
    return section ? section.name : sectionId;
};

/* ------------------------------- Tasks ---------------------------------- */

const TASK_PREFIX: Record<Department, string> = {
    Engineering: "ENG",
    Traction: "TRC",
    "S&T": "SNT",
    Operations: "OPS",
    Mechanical: "MEC",
    Safety: "SFY"
};

const ASSETS_BY_DEPARTMENT: Record<Department, string[]> = {
    Engineering: ["Track", "Bridge", "Formation", "Point & Crossing", "Culvert", "Platform Edge"],
    Traction: ["OHE", "Feeder Column", "Traction Substation", "Pantograph Zone", "Return Circuit"],
    "S&T": ["Signal Point", "Interlocking", "Track Circuit", "Axle Counter", "Telecom Cable"],
    Operations: ["Station Equipment", "Water Column", "Signage", "Platform Shelter", "Yard Point"],
    Mechanical: ["Bogie", "Brake System", "Coupler", "Rake Coach", "Draft Gear"],
    Safety: ["Level Crossing", "Perimeter Fencing", "Warning Signage", "Fire Panel", "Escape Ramp"]
};

const CREWS_BY_DEPARTMENT: Record<Department, string[]> = {
    Engineering: ["Gang Alpha", "Gang Bravo", "Bridge Team 1", "Track Team 4"],
    Traction: ["OHE Gang 3", "OHE Gang 7", "PSI Team 2"],
    "S&T": ["S&T Team 2", "Relay Team 1", "Cable Gang 5"],
    Operations: ["Station Team 1", "Yard Team 3"],
    Mechanical: ["Rake Team 5", "Bogie Team 2"],
    Safety: ["Safety Squad 1", "Audit Team 2"]
};

const DEPARTMENTS: Department[] = [
    "Engineering",
    "Traction",
    "S&T",
    "Operations",
    "Mechanical",
    "Safety"
];

const SOURCE_SYSTEMS = ["MMS-Demo", "ICMS-Demo", "TMS-Demo", "SMMS-Demo", "BDMS-Demo"];

const CRITICALITY_POOL: MaintenanceTask["criticality"][] = [
    "critical", "critical", "high", "high", "high", "medium", "medium", "medium", "low", "low"
];

const URGENCY_POOL: MaintenanceTask["urgency"][] = [
    "critical", "high", "high", "medium", "medium", "medium", "low", "low"
];

const STATUS_POOL: MaintenanceTask["status"][] = [
    "pending", "pending", "pending", "pending", "pending",
    "scheduled", "scheduled", "scheduled",
    "in-progress", "in-progress",
    "completed", "completed"
];

const TASK_NOTES = [
    "Possession coordinated with section controller.",
    "Requires detailed engineering inspection before release.",
    "Material staged at nearest depot.",
    "Requires clearance from operating department.",
    "Night shift preferred to avoid passenger window.",
    "Cross-department dependency recorded.",
    "Vendor supervision required during execution.",
    undefined
];

const taskCounter: Record<string, number> = {};

export const DEMO_TASKS: MaintenanceTask[] = Array.from({ length: 48 }, (_, index) => {
    const department = pick(DEPARTMENTS, index + (index % 5));
    const prefix = TASK_PREFIX[department];
    taskCounter[prefix] = (taskCounter[prefix] ?? 100) + 1;
    const sectionId = pick(SECTION_IDS, index * 3 + 1);
    const criticality = pick(CRITICALITY_POOL, index);
    const status = pick(STATUS_POOL, index * 2);
    const dueOffset =
        index % 7 === 0
            ? -(1 + (index % 3)) * DAY + 4 * HOUR // overdue work
            : (index % 14) * DAY + (index % 9) * HOUR;
    const durationMinutes = pick([45, 60, 90, 120, 150, 180, 240, 300, 360], index * 5);
    const progress =
        status === "completed"
            ? 100
            : status === "in-progress"
              ? 25 + ((index * 13) % 60)
              : status === "scheduled"
                ? 0
                : 0;

    return {
        id: `task-${index + 1}`,
        taskCode: `${prefix}-${taskCounter[prefix]}`,
        department,
        assetType: pick(ASSETS_BY_DEPARTMENT[department], index),
        sectionId,
        criticality,
        urgency: pick(URGENCY_POOL, index + 3),
        dueDate: isoAt(dueOffset),
        durationMinutes,
        status,
        assignedCrew: pick(CREWS_BY_DEPARTMENT[department], index),
        workOrder: `WO-${2026}${pad2((index % 12) + 1)}-${pad2(index + 1)}${index % 10}`,
        progressPercent: progress,
        priorityRank: index + 1,
        workforce: 2 + (index % 7),
        estimatedCostInr: (2 + (index % 11)) * 45_000,
        dependencies:
            index % 6 === 0 && index > 5
                ? [`WO-${2026}0${(index % 9) + 1}-${pad2(index - 4)}${(index - 4) % 10}`]
                : undefined,
        notes: pick(TASK_NOTES, index),
        sourceSystem: pick(SOURCE_SYSTEMS, index),
        sourceRecordId: `SRC-${prefix}-${1000 + index}`,
        sourceTimestamp: isoAt(-((index % 48) + 1) * 37 * MIN),
        isSimulation: true
    };
});

/* ------------------------- Department requests --------------------------- */

const REQUEST_ACTIVITIES: Record<Department, string[]> = {
    Engineering: [
        "Track geometry correction",
        "Rails replacement in worn patch",
        "Bridge bearing inspection",
        "Formation de-watering work",
        "Point & crossing rail grinding"
    ],
    Traction: [
        "OHE tensioning check",
        "Feeder column replacement",
        "Contact wire height measurement",
        "Traction substation breaker service",
        "Return circuit resistance test"
    ],
    "S&T": [
        "Signal relay replacement",
        "Interlocking logic verification",
        "Track circuit alignment",
        "Axle counter drop-detector check",
        "Telecom cable jointing"
    ],
    Operations: [
        "Platform resurfacing work",
        "Yard shunting suspension request",
        "Station signage renewal",
        "Water column maintenance",
        "Coach watering schedule change"
    ],
    Mechanical: [
        "Bogie weld inspection",
        "Brake pad replacement",
        "Coupler overhaul",
        "Rake coach door repair",
        "Draft gear change-out"
    ],
    Safety: [
        "Level crossing boom renewal",
        "Perimeter fencing repair",
        "Fire panel sensor replacement",
        "Escape ramp clearance check",
        "Warning signage replacement"
    ]
};

const REQUEST_STATUSES: DepartmentRequest["status"][] = [
    "proposed", "proposed", "proposed", "proposed",
    "approved", "approved", "approved",
    "modified", "rejected"
];

const REVIEWERS = ["A. Menon (DMM)", "R. Kulkarni (Sr.DSTE)", "S. Iyer (DOM)", "P. Sharma (DME)"];

export const DEMO_DEPARTMENT_REQUESTS: DepartmentRequest[] = Array.from(
    { length: 24 },
    (_, index) => {
        const department = pick(DEPARTMENTS, index * 2);
        const startHour = 1 + (index % 5);
        const startMinute = index % 2 === 0 ? 0 : 30;
        const durationMinutes = pick([60, 90, 120, 150, 180], index);
        const start = new Date(2026, 0, 1, startHour, startMinute);
        const end = new Date(start.getTime() + durationMinutes * MIN);
        const requestedDate = ymd(new Date(NOW + ((index % 10) - 3) * DAY));

        return {
            id: `req-${index + 1}`,
            department,
            activity: pick(REQUEST_ACTIVITIES[department], index),
            sectionId: pick(SECTION_IDS, index * 5 + 2),
            requestedWindowStart: hhmm(start),
            requestedWindowEnd: hhmm(end),
            requestedDate,
            status: pick(REQUEST_STATUSES, index * 3),
            priority: pick(CRITICALITY_POOL, index + 1),
            requestedBy: pick(["L. Fernandes", "K. Bhat", "M. Nair", "D. Patil", "V. Rao", "T. Ghosh"], index),
            workforce: 2 + (index % 8),
            durationMinutes,
            reviewNote:
                index % 4 === 0
                    ? "Shifted by 30 min to protect express path."
                    : index % 7 === 0
                      ? "Rejected: clashes with sanctioned block."
                      : undefined,
            sourceSystem: "REQ-HUB-Demo",
            sourceRecordId: `REQ-${2026}${pad2((index % 12) + 1)}${pad2((index % 27) + 1)}`,
            sourceTimestamp: isoAt(-((index % 30) + 1) * 53 * MIN),
            isSimulation: true
        };
    }
);

/* ------------------------------- Conflicts ------------------------------- */

const CONFLICT_SEEDS: Array<Omit<Conflict, "id" | "detectedAt" | "sourceSystem" | "sourceRecordId" | "sourceTimestamp" | "isSimulation">> = [
    {
        category: "Block Conflict",
        severity: "critical",
        status: "open",
        title: "Overlapping block request in A12",
        description: "ENG-092 and TRC-104 request overlapping possession windows on Section A12 within the same night block.",
        recommendedAction: "Merge into a single integrated block covering 02:00-05:00.",
        ruleId: "RULE-BLK-001",
        sectionIds: ["A12"],
        affectedTrains: 4
    },
    {
        category: "Train Conflict",
        severity: "high",
        status: "open",
        title: "Express path clashes with SNT-047 window",
        description: "Vande Bharat Express scheduled path intersects the proposed signal relay window on Section B09.",
        recommendedAction: "Shift the maintenance window by 30 minutes after the express path.",
        ruleId: "RULE-TRN-004",
        sectionIds: ["B09"],
        affectedTrains: 2
    },
    {
        category: "Resource Conflict",
        severity: "medium",
        status: "acknowledged",
        title: "Tower wagon scarcity across division",
        description: "Four traction tasks require tower wagons concurrently but only two are available in the division.",
        recommendedAction: "Reschedule TRC-106 by one day or requisition an additional tower wagon.",
        ruleId: "RULE-RES-002",
        sectionIds: ["C22", "B02"],
        affectedTrains: 0
    },
    {
        category: "Safety Conflict",
        severity: "critical",
        status: "open",
        title: "Level crossing interlock unavailable",
        description: "SFY-018 renewal on Section C21 leaves the level crossing without interlocking protection during the window.",
        recommendedAction: "Provide manual protection briefing and road closure plan before block start.",
        ruleId: "RULE-SAF-001",
        sectionIds: ["C21"],
        affectedTrains: 3
    },
    {
        category: "Block Conflict",
        severity: "high",
        status: "open",
        title: "Successive blocks without recovery margin",
        description: "Back-to-back blocks on Line W1 leave no recovery margin for late running passenger services.",
        recommendedAction: "Insert a 20 minute recovery gap between the two blocks.",
        ruleId: "RULE-BLK-007",
        sectionIds: ["A11", "A12"],
        affectedTrains: 6
    },
    {
        category: "Train Conflict",
        severity: "medium",
        status: "acknowledged",
        title: "Freight loop occupancy overlap",
        description: "Freight consist held in loop conflicts with the planned departure path of the MEMU service.",
        recommendedAction: "Advance freight departure by 12 minutes.",
        ruleId: "RULE-TRN-011",
        sectionIds: ["B06"],
        affectedTrains: 2
    },
    {
        category: "Resource Conflict",
        severity: "low",
        status: "open",
        title: "Workforce double booking",
        description: "Gang Bravo is assigned to two tasks scheduled in the same window on different sections.",
        recommendedAction: "Reassign the second task to Gang Alpha.",
        ruleId: "RULE-RES-005",
        sectionIds: ["A14", "C20"],
        affectedTrains: 0
    },
    {
        category: "Block Conflict",
        severity: "high",
        status: "open",
        title: "Possession overlaps with block already granted",
        description: "Proposed possession on Section B05 overlaps an approved block granted to the traction department.",
        recommendedAction: "Merge into the approved block or defer the request to the next window.",
        ruleId: "RULE-BLK-003",
        sectionIds: ["B05"],
        affectedTrains: 5
    },
    {
        category: "Train Conflict",
        severity: "critical",
        status: "open",
        title: "Detour path not protected",
        description: "Detour proposed for delayed train 12907 crosses a section that is under maintenance during the same interval.",
        recommendedAction: "Hold the detour at origin until the block is released.",
        ruleId: "RULE-TRN-002",
        sectionIds: ["A13"],
        affectedTrains: 1
    },
    {
        category: "Safety Conflict",
        severity: "medium",
        status: "resolved",
        title: "Axle counter test without clearance",
        description: "Planned axle counter test on C19 lacked clearance from the operating department.",
        recommendedAction: "Record clearance reference and re-run conflict detection.",
        ruleId: "RULE-SAF-004",
        sectionIds: ["C19"],
        affectedTrains: 0
    },
    {
        category: "Resource Conflict",
        severity: "high",
        status: "open",
        title: "Relay set unavailable for concurrent jobs",
        description: "Two interlocking jobs need the same spare relay set on the same night.",
        recommendedAction: "Sequence jobs across consecutive nights.",
        ruleId: "RULE-RES-008",
        sectionIds: ["A13", "B09"],
        affectedTrains: 0
    },
    {
        category: "Block Conflict",
        severity: "low",
        status: "resolved",
        title: "Window shorter than task duration",
        description: "Proposed window of 60 minutes is shorter than the 150 minute task duration for MEC-118.",
        recommendedAction: "Extend window to 180 minutes or split the task into two shifts.",
        ruleId: "RULE-BLK-012",
        sectionIds: ["C23"],
        affectedTrains: 1
    }
];

export const DEMO_CONFLICTS: Conflict[] = CONFLICT_SEEDS.map((seed, index) => ({
    ...seed,
    id: `conflict-${index + 1}`,
    detectedAt: isoAt(-((index % 12) + 1) * 26 * MIN),
    resolutionNote:
        seed.status === "resolved"
            ? seed.recommendedAction
            : undefined,
    sourceSystem: "CONFLICT-ENGINE-Demo",
    sourceRecordId: `CF-${2026}${pad2(index + 1)}`,
    sourceTimestamp: isoAt(-((index % 12) + 1) * 26 * MIN),
    isSimulation: true
}));

/* -------------------------------- Trains -------------------------------- */

const TRAIN_NAMES = [
    "Rajdhani Express",
    "Shatabdi Express",
    "Vande Bharat",
    "Duronto Express",
    "Mail Express",
    "Superfast Express",
    "MEMU",
    "EMU Local",
    "Container Freight",
    "Oil Tanker Freight",
    "Ballast Train",
    "Track Inspection Car",
    "Rake Positioning Run",
    "Passenger Special"
];

const TRAIN_ORIGINS = ["MMCT", "PUNE", "DIVA", "BVI", "KYN", "DR", "CSMT", "NHAV", "Panvel", "Karjat"];
const TRAIN_TYPES: Train["trainType"][] = [
    "Express", "Superfast", "Local", "Freight", "Maintenance", "Inspection"
];
const TRAIN_STATUSES: Train["status"][] = [
    "on-time", "on-time", "on-time", "on-time", "delayed", "delayed", "diverted", "scheduled", "terminated"
];

export const DEMO_TRAINS: Train[] = Array.from({ length: 28 }, (_, index) => {
    const status = pick(TRAIN_STATUSES, index);
    const type = pick(TRAIN_TYPES, index * 2);
    const origin = pick(TRAIN_ORIGINS, index);
    const destination = pick(TRAIN_ORIGINS, index + 4);
    const departure = new Date(NOW - ((index % 6) - 2) * HOUR + (index % 40) * MIN);
    const arrival = new Date(departure.getTime() + (35 + (index % 9) * 12) * MIN);

    return {
        id: `train-${index + 1}`,
        trainNumber: String(12001 + index * 7 + (index % 5) * 100),
        trainName: pick(TRAIN_NAMES, index),
        trainType: type,
        origin,
        destination: origin === destination ? pick(TRAIN_ORIGINS, index + 7) : destination,
        sectionId: pick(SECTION_IDS, index * 7 + 3),
        status,
        delayMinutes:
            status === "delayed" ? 8 + ((index * 7) % 65) : status === "diverted" ? 15 + (index % 20) : 0,
        platform: `PF-${1 + (index % 6)}`,
        speedKph: status === "terminated" ? 0 : status === "scheduled" ? 0 : 35 + ((index * 11) % 95),
        scheduledDeparture: departure.toISOString(),
        scheduledArrival: arrival.toISOString(),
        rakeId: `RKE-${101 + (index % 14)}`,
        priorityCode: type === "Freight" ? "P4" : type === "Local" ? "P3" : index % 3 === 0 ? "P1" : "P2",
        sourceSystem: "TMS-Demo",
        sourceRecordId: `TRN-${12001 + index}`,
        sourceTimestamp: isoAt(-((index % 20) + 1) * 2 * MIN),
        isSimulation: true
    };
});

/* ----------------------------- Block windows ----------------------------- */

const BLOCK_STATUSES: BlockWindow["status"][] = [
    "proposed", "proposed", "proposed",
    "approved", "approved", "approved", "approved",
    "utilized", "utilized",
    "released",
    "rejected"
];

export const DEMO_BLOCK_WINDOWS: BlockWindow[] = Array.from({ length: 20 }, (_, index) => {
    const date = new Date(NOW + ((index % 12) - 4) * DAY);
    const startHour = 1 + (index % 4);
    const start = new Date(2026, 0, 1, startHour, index % 2 === 0 ? 0 : 30);
    const end = new Date(start.getTime() + (120 + (index % 4) * 60) * MIN);
    const status = pick(BLOCK_STATUSES, index);
    const assigned =
        status === "proposed"
            ? DEMO_TASKS.filter((task) => task.sectionId === pick(SECTION_IDS, index * 3 + 1))
                  .slice(0, 1 + (index % 2))
                  .map((task) => task.taskCode)
            : DEMO_TASKS.filter((task) => task.sectionId === pick(SECTION_IDS, index * 3 + 1))
                  .slice(0, 1 + (index % 3))
                  .map((task) => task.taskCode);

    return {
        id: `block-${index + 1}`,
        blockCode: `BLK-${2026}${pad2(index + 1)}`,
        sectionId: pick(SECTION_IDS, index * 3 + 1),
        date: ymd(date),
        start: hhmm(start),
        end: hhmm(end),
        status,
        department: pick(DEPARTMENTS, index),
        assignedTaskIds: assigned,
        capacityTrains: 4 + (index % 5),
        protectedTrains: index % 6,
        approvedBy:
            status === "approved" || status === "utilized" || status === "released"
                ? pick(REVIEWERS, index)
                : undefined,
        notes: pick(
            [
                "Coordinated with section controller.",
                "Awaiting operating clearance.",
                "Merged request from two departments.",
                "Protected paths identified for passenger services.",
                undefined
            ],
            index
        ),
        sourceSystem: "PLANNER-Demo",
        sourceRecordId: `BLK-REC-${500 + index}`,
        sourceTimestamp: isoAt(-((index % 20) + 1) * 45 * MIN),
        isSimulation: true
    };
});

/* --------------------------------- Assets -------------------------------- */

const ASSET_CONDITIONS: Asset["condition"][] = ["good", "good", "good", "fair", "fair", "poor", "critical"];

export const DEMO_ASSETS: Asset[] = Array.from({ length: 32 }, (_, index) => {
    const department = pick(DEPARTMENTS, index * 3);
    const condition = pick(ASSET_CONDITIONS, index);
    const healthPercent =
        condition === "good"
            ? 85 + (index % 15)
            : condition === "fair"
              ? 65 + (index % 18)
              : condition === "poor"
                ? 40 + (index % 20)
                : 18 + (index % 20);

    return {
        id: `asset-${index + 1}`,
        assetCode: `${TASK_PREFIX[department]}-AST-${pad2(index + 1)}${index % 9}`,
        name: `${pick(ASSETS_BY_DEPARTMENT[department], index)} ${pick(["A", "B", "C", "D"], index)}${(index % 9) + 1}`,
        assetType: pick(ASSETS_BY_DEPARTMENT[department], index),
        sectionId: pick(SECTION_IDS, index * 5 + 4),
        ownerDepartment: department,
        healthPercent,
        condition,
        criticality: pick(CRITICALITY_POOL, index + 2),
        lastInspectionDate: ymd(new Date(NOW - ((index % 20) + 3) * DAY)),
        nextInspectionDue: ymd(new Date(NOW + ((index % 25) + 1) * DAY)),
        openWorkOrders: condition === "critical" ? 2 + (index % 3) : index % 4,
        runningHours: 1200 + ((index * 613) % 24000),
        sourceSystem: "ASSET-REG-Demo",
        sourceRecordId: `AST-${6000 + index}`,
        sourceTimestamp: isoAt(-((index % 25) + 1) * 90 * MIN),
        isSimulation: true
    };
});

/* -------------------------------- Reports -------------------------------- */

const REPORT_SEEDS: Array<Pick<ReportItem, "title" | "category" | "format">> = [
    { title: "Daily Maintenance Operations Summary", category: "Operational", format: "PDF" },
    { title: "Block Utilization by Corridor", category: "Planning", format: "XLSX" },
    { title: "Critical Task Aging Report", category: "Maintenance", format: "CSV" },
    { title: "Department Request Approval Audit", category: "Compliance", format: "PDF" },
    { title: "Train Delay Attribution Analysis", category: "Operational", format: "CSV" },
    { title: "Asset Health and Inspection Due List", category: "Maintenance", format: "XLSX" },
    { title: "Conflict Resolution Turnaround", category: "Planning", format: "CSV" },
    { title: "Possession Window Compliance Certificate", category: "Compliance", format: "PDF" },
    { title: "Workforce Deployment Cost Sheet", category: "Financial", format: "XLSX" },
    { title: "Weekly Corridor Availability Review", category: "Operational", format: "PDF" },
    { title: "Monthly Preventive Maintenance Compliance", category: "Maintenance", format: "PDF" },
    { title: "Planned vs Executed Block Report", category: "Planning", format: "CSV" },
    { title: "Safety Observation Register", category: "Compliance", format: "PDF" },
    { title: "Departmental Budget Utilization", category: "Financial", format: "XLSX" }
];

const REPORT_STATUSES: ReportItem["status"][] = [
    "ready", "ready", "ready", "ready", "ready", "ready",
    "scheduled", "scheduled", "generating", "failed"
];

export const DEMO_REPORTS: ReportItem[] = REPORT_SEEDS.map((seed, index) => ({
    id: `report-${index + 1}`,
    reportCode: `RPT-${2026}${pad2(index + 1)}`,
    title: seed.title,
    category: seed.category,
    period:
        index % 3 === 0
            ? `Day ${ymd(new Date(NOW - DAY))}`
            : index % 3 === 1
              ? `Week ${Math.ceil(new Date(NOW).getDate() / 7)} · Oct 2026`
              : "Sep 2026",
    generatedAt: isoAt(-((index % 14) + 1) * 3 * HOUR),
    status: pick(REPORT_STATUSES, index * 3),
    format: seed.format,
    sizeKb: 40 + ((index * 97) % 2400),
    author: pick(["RMOS Scheduler", "A. Menon", "R. Kulkarni", "S. Iyer"], index),
    sourceSystem: "REPORT-ENGINE-Demo",
    sourceRecordId: `RPT-SRC-${900 + index}`,
    sourceTimestamp: isoAt(-((index % 14) + 1) * 3 * HOUR),
    isSimulation: true
}));

/* ----------------------------- Events / feed ----------------------------- */

const EVENT_TEMPLATES: Record<OperationalEvent["type"], string[]> = {
    conflict_detected: [
        "Block conflict detected in Section {S}.",
        "Resource clash detected between two department tasks.",
        "Train path conflict raised by rule engine."
    ],
    conflict_resolved: [
        "Conflict on Section {S} resolved by planner.",
        "Resource clash cleared after reassignment."
    ],
    request_added: [
        "New {D} request added for Section {S}.",
        "{D} submitted a block request for the night window."
    ],
    request_reviewed: [
        "{D} request reviewed and moved to modified state.",
        "Approval decision recorded for {D} request."
    ],
    maintenance_updated: [
        "{T} status updated to IN-PROGRESS.",
        "Progress updated for {T} after site report.",
        "{T} work order revised by planning team."
    ],
    block_changed: [
        "Block window shifted for Section {S} following planner recommendation.",
        "Possession start time revised on Section {S}."
    ],
    block_approved: [
        "Block {T} approved by approver.",
        "Sanctioned block released to operating department."
    ],
    train_delayed: [
        "Train {T} reported {N} min late at Junction B.",
        "Delay chain extended for {T} due to section restriction."
    ],
    train_scheduled: [
        "Train {T} path inserted for Section {S}.",
        "Path confirmed for {T} in the current planning run."
    ],
    asset_flagged: [
        "Asset health dropped below threshold on Section {S}.",
        "Inspection overdue flagged for asset on Section {S}."
    ],
    report_generated: [
        "Report {T} generated and stored.",
        "Scheduled report {T} completed successfully."
    ]
};

const EVENT_TYPES: OperationalEvent["type"][] = [
    "conflict_detected",
    "conflict_resolved",
    "request_added",
    "request_reviewed",
    "maintenance_updated",
    "maintenance_updated",
    "block_changed",
    "block_approved",
    "train_delayed",
    "train_scheduled",
    "asset_flagged",
    "report_generated"
];

const EVENT_SEVERITY: Record<OperationalEvent["type"], OperationalEvent["severity"]> = {
    conflict_detected: "critical",
    conflict_resolved: "info",
    request_added: "info",
    request_reviewed: "info",
    maintenance_updated: "info",
    block_changed: "warning",
    block_approved: "info",
    train_delayed: "warning",
    train_scheduled: "info",
    asset_flagged: "warning",
    report_generated: "info"
};

const EVENT_ACTORS = [
    "Planner Service",
    "Conflict Engine",
    "Section Controller",
    "Dept. Coordinator",
    "Simulation Injector",
    "RMOS Scheduler"
];

export const DEMO_EVENTS: OperationalEvent[] = Array.from({ length: 36 }, (_, index) => {
    const type = pick(EVENT_TYPES, index);
    const template = pick(EVENT_TEMPLATES[type], index);
    const message = template
        .replace("{S}", pick(SECTION_IDS, index * 3))
        .replace("{D}", pick(DEPARTMENTS, index * 5))
        .replace(
            "{T}",
            type.startsWith("train")
                ? pick(DEMO_TRAINS, index).trainNumber
                : type.startsWith("block")
                  ? pick(DEMO_BLOCK_WINDOWS, index).blockCode
                  : type.startsWith("report")
                    ? pick(DEMO_REPORTS, index).reportCode
                    : pick(DEMO_TASKS, index).taskCode
        )
        .replace("{N}", String(8 + ((index * 9) % 60)));

    return {
        id: `event-${index + 1}`,
        timestamp: isoAt(-((index * 37) % 600 + 1) * MIN),
        type,
        severity: EVENT_SEVERITY[type],
        message,
        actor: pick(EVENT_ACTORS, index),
        sectionId: pick(SECTION_IDS, index * 3),
        isSimulation: true
    };
}).sort((a, b) => new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime());

/* ---------------------------------- KPIs --------------------------------- */

const openTasks = DEMO_TASKS.filter((task) => task.status !== "completed");
const criticalOpenTasks = openTasks.filter((task) => task.criticality === "critical");
const overdueTasks = openTasks.filter((task) => new Date(task.dueDate).getTime() < NOW);
const approvedBlocks = DEMO_BLOCK_WINDOWS.filter((block) => block.status === "approved");
const openConflicts = DEMO_CONFLICTS.filter((conflict) => conflict.status !== "resolved");
const delayedTrains = DEMO_TRAINS.filter((train) => train.status === "delayed");
const pendingRequests = DEMO_DEPARTMENT_REQUESTS.filter((request) => request.status === "proposed");
const healthyAssets = DEMO_ASSETS.filter((asset) => asset.condition !== "critical");

export const DEMO_KPIS: DashboardKpis = {
    criticalMaintenance: criticalOpenTasks.length,
    overdueTasks: overdueTasks.length,
    availableBlockWindows: approvedBlocks.length + 4,
    activeConflicts: openConflicts.length,
    assetAvailabilityPercent: Math.round(
        (healthyAssets.length / DEMO_ASSETS.length) * 1000
    ) / 10,
    blockUtilizationPercent: Math.round(
        DEMO_SECTIONS.reduce((total, section) => total + section.utilizationPercent, 0) /
            DEMO_SECTIONS.length
    ),
    trainsRunning: DEMO_TRAINS.filter(
        (train) => train.status === "on-time" || train.status === "delayed" || train.status === "diverted"
    ).length,
    onTimePercent:
        Math.round(
            ((DEMO_TRAINS.length - delayedTrains.length) / DEMO_TRAINS.length) * 1000
        ) / 10,
    tasksCompletedToday: DEMO_TASKS.filter((task) => task.status === "completed").length,
    openApprovalRequests: pendingRequests.length,
    isSimulation: true
};

/** Seven-day planning trend used by the analytics view. */
export const DEMO_WEEKLY_TREND: Array<{ day: string; planned: number; completed: number; delays: number }> =
    Array.from({ length: 7 }, (_, index) => {
        const date = new Date(NOW - (6 - index) * DAY);
        return {
            day: date.toLocaleDateString(undefined, { weekday: "short" }),
            planned: 12 + ((index * 5) % 14),
            completed: 8 + ((index * 7) % 12),
            delays: 1 + ((index * 3) % 6)
        };
    });

export const DEMO_DEPARTMENTS = DEPARTMENTS;
