import { KpiStrip, type KpiItem } from "@/components/rmos/KpiStrip";
import { useData } from "@/hooks/useData";
import { getRepository } from "@/services/api";
import {
    AlertTriangle,
    ActivitySquare,
    BarChart3,
    CalendarClock,
    CalendarRange,
    CheckCircle2,
    ShieldAlert,
    Timer,
    Train,
    Users
} from "lucide-react";

interface DashboardKpisProps {
    /** Passed through so each metric can open the module it summarises. */
    onNavigate?: (page: string) => void;
}

export function DashboardKpis({ onNavigate }: DashboardKpisProps) {
    const { data: kpis, isLoading, error } = useData(() => getRepository().getDashboardKpis());

    if (isLoading) {
        return (
            <div className="grid gap-3 md:grid-cols-3 xl:grid-cols-5">
                {Array.from({ length: 10 }).map((_, index) => (
                    <div key={index} className="h-[86px] animate-pulse rounded-md border bg-slate-100" />
                ))}
            </div>
        );
    }

    if (error || !kpis) {
        return (
            <div className="text-sm text-red-500 py-3 text-center w-full rounded-md border border-red-line bg-red-bg">
                Failed to load KPIs.
            </div>
        );
    }

    // Each card opens the module that owns the metric, so the dashboard
    // overview is a starting point rather than a dead end. KPIs without a
    // meaningful destination would stay plain cards (none here).
    const items: KpiItem[] = [
        { label: "Critical Tasks", value: kpis.criticalMaintenance, hint: "Pending high-risk work", icon: AlertTriangle, tone: "red", page: "maintenance" },
        { label: "Overdue Tasks", value: kpis.overdueTasks, hint: "Require immediate action", icon: CalendarClock, tone: kpis.overdueTasks ? "amber" : "green", page: "maintenance" },
        { label: "Available Blocks", value: kpis.availableBlockWindows, hint: "Next 24 hours", icon: CalendarRange, tone: "blue", page: "planner" },
        { label: "Active Conflicts", value: kpis.activeConflicts, hint: "Unresolved schedule clashes", icon: ShieldAlert, tone: kpis.activeConflicts ? "red" : "green", page: "conflicts" },
        { label: "Asset Availability", value: `${kpis.assetAvailabilityPercent}%`, hint: "Network wide average", icon: ActivitySquare, tone: "green", page: "corridor" },
        { label: "Block Utilization", value: `${kpis.blockUtilizationPercent}%`, hint: "Of granted windows", icon: BarChart3, page: "planner" },
        { label: "Trains Running", value: kpis.trainsRunning, hint: "Services on the board", icon: Train, tone: "blue", page: "trains" },
        { label: "On-time Rate", value: `${kpis.onTimePercent}%`, hint: "Without recorded delay", icon: Timer, tone: kpis.onTimePercent >= 85 ? "green" : "amber", page: "trains" },
        { label: "Completed Today", value: kpis.tasksCompletedToday, hint: "Tasks closed this run", icon: CheckCircle2, tone: "green", page: "maintenance" },
        { label: "Open Approvals", value: kpis.openApprovalRequests, hint: "Awaiting department review", icon: Users, tone: kpis.openApprovalRequests ? "amber" : "green", page: "departments" }
    ];

    return (
        <div>
            <KpiStrip items={items} className="xl:grid-cols-5" onNavigate={onNavigate} />
        </div>
    );
}
