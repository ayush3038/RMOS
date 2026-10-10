import type { DataRepository } from "./repository";
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
import {
    DEMO_KPIS,
    DEMO_SECTIONS,
    DEMO_TASKS,
    DEMO_DEPARTMENT_REQUESTS,
    DEMO_CONFLICTS,
    DEMO_EVENTS,
    DEMO_TRAINS,
    DEMO_BLOCK_WINDOWS,
    DEMO_ASSETS,
    DEMO_REPORTS
} from "@/data/demo/mockData";

/**
 * Returns mock data wrapped in promises to simulate network requests.
 * The artificial latency keeps loading states visible during demonstration.
 */
const respond = <T>(payload: T, latencyMs = 300): Promise<T> =>
    new Promise((resolve) => {
        setTimeout(() => resolve(payload), latencyMs);
    });

export class DemoDataRepository implements DataRepository {
    async getDashboardKpis(): Promise<DashboardKpis> {
        return respond(DEMO_KPIS);
    }

    async getCorridorSections(): Promise<CorridorSection[]> {
        return respond(DEMO_SECTIONS);
    }

    async getMaintenanceTasks(): Promise<MaintenanceTask[]> {
        return respond(DEMO_TASKS);
    }

    async getDepartmentRequests(): Promise<DepartmentRequest[]> {
        return respond(DEMO_DEPARTMENT_REQUESTS);
    }

    async getConflicts(): Promise<Conflict[]> {
        return respond(DEMO_CONFLICTS);
    }

    async getRecentEvents(): Promise<OperationalEvent[]> {
        return respond(DEMO_EVENTS);
    }

    async getTrains(): Promise<Train[]> {
        return respond(DEMO_TRAINS);
    }

    async getBlockWindows(): Promise<BlockWindow[]> {
        return respond(DEMO_BLOCK_WINDOWS);
    }

    async getAssets(): Promise<Asset[]> {
        return respond(DEMO_ASSETS);
    }

    async getReports(): Promise<ReportItem[]> {
        return respond(DEMO_REPORTS);
    }
}
