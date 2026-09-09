import type { DataRepository } from "./repository";
import type {
    DashboardKpis,
    CorridorSection,
    MaintenanceTask,
    DepartmentRequest,
    Conflict,
    OperationalEvent
} from "@/types/models";
import {
    DEMO_KPIS,
    DEMO_SECTIONS,
    DEMO_TASKS,
    DEMO_DEPARTMENT_REQUESTS,
    DEMO_CONFLICTS,
    DEMO_EVENTS
} from "@/data/demo/mockData";

/**
 * Returns mock data wrapped in promises to simulate network requests.
 */
export class DemoDataRepository implements DataRepository {
    async getDashboardKpis(): Promise<DashboardKpis> {
        return new Promise((resolve) => {
            setTimeout(() => resolve(DEMO_KPIS), 300);
        });
    }

    async getCorridorSections(): Promise<CorridorSection[]> {
        return new Promise((resolve) => {
            setTimeout(() => resolve(DEMO_SECTIONS), 300);
        });
    }

    async getMaintenanceTasks(): Promise<MaintenanceTask[]> {
        return new Promise((resolve) => {
            setTimeout(() => resolve(DEMO_TASKS), 300);
        });
    }

    async getDepartmentRequests(): Promise<DepartmentRequest[]> {
        return new Promise((resolve) => {
            setTimeout(() => resolve(DEMO_DEPARTMENT_REQUESTS), 300);
        });
    }

    async getConflicts(): Promise<Conflict[]> {
        return new Promise((resolve) => {
            setTimeout(() => resolve(DEMO_CONFLICTS), 300);
        });
    }

    async getRecentEvents(): Promise<OperationalEvent[]> {
        return new Promise((resolve) => {
            setTimeout(() => resolve(DEMO_EVENTS), 300);
        });
    }
}
