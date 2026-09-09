import type {
    DashboardKpis,
    CorridorSection,
    MaintenanceTask,
    DepartmentRequest,
    Conflict,
    OperationalEvent
} from "@/types/models";

export interface DataRepository {
    getDashboardKpis(): Promise<DashboardKpis>;
    getCorridorSections(): Promise<CorridorSection[]>;
    getMaintenanceTasks(): Promise<MaintenanceTask[]>;
    getDepartmentRequests(): Promise<DepartmentRequest[]>;
    getConflicts(): Promise<Conflict[]>;
    getRecentEvents(): Promise<OperationalEvent[]>;
}
