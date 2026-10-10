import { useCallback } from "react";
import { useData } from "./useData";
import { getRepository } from "@/services/api";

/**
 * Single access point for module pages. Loads every demo dataset once per
 * mounted page and exposes the individual loading / error states plus a
 * combined refresh action for the toolbars.
 */
export function useModuleData() {
    const repository = getRepository();

    const kpis = useData(() => repository.getDashboardKpis());
    const sections = useData(() => repository.getCorridorSections());
    const tasks = useData(() => repository.getMaintenanceTasks());
    const requests = useData(() => repository.getDepartmentRequests());
    const conflicts = useData(() => repository.getConflicts());
    const events = useData(() => repository.getRecentEvents());
    const trains = useData(() => repository.getTrains());
    const blocks = useData(() => repository.getBlockWindows());
    const assets = useData(() => repository.getAssets());
    const reports = useData(() => repository.getReports());

    const states = [
        kpis,
        sections,
        tasks,
        requests,
        conflicts,
        events,
        trains,
        blocks,
        assets,
        reports
    ];

    const isLoading = states.some((state) => state.isLoading);
    const error = states.find((state) => state.error)?.error ?? null;

    const refetchKey = states.map((state) => state.refetch).join("|");

    const refresh = useCallback(() => {
        states.forEach((state) => state.refetch());
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [refetchKey]);

    return {
        kpis,
        sections,
        tasks,
        requests,
        conflicts,
        events,
        trains,
        blocks,
        assets,
        reports,
        isLoading,
        error,
        refresh
    };
}
