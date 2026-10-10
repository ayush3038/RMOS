import type { ComponentType } from "react";
import {
    LayoutDashboard,
    Train,
    Route,
    CalendarDays,
    Calendar,
    CalendarRange,
    Wrench,
    Users,
    Siren,
    LineChart,
    FileText,
    Bot
} from "lucide-react";

export type BadgeKey =
    | "pendingTasks"
    | "openConflicts"
    | "delayedTrains"
    | "pendingApprovals";

export interface NavItem {
    id: string;
    label: string;
    hint: string;
    icon: ComponentType<{ className?: string }>;
    badge?: BadgeKey;
}

export interface NavGroup {
    id: string;
    label: string;
    items: NavItem[];
}

export const NAV_GROUPS: NavGroup[] = [
    {
        id: "operations",
        label: "Operations",
        items: [
            { id: "dashboard", label: "Dashboard", hint: "Live network overview", icon: LayoutDashboard },
            { id: "trains", label: "Train Operations", hint: "Running and delayed services", icon: Train, badge: "delayedTrains" },
            { id: "corridor", label: "Corridor Availability", hint: "Section status and utilization", icon: Route }
        ]
    },
    {
        id: "planning",
        label: "Planning",
        items: [
            { id: "planner", label: "Block Planner", hint: "Candidate tasks and block windows", icon: CalendarDays },
            { id: "maintenance", label: "Maintenance", hint: "Task queue and work orders", icon: Wrench, badge: "pendingTasks" },
            { id: "departments", label: "Departments", hint: "Cross-department approvals", icon: Users, badge: "pendingApprovals" },
            { id: "weekly", label: "Weekly Plan", hint: "Seven day planning board", icon: Calendar },
            { id: "monthly", label: "Monthly Plan", hint: "Month load heatmap", icon: CalendarRange }
        ]
    },
    {
        id: "control",
        label: "Control & Insights",
        items: [
            { id: "assistant", label: "AI Assistant", hint: "Plans, blocks and priorities explained", icon: Bot },
            { id: "conflicts", label: "Conflicts", hint: "Detected clashes and resolutions", icon: Siren, badge: "openConflicts" },
            { id: "analytics", label: "Analytics", hint: "KPI trends and distributions", icon: LineChart },
            { id: "reports", label: "Reports", hint: "Generated operational reports", icon: FileText }
        ]
    }
];

export const NAV_ITEMS: NavItem[] = NAV_GROUPS.flatMap((group) => group.items);

export const findNavItem = (pageId: string): NavItem | undefined =>
    NAV_ITEMS.find((item) => item.id === pageId);
