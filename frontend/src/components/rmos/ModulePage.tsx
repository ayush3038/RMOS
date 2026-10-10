import type { ComponentType } from "react";
import { Button } from "@/components/ui/button";
import { PageHeader } from "@/components/rmos/PageHeader";
import { DemoBanner } from "@/components/rmos/DemoBanner";
import { EmptyState } from "@/components/rmos/EmptyState";
import { useToast } from "@/store/toast";
import { Play, RefreshCcw } from "lucide-react";

import { PlannerPage } from "@/components/rmos/modules/PlannerPage";
import { MaintenancePage } from "@/components/rmos/modules/MaintenancePage";
import { TrainsPage } from "@/components/rmos/modules/TrainsPage";
import { CorridorPage } from "@/components/rmos/modules/CorridorPage";
import { DepartmentsPage } from "@/components/rmos/modules/DepartmentsPage";
import { WeeklyPage } from "@/components/rmos/modules/WeeklyPage";
import { MonthlyPage } from "@/components/rmos/modules/MonthlyPage";
import { ConflictsPage } from "@/components/rmos/modules/ConflictsPage";
import { AnalyticsPage } from "@/components/rmos/modules/AnalyticsPage";
import { ReportsPage } from "@/components/rmos/modules/ReportsPage";

interface ModulePageProps {
    page: string;
    title: string;
    description: string;
}

/** Primary header action per module, with demo feedback. */
const HEADER_ACTIONS: Record<string, { label: string; message: string }> = {
    planner: {
        label: "Run optimizer",
        message: "Optimization run queued · 0 infeasible candidates (demo solver)."
    },
    maintenance: {
        label: "Bulk schedule",
        message: "Bulk scheduling applied to pending tasks in the demo queue."
    },
    trains: {
        label: "Broadcast advisory",
        message: "Path advisory broadcast to delayed services (demo only)."
    },
    corridor: {
        label: "Recalculate availability",
        message: "Availability recomputed from current synthetic possession state."
    },
    departments: {
        label: "Nudge reviewers",
        message: "Review reminders sent to department coordinators (demo only)."
    },
    weekly: {
        label: "Publish weekly plan",
        message: "Weekly plan published for supervisor review — approval still required."
    },
    monthly: {
        label: "Publish monthly plan",
        message: "Monthly plan published for supervisor review — approval still required."
    },
    conflicts: {
        label: "Re-run detection",
        message: "Conflict detection re-run against the latest planning state."
    },
    analytics: {
        label: "Save snapshot",
        message: "Analytics snapshot saved to the demo archive."
    },
    reports: {
        label: "Schedule digest",
        message: "Daily operations digest scheduled for 06:00 (demo)."
    }
};

const PAGES: Record<string, ComponentType> = {
    planner: PlannerPage,
    maintenance: MaintenancePage,
    trains: TrainsPage,
    corridor: CorridorPage,
    departments: DepartmentsPage,
    weekly: WeeklyPage,
    monthly: MonthlyPage,
    conflicts: ConflictsPage,
    analytics: AnalyticsPage,
    reports: ReportsPage
};

export function ModulePage({ page, title, description }: ModulePageProps) {
    const { toast } = useToast();
    const action = HEADER_ACTIONS[page];
    const PageContent = PAGES[page];

    return (
        <div className="flex flex-col gap-4">
            <PageHeader
                title={title}
                description={description}
                actions={
                    <>
                        <Button
                            variant="outline"
                            size="sm"
                            onClick={() => toast("Module data reloaded from the simulation source.", "info")}
                        >
                            <RefreshCcw className="h-4 w-4" />
                            Reload data
                        </Button>
                        {action && (
                            <Button
                                size="sm"
                                onClick={() => toast(action.message, "info")}
                                title={action.message}
                            >
                                <Play className="h-4 w-4" />
                                {action.label}
                            </Button>
                        )}
                    </>
                }
            />

            <DemoBanner />

            {PageContent ? <PageContent /> : <EmptyState message="This module has no content yet." />}
        </div>
    );
}
