export type PriorityLevel = "critical" | "high" | "medium" | "low";
export type Department = "Engineering" | "Traction" | "S&T" | "Operations";
export type TaskStatus = "pending" | "scheduled" | "in-progress" | "completed";

export interface MaintenanceTask {
    id: string;
    taskCode: string; // User friendly ID, e.g. TSK-1002
    department: Department;
    assetType: string; // e.g. Track, OHE, Signal
    sectionId: string;
    criticality: PriorityLevel;
    urgency: PriorityLevel;
    dueDate: string; // ISO Date String
    durationMinutes: number;
    status: TaskStatus;
    dependencies?: string[];
    sourceSystem: string;
    isSimulation: boolean;
}

export interface CorridorSection {
    id: string;
    name: string;
    availabilityStatus: "available" | "maintenance" | "blocked" | "restricted";
    currentTrains: number;
    upcomingMaintenanceCode?: string;
    isSimulation: boolean;
}

export interface DepartmentRequest {
    id: string;
    department: Department;
    activity: string;
    sectionId: string;
    requestedWindowStart: string; // e.g. '02:00'
    requestedWindowEnd: string; // e.g. '04:00'
    isSimulation: boolean;
}

export interface Conflict {
    id: string;
    category: "Train Conflict" | "Resource Conflict" | "Block Conflict";
    severity: PriorityLevel;
    title: string;
    description: string;
    recommendedAction: string;
    isSimulation: boolean;
}

export interface OperationalEvent {
    id: string;
    timestamp: string; // ISO date string
    type: "maintenance_updated" | "block_changed" | "train_delayed" | "request_added" | "conflict_detected";
    message: string;
    isSimulation: boolean;
}

export interface DashboardKpis {
    criticalMaintenance: number;
    overdueTasks: number;
    availableBlockWindows: number;
    activeConflicts: number;
    assetAvailabilityPercent: number;
    blockUtilizationPercent: number;
    isSimulation: boolean;
}
