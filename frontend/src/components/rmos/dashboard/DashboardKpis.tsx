import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { DEMO_KPIS } from "@/data/demo/mockData";
import {
    AlertTriangle,
    CalendarClock,
    CalendarRange,
    ShieldAlert,
    ActivitySquare,
    BarChart3
} from "lucide-react";

export function DashboardKpis() {
    return (
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-6 mb-6">
            <Card>
                <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                    <CardTitle className="text-sm font-medium">Critical Tasks</CardTitle>
                    <AlertTriangle className="h-4 w-4 text-destructive" />
                </CardHeader>
                <CardContent>
                    <div className="text-2xl font-bold font-mono">{DEMO_KPIS.criticalMaintenance}</div>
                    <p className="text-xs text-muted-foreground mt-1">Pending maintenance</p>
                </CardContent>
            </Card>

            <Card>
                <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                    <CardTitle className="text-sm font-medium">Overdue Tasks</CardTitle>
                    <CalendarClock className="h-4 w-4 text-amber-500" />
                </CardHeader>
                <CardContent>
                    <div className="text-2xl font-bold font-mono text-amber-500">{DEMO_KPIS.overdueTasks}</div>
                    <p className="text-xs text-muted-foreground mt-1">Require immediate action</p>
                </CardContent>
            </Card>

            <Card>
                <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                    <CardTitle className="text-sm font-medium">Available Blocks</CardTitle>
                    <CalendarRange className="h-4 w-4 text-primary" />
                </CardHeader>
                <CardContent>
                    <div className="text-2xl font-bold font-mono">{DEMO_KPIS.availableBlockWindows}</div>
                    <p className="text-xs text-muted-foreground mt-1">Next 24 hours</p>
                </CardContent>
            </Card>

            <Card>
                <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                    <CardTitle className="text-sm font-medium">Active Conflicts</CardTitle>
                    <ShieldAlert className="h-4 w-4 text-destructive" />
                </CardHeader>
                <CardContent>
                    <div className="text-2xl font-bold font-mono text-destructive">{DEMO_KPIS.activeConflicts}</div>
                    <p className="text-xs text-muted-foreground mt-1">Unresolved schedule clashes</p>
                </CardContent>
            </Card>

            <Card>
                <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                    <CardTitle className="text-sm font-medium">Asset Availability</CardTitle>
                    <ActivitySquare className="h-4 w-4 text-green-600" />
                </CardHeader>
                <CardContent>
                    <div className="text-2xl font-bold font-mono text-green-600">{DEMO_KPIS.assetAvailabilityPercent}%</div>
                    <p className="text-xs text-muted-foreground mt-1">Network wide average</p>
                </CardContent>
            </Card>

            <Card>
                <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                    <CardTitle className="text-sm font-medium">Block Utilization</CardTitle>
                    <BarChart3 className="h-4 w-4 text-primary" />
                </CardHeader>
                <CardContent>
                    <div className="text-2xl font-bold font-mono">{DEMO_KPIS.blockUtilizationPercent}%</div>
                    <p className="text-xs text-muted-foreground mt-1">Of granted windows</p>
                </CardContent>
            </Card>
        </div>
    );
}
