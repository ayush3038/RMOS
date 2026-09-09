import { useState, useEffect } from "react";
import { PageHeader } from "@/components/rmos/PageHeader";
import { Button } from "@/components/ui/button";
import { DashboardKpis } from "./DashboardKpis";
import { CorridorOverview } from "./CorridorOverview";
import { DepartmentCoordination } from "./DepartmentCoordination";
import { MaintenanceQueue } from "./MaintenanceQueue";
import { ConflictPanel } from "./ConflictPanel";
import { ActivityFeed } from "./ActivityFeed";
import { Play, FileText, CheckCircle2 } from "lucide-react";

export function Dashboard() {
    const [toastMessage, setToastMessage] = useState<string | null>(null);

    const handleAction = (message: string) => {
        setToastMessage(message);
    };

    useEffect(() => {
        if (toastMessage) {
            const timer = setTimeout(() => setToastMessage(null), 3000);
            return () => clearTimeout(timer);
        }
    }, [toastMessage]);

    const headerActions = (
        <>
            <Button variant="outline" size="sm" onClick={() => handleAction("Reports integration pending.")}>
                <FileText className="h-4 w-4 mr-2" />
                View Reports
            </Button>
            <Button size="sm" onClick={() => handleAction("Planner integration pending.")}>
                <Play className="h-4 w-4 mr-2" />
                Generate Plan
            </Button>
        </>
    );

    return (
        <div className="flex flex-col gap-6 relative pb-10">
            <PageHeader
                title="Railway Maintenance Operations"
                description="AI-assisted maintenance planning, coordination and block optimization."
                actions={headerActions}
            />

            <DashboardKpis />

            <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                <div className="lg:col-span-2">
                    <CorridorOverview />
                </div>
                <div className="lg:col-span-1">
                    <DepartmentCoordination />
                </div>
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-4 gap-6">
                <div className="lg:col-span-3">
                    <MaintenanceQueue />
                </div>
                <div className="lg:col-span-1 space-y-6">
                    <ConflictPanel />
                    <ActivityFeed />
                </div>
            </div>

            {toastMessage && (
                <div className="fixed bottom-6 right-6 bg-slate-900 text-white px-4 py-3 rounded-md shadow-lg flex items-center gap-3 animate-in slide-in-from-bottom-5 fade-in z-50">
                    <CheckCircle2 className="h-5 w-5 text-green-400" />
                    <span className="text-sm font-medium">{toastMessage}</span>
                </div>
            )}
        </div>
    );
}
