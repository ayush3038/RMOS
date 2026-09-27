import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { PageHeader } from "@/components/rmos/PageHeader";
import { useData } from "@/hooks/useData";
import { getRepository } from "@/services/api";

interface ModulePageProps {
    page: string;
    title: string;
    description: string;
}

export function ModulePage({ page, title, description }: ModulePageProps) {
    const repository = getRepository();

    const { data: kpis, isLoading: kpisLoading } =
        useData(() => repository.getDashboardKpis());

    const { data: sections, isLoading: sectionsLoading } =
        useData(() => repository.getCorridorSections());

    const { data: tasks, isLoading: tasksLoading } =
        useData(() => repository.getMaintenanceTasks());

    const { data: requests, isLoading: requestsLoading } =
        useData(() => repository.getDepartmentRequests());

    const { data: conflicts, isLoading: conflictsLoading } =
        useData(() => repository.getConflicts());

    const { data: events, isLoading: eventsLoading } =
        useData(() => repository.getRecentEvents());


    const loading =
        kpisLoading ||
        sectionsLoading ||
        tasksLoading ||
        requestsLoading ||
        conflictsLoading ||
        eventsLoading;

    const pendingTasks =
        tasks?.filter((task) => task.status === "pending").length ?? 0;

    const scheduledTasks =
        tasks?.filter((task) => task.status === "scheduled").length ?? 0;

    const criticalTasks =
        tasks?.filter((task) => task.criticality === "critical").length ?? 0;

    const delayedTrains =
        events?.filter((event) => event.type === "train_delayed").length ?? 0;

    return (
        <div className="flex flex-col gap-6">
            <PageHeader title={title} description={description} />

            <div className="flex items-center justify-between border border-amber-200 bg-amber-50 rounded-lg px-4 py-3">
                <div>
                    <p className="font-medium text-amber-900">
                        Simulation Mode
                    </p>
                    <p className="text-sm text-amber-700">
                        This module is currently displaying RMOS synthetic
                        demonstration data.
                    </p>
                </div>
                <span className="text-xs font-semibold px-3 py-1 rounded-full bg-amber-100 text-amber-800">
                    DEMO
                </span>
            </div>

            {loading ? (
                <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
                    {[1, 2, 3, 4].map((item) => (
                        <Card key={item}>
                            <CardContent className="p-6">
                                <div className="h-16 animate-pulse rounded bg-slate-100" />
                            </CardContent>
                        </Card>
                    ))}
                </div>
            ) : (
                <>
                    {page === "planner" && (
                        <>
                            <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
                                <Metric label="Pending Tasks" value={pendingTasks} />
                                <Metric label="Scheduled Tasks" value={scheduledTasks} />
                                <Metric label="Critical Tasks" value={criticalTasks} />
                                <Metric
                                    label="Available Blocks"
                                    value={kpis?.availableBlockWindows ?? 0}
                                />
                            </div>

                            <DataCard title="Candidate Planning Inputs">
                                <TaskList tasks={tasks ?? []} />
                            </DataCard>
                        </>
                    )}

                    {page === "maintenance" && (
                        <DataCard title="Maintenance Tasks">
                            <TaskList tasks={tasks ?? []} />
                        </DataCard>
                    )}

                    {page === "corridor" && (
                        <DataCard title="Corridor Availability">
                            <div className="space-y-3">
                                {(sections ?? []).map((section) => (
                                    <div
                                        key={section.id}
                                        className="flex items-center justify-between border rounded-lg p-4"
                                    >
                                        <div>
                                            <p className="font-medium">{section.name}</p>
                                            <p className="text-xs text-muted-foreground">
                                                Trains: {section.currentTrains}
                                            </p>
                                        </div>

                                        <span className="text-sm capitalize font-medium">
                                            {section.availabilityStatus}
                                        </span>
                                    </div>
                                ))}
                            </div>
                        </DataCard>
                    )}

                    {page === "departments" && (
                        <DataCard title="Department Requests">
                            <div className="space-y-3">
                                {(requests ?? []).map((request) => (
                                    <div
                                        key={request.id}
                                        className="border rounded-lg p-4"
                                    >
                                        <div className="flex justify-between gap-4">
                                            <div>
                                                <p className="font-medium">
                                                    {request.activity}
                                                </p>
                                                <p className="text-sm text-muted-foreground">
                                                    {request.department} · {request.sectionId}
                                                </p>
                                            </div>
                                            <span className="text-sm font-mono">
                                                {request.requestedWindowStart}–
                                                {request.requestedWindowEnd}
                                            </span>
                                        </div>
                                    </div>
                                ))}
                            </div>
                        </DataCard>
                    )}

                    {page === "conflicts" && (
                        <DataCard title="Active Conflicts">
                            <div className="space-y-3">
                                {(conflicts ?? []).map((conflict) => (
                                    <div
                                        key={conflict.id}
                                        className="border rounded-lg p-4"
                                    >
                                        <div className="flex justify-between gap-4">
                                            <div>
                                                <p className="font-medium">
                                                    {conflict.title}
                                                </p>
                                                <p className="text-sm text-muted-foreground mt-1">
                                                    {conflict.description}
                                                </p>
                                            </div>
                                            <span className="text-xs uppercase font-semibold">
                                                {conflict.severity}
                                            </span>
                                        </div>

                                        <p className="text-sm mt-3">
                                            Recommended: {conflict.recommendedAction}
                                        </p>
                                    </div>
                                ))}
                            </div>
                        </DataCard>
                    )}

                    {page === "analytics" && (
                        <>
                            <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
                                <Metric
                                    label="Asset Availability"
                                    value={`${kpis?.assetAvailabilityPercent ?? 0}%`}
                                />
                                <Metric
                                    label="Block Utilization"
                                    value={`${kpis?.blockUtilizationPercent ?? 0}%`}
                                />
                                <Metric
                                    label="Critical Maintenance"
                                    value={kpis?.criticalMaintenance ?? 0}
                                />
                                <Metric
                                    label="Active Conflicts"
                                    value={kpis?.activeConflicts ?? 0}
                                />
                            </div>

                            <DataCard title="Operational Snapshot">
                                <TaskList tasks={tasks ?? []} />
                            </DataCard>
                        </>
                    )}

                    {page === "reports" && (
                        <DataCard title="Operational Report">
                            <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
                                <Metric label="Maintenance Tasks" value={tasks?.length ?? 0} />
                                <Metric label="Department Requests" value={requests?.length ?? 0} />
                                <Metric label="Conflicts" value={conflicts?.length ?? 0} />
                                <Metric label="Corridors" value={sections?.length ?? 0} />
                                <Metric label="Recent Events" value={events?.length ?? 0} />
                                <Metric label="Delayed Train Events" value={delayedTrains} />
                            </div>
                        </DataCard>
                    )}

                    {page === "weekly" && (
                        <DataCard title="Weekly Maintenance Plan">
                            <TaskList tasks={tasks ?? []} />
                        </DataCard>
                    )}

                    {page === "monthly" && (
                        <DataCard title="Monthly Maintenance Overview">
                            <TaskList tasks={tasks ?? []} />
                        </DataCard>
                    )}

                    {page === "trains" && (
                        <DataCard title="Train Operations">
                            <div className="space-y-3">
                                {(sections ?? []).map((section) => (
                                    <div
                                        key={section.id}
                                        className="flex items-center justify-between border rounded-lg p-4"
                                    >
                                        <div>
                                            <p className="font-medium">
                                                {section.name}
                                            </p>
                                            <p className="text-sm text-muted-foreground">
                                                Synthetic operational state
                                            </p>
                                        </div>

                                        <span className="font-mono">
                                            {section.currentTrains} trains
                                        </span>
                                    </div>
                                ))}
                            </div>
                        </DataCard>
                    )}
                </>
            )}
        </div>
    );
}

function Metric({
    label,
    value,
}: {
    label: string;
    value: string | number;
}) {
    return (
        <Card>
            <CardHeader>
                <CardTitle className="text-sm font-medium text-muted-foreground">
                    {label}
                </CardTitle>
            </CardHeader>
            <CardContent>
                <p className="text-2xl font-bold font-mono">{value}</p>
            </CardContent>
        </Card>
    );
}

function DataCard({
    title,
    children,
}: {
    title: string;
    children: React.ReactNode;
}) {
    return (
        <Card>
            <CardHeader>
                <CardTitle>{title}</CardTitle>
            </CardHeader>
            <CardContent>{children}</CardContent>
        </Card>
    );
}

function TaskList({
    tasks,
}: {
    tasks: Array<{
        id: string;
        taskCode: string;
        department: string;
        assetType: string;
        sectionId: string;
        criticality: string;
        urgency: string;
        dueDate: string;
        durationMinutes: number;
        status: string;
        sourceSystem: string;
    }>;
}) {
    return (
        <div className="space-y-3">
            {tasks.map((task) => (
                <div
                    key={task.id}
                    className="border rounded-lg p-4 flex items-center justify-between gap-4"
                >
                    <div>
                        <p className="font-medium">{task.taskCode}</p>
                        <p className="text-sm text-muted-foreground">
                            {task.department} · {task.assetType} · Section {task.sectionId}
                        </p>
                    </div>

                    <div className="text-right text-sm">
                        <p className="font-medium capitalize">
                            {task.status}
                        </p>
                        <p className="text-muted-foreground">
                            {task.durationMinutes} min · {task.criticality}
                        </p>
                    </div>
                </div>
            ))}
        </div>
    );
}