export type PriorityLevel = "critical" | "high" | "medium" | "low";
export type Department =
    | "Engineering"
    | "Traction"
    | "S&T"
    | "Operations"
    | "Mechanical"
    | "Safety";
export type TaskStatus = "pending" | "scheduled" | "in-progress" | "completed";
export type RequestStatus = "proposed" | "approved" | "modified" | "rejected";
export type ConflictStatus = "open" | "acknowledged" | "resolved";
export type TrainStatus = "on-time" | "delayed" | "diverted" | "terminated" | "scheduled";
export type BlockStatus = "proposed" | "approved" | "rejected" | "utilized" | "released";
export type AssetCondition = "good" | "fair" | "poor" | "critical";
export type ReportStatus = "ready" | "generating" | "scheduled" | "failed";
export type AvailabilityStatus = "available" | "maintenance" | "blocked" | "restricted";

/**
 * Every synthetic record carries source system, source record id and a source
 * timestamp so provenance and freshness stay visible (see RULES.md, Data rules).
 */
export interface Provenance {
    sourceSystem: string;
    sourceRecordId: string;
    sourceTimestamp: string;
    isSimulation: boolean;
}

export interface MaintenanceTask extends Provenance {
    id: string;
    taskCode: string;
    department: Department;
    assetType: string;
    sectionId: string;
    criticality: PriorityLevel;
    urgency: PriorityLevel;
    dueDate: string;
    durationMinutes: number;
    status: TaskStatus;
    assignedCrew: string;
    workOrder: string;
    progressPercent: number;
    priorityRank: number;
    workforce: number;
    estimatedCostInr: number;
    dependencies?: string[];
    notes?: string;
}

export interface CorridorSection extends Provenance {
    id: string;
    name: string;
    corridor: string;
    line: string;
    availabilityStatus: AvailabilityStatus;
    currentTrains: number;
    capacityTrains: number;
    utilizationPercent: number;
    lengthKm: number;
    speedLimitKph: number;
    upcomingMaintenanceCode?: string;
    nextWindowStart?: string;
    lastInspectionDate: string;
}

export interface DepartmentRequest extends Provenance {
    id: string;
    department: Department;
    activity: string;
    sectionId: string;
    requestedWindowStart: string;
    requestedWindowEnd: string;
    requestedDate: string;
    status: RequestStatus;
    priority: PriorityLevel;
    requestedBy: string;
    workforce: number;
    durationMinutes: number;
    reviewNote?: string;
}

export interface Conflict extends Provenance {
    id: string;
    category: "Train Conflict" | "Resource Conflict" | "Block Conflict" | "Safety Conflict";
    severity: PriorityLevel;
    status: ConflictStatus;
    title: string;
    description: string;
    recommendedAction: string;
    ruleId: string;
    sectionIds: string[];
    affectedTrains: number;
    detectedAt: string;
    resolutionNote?: string;
}

export interface Train extends Provenance {
    id: string;
    trainNumber: string;
    trainName: string;
    trainType: "Express" | "Superfast" | "Local" | "Freight" | "Maintenance" | "Inspection";
    origin: string;
    destination: string;
    sectionId: string;
    status: TrainStatus;
    delayMinutes: number;
    platform: string;
    speedKph: number;
    scheduledDeparture: string;
    scheduledArrival: string;
    rakeId: string;
    priorityCode: string;
}

export interface BlockWindow extends Provenance {
    id: string;
    blockCode: string;
    sectionId: string;
    date: string;
    start: string;
    end: string;
    status: BlockStatus;
    department: Department;
    assignedTaskIds: string[];
    capacityTrains: number;
    protectedTrains: number;
    approvedBy?: string;
    notes?: string;
}

export interface Asset extends Provenance {
    id: string;
    assetCode: string;
    name: string;
    assetType: string;
    sectionId: string;
    ownerDepartment: Department;
    healthPercent: number;
    condition: AssetCondition;
    criticality: PriorityLevel;
    lastInspectionDate: string;
    nextInspectionDue: string;
    openWorkOrders: number;
    runningHours: number;
}

export interface ReportItem extends Provenance {
    id: string;
    reportCode: string;
    title: string;
    category: "Operational" | "Maintenance" | "Compliance" | "Planning" | "Financial";
    period: string;
    generatedAt: string;
    status: ReportStatus;
    format: "PDF" | "CSV" | "XLSX";
    sizeKb: number;
    author: string;
}

export interface DashboardKpis {
    criticalMaintenance: number;
    overdueTasks: number;
    availableBlockWindows: number;
    activeConflicts: number;
    assetAvailabilityPercent: number;
    blockUtilizationPercent: number;
    trainsRunning: number;
    onTimePercent: number;
    tasksCompletedToday: number;
    openApprovalRequests: number;
    isSimulation: boolean;
}

export type OperationalEventType =
    | "maintenance_updated"
    | "block_changed"
    | "block_approved"
    | "train_delayed"
    | "train_scheduled"
    | "request_added"
    | "request_reviewed"
    | "conflict_detected"
    | "conflict_resolved"
    | "asset_flagged"
    | "report_generated";

export interface OperationalEvent {
    id: string;
    timestamp: string;
    type: OperationalEventType;
    severity: "info" | "warning" | "critical";
    message: string;
    actor: string;
    sectionId?: string;
    isSimulation: boolean;
}
