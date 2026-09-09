import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { useData } from "@/hooks/useData";
import { getRepository } from "@/services/api";
import {
    AlertTriangle,
    CalendarClock,
    CalendarRange,
    ShieldAlert,
    ActivitySquare,
    BarChart3
} from "lucide-react";

export function DashboardKpis() {
    const { data: kpis, isLoading, error } = useData(() => getRepository().getDashboardKpis());

    if (isLoading) {
        return <div className="text-sm text-slate-500 py-4 text-center w-full">Loading KPIs...</div>;
    }

    if (error || !kpis) {
        return <div className="text-sm text-red-500 py-4 text-center w-full">Failed to load KPIs.</div>;
    }

    return (
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-6 mb-6">
            <Card>
                <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                    <CardTitle className="text-sm font-medium">Critical Tasks</CardTitle>
                    <AlertTriangle className="h-4 w-4 text-destructive" />
                </CardHeader>
                <CardContent>
                    <div className="text-2xl font-bold font-mono">{kpis.criticalMaintenance}</div>
                    <p className="text-xs text-muted-foreground mt-1">Pending maintenance</p>
                </CardContent>
            </Card>

            <Card>
                <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                    <CardTitle className="text-sm font-medium">Overdue Tasks</CardTitle>
                    <CalendarClock className="h-4 w-4 text-amber-500" />
                </CardHeader>
                <CardContent>
                    <div className="text-2xl font-bold font-mono text-amber-500">{kpis.overdueTasks}</div>
                    <p className="text-xs text-muted-foreground mt-1">Require immediate action</p>
                </CardContent>
            </Card>

            <Card>
                <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                    <CardTitle className="text-sm font-medium">Available Blocks</CardTitle>
                    <CalendarRange className="h-4 w-4 text-primary" />
                </CardHeader>
                <CardContent>
                    <div className="text-2xl font-bold font-mono">{kpis.availableBlockWindows}</div>
                    <p className="text-xs text-muted-foreground mt-1">Next 24 hours</p>
                </CardContent>
            </Card>

            <Card>
                <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                    <CardTitle className="text-sm font-medium">Active Conflicts</CardTitle>
                    <ShieldAlert className="h-4 w-4 text-destructive" />
                </CardHeader>
                <CardContent>
                    <div className="text-2xl font-bold font-mono text-destructive">{kpis.activeConflicts}</div>
                    <p className="text-xs text-muted-foreground mt-1">Unresolved schedule clashes</p>
                </CardContent>
            </Card>

            <Card>
                <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                    <CardTitle className="text-sm font-medium">Asset Availability</CardTitle>
                    <ActivitySquare className="h-4 w-4 text-green-600" />
                </CardHeader>
                <CardContent>
                    <div className="text-2xl font-bold font-mono text-green-600">{kpis.assetAvailabilityPercent}%</div>
                    <p className="text-xs text-muted-foreground mt-1">Network wide average</p>
                </CardContent>
            </Card>

            <Card>
                <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                    <CardTitle className="text-sm font-medium">Block Utilization</CardTitle>
                    <BarChart3 className="h-4 w-4 text-primary" />
                </CardHeader>
                <CardContent>
                    <div className="text-2xl font-bold font-mono">{kpis.blockUtilizationPercent}%</div>
                    <p className="text-xs text-muted-foreground mt-1">Of granted windows</p>
                </CardContent>
            </Card>
        </div>
    );
}
