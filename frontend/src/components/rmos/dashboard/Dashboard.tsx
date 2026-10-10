import { Button } from "@/components/ui/button";
import { PageHeader } from "@/components/rmos/PageHeader";
import { DemoBanner } from "@/components/rmos/DemoBanner";
import { useToast } from "@/store/toast";
import { DashboardKpis } from "./DashboardKpis";
import { CorridorOverview } from "./CorridorOverview";
import { DepartmentCoordination } from "./DepartmentCoordination";
import { MaintenanceQueue } from "./MaintenanceQueue";
import { ConflictPanel } from "./ConflictPanel";
import { ActivityFeed } from "./ActivityFeed";
import { FileText, Play } from "lucide-react";

interface DashboardProps {
    onNavigate: (page: string) => void;
}

export function Dashboard({ onNavigate }: DashboardProps) {
    const { toast } = useToast();

    const headerActions = (
        <>
            <Button
                variant="outline"
                size="sm"
                onClick={() => {
                    toast("Opening the report library.", "info");
                    onNavigate("reports");
                }}
            >
                <FileText className="h-4 w-4" />
                View Reports
            </Button>
            <Button
                size="sm"
                onClick={() => {
                    toast("Opening the block planner for the next planning cycle.", "info");
                    onNavigate("planner");
                }}
            >
                <Play className="h-4 w-4" />
                Generate Plan
            </Button>
        </>
    );

    return (
        <div className="flex flex-col gap-4 relative pb-6">
            <PageHeader
                title="Railway Maintenance Operations"
                description="AI-assisted maintenance planning, coordination and block optimization."
                actions={headerActions}
            />

            <DemoBanner />

            <DashboardKpis />

            <div className="grid grid-cols-1 lg:grid-cols-3 gap-4">
                <div className="lg:col-span-2">
                    <CorridorOverview />
                </div>
                <div className="lg:col-span-1">
                    <DepartmentCoordination />
                </div>
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-4 gap-4">
                <div className="lg:col-span-3">
                    <MaintenanceQueue />
                </div>
                <div className="lg:col-span-1 space-y-4">
                    <ConflictPanel />
                    <ActivityFeed />
                </div>
            </div>
        </div>
    );
}
