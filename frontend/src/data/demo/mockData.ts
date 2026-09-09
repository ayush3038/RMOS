import type {
    MaintenanceTask,
    CorridorSection,
    DepartmentRequest,
    Conflict,
    OperationalEvent,
    DashboardKpis
} from "./types";

export const DEMO_KPIS: DashboardKpis = {
    criticalMaintenance: 14,
    overdueTasks: 3,
    availableBlockWindows: 8,
    activeConflicts: 5,
    assetAvailabilityPercent: 94.2,
    blockUtilizationPercent: 78.5,
    isSimulation: true,
};

export const DEMO_SECTIONS: CorridorSection[] = [
    { id: "s1", name: "Section A10", availabilityStatus: "available", currentTrains: 2, isSimulation: true },
    { id: "s2", name: "Section A11", availabilityStatus: "restricted", currentTrains: 1, isSimulation: true },
    { id: "s3", name: "Section A12", availabilityStatus: "maintenance", currentTrains: 0, upcomingMaintenanceCode: "ENG-092", isSimulation: true },
    { id: "s4", name: "Section A13", availabilityStatus: "blocked", currentTrains: 0, isSimulation: true },
    { id: "s5", name: "Section A14", availabilityStatus: "available", currentTrains: 3, isSimulation: true },
    { id: "s6", name: "Section A15", availabilityStatus: "available", currentTrains: 1, isSimulation: true },
];

export const DEMO_DEPARTMENT_REQUESTS: DepartmentRequest[] = [
    { id: "req1", department: "Engineering", activity: "Track geometry correction", sectionId: "Section A12", requestedWindowStart: "02:00", requestedWindowEnd: "04:00", isSimulation: true },
    { id: "req2", department: "Traction", activity: "OHE tensioning check", sectionId: "Section A12", requestedWindowStart: "02:30", requestedWindowEnd: "04:30", isSimulation: true },
    { id: "req3", department: "S&T", activity: "Signal relay replacement", sectionId: "Section A12", requestedWindowStart: "02:15", requestedWindowEnd: "03:15", isSimulation: true },
];

export const DEMO_TASKS: MaintenanceTask[] = [
    { id: "t1", taskCode: "ENG-092", department: "Engineering", assetType: "Track", sectionId: "A12", criticality: "critical", urgency: "high", dueDate: new Date(Date.now() + 86400000).toISOString(), durationMinutes: 120, status: "scheduled", sourceSystem: "MMS-Demo", isSimulation: true },
    { id: "t2", taskCode: "TRC-104", department: "Traction", assetType: "OHE", sectionId: "B04", criticality: "high", urgency: "medium", dueDate: new Date(Date.now() + 172800000).toISOString(), durationMinutes: 180, status: "pending", sourceSystem: "MMS-Demo", isSimulation: true },
    { id: "t3", taskCode: "SNT-045", department: "S&T", assetType: "Signal Point", sectionId: "A14", criticality: "medium", urgency: "low", dueDate: new Date(Date.now() + 259200000).toISOString(), durationMinutes: 60, status: "pending", sourceSystem: "MMS-Demo", isSimulation: true },
    { id: "t4", taskCode: "ENG-093", department: "Engineering", assetType: "Bridge", sectionId: "C19", criticality: "high", urgency: "critical", dueDate: new Date(Date.now() - 3600000).toISOString(), durationMinutes: 240, status: "in-progress", dependencies: ["TRC-102"], sourceSystem: "MMS-Demo", isSimulation: true },
    { id: "t5", taskCode: "TRC-105", department: "Traction", assetType: "Pantograph Zone", sectionId: "A15", criticality: "medium", urgency: "medium", dueDate: new Date(Date.now() + 345600000).toISOString(), durationMinutes: 90, status: "pending", sourceSystem: "MMS-Demo", isSimulation: true },
    { id: "t6", taskCode: "SNT-046", department: "S&T", assetType: "Interlocking", sectionId: "B02", criticality: "critical", urgency: "high", dueDate: new Date(Date.now() + 43200000).toISOString(), durationMinutes: 300, status: "scheduled", sourceSystem: "MMS-Demo", isSimulation: true },
    { id: "t7", taskCode: "ENG-094", department: "Engineering", assetType: "Track", sectionId: "A10", criticality: "low", urgency: "low", dueDate: new Date(Date.now() + 604800000).toISOString(), durationMinutes: 180, status: "pending", sourceSystem: "MMS-Demo", isSimulation: true },
    { id: "t8", taskCode: "TRC-106", department: "Traction", assetType: "OHE", sectionId: "C22", criticality: "high", urgency: "high", dueDate: new Date(Date.now() + 86400000).toISOString(), durationMinutes: 150, status: "pending", sourceSystem: "MMS-Demo", isSimulation: true },
    { id: "t9", taskCode: "SNT-047", department: "S&T", assetType: "Track Circuit", sectionId: "B09", criticality: "medium", urgency: "high", dueDate: new Date(Date.now() + 172800000).toISOString(), durationMinutes: 120, status: "pending", sourceSystem: "MMS-Demo", isSimulation: true },
];

export const DEMO_CONFLICTS: Conflict[] = [
    { id: "c1", category: "Block Conflict", severity: "critical", title: "Overlapping Block Request", description: "ENG-092 & TRC-104 request overlapping maintenance windows in Section A12.", recommendedAction: "Merge into integrated block.", isSimulation: true },
    { id: "c2", category: "Train Conflict", severity: "high", title: "Express Train Arrival", description: "Vande Bharat Ex delay overlaps with SNT-045 window.", recommendedAction: "Shift window +30 mins.", isSimulation: true },
    { id: "c3", category: "Resource Conflict", severity: "medium", title: "Tower Wagon Scarcity", description: "Insufficient Tower Wagons available for concurrent traction tasks in division.", recommendedAction: "Reschedule TRC-106.", isSimulation: true }
];

export const DEMO_EVENTS: OperationalEvent[] = [
    { id: "e1", timestamp: new Date(Date.now() - 60000).toISOString(), type: "conflict_detected", message: "Block conflict detected in Section A12.", isSimulation: true },
    { id: "e2", timestamp: new Date(Date.now() - 360000).toISOString(), type: "request_added", message: "New engineering request added by Dept.", isSimulation: true },
    { id: "e3", timestamp: new Date(Date.now() - 1200000).toISOString(), type: "train_delayed", message: "Train 12001 reported 20m late at Junction B.", isSimulation: true },
    { id: "e4", timestamp: new Date(Date.now() - 3600000).toISOString(), type: "maintenance_updated", message: "ENG-093 status updated to IN-PROGRESS.", isSimulation: true },
    { id: "e5", timestamp: new Date(Date.now() - 7200000).toISOString(), type: "block_changed", message: "Block window shifted for A14 following planner recommendation.", isSimulation: true },
];
