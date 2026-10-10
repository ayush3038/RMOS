import type {
    Asset,
    BlockWindow,
    DashboardKpis,
    CorridorSection,
    MaintenanceTask,
    DepartmentRequest,
    Conflict,
    OperationalEvent,
    ReportItem,
    Train
} from "@/types/models";

export interface DataRepository {
    getDashboardKpis(): Promise<DashboardKpis>;
    getCorridorSections(): Promise<CorridorSection[]>;
    getMaintenanceTasks(): Promise<MaintenanceTask[]>;
    getDepartmentRequests(): Promise<DepartmentRequest[]>;
    getConflicts(): Promise<Conflict[]>;
    getRecentEvents(): Promise<OperationalEvent[]>;
    getTrains(): Promise<Train[]>;
    getBlockWindows(): Promise<BlockWindow[]>;
    getAssets(): Promise<Asset[]>;
    getReports(): Promise<ReportItem[]>;
}
