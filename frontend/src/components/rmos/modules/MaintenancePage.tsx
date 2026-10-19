import { useEffect, useMemo, useRef, useState } from "react";
import {
    AlertTriangle,
    CalendarClock,
    CheckCircle2,
    ListTodo,
    PlayCircle,
    Timer,
    Users,
    Wrench
} from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import {
    Sheet,
    SheetContent,
    SheetDescription,
    SheetHeader,
    SheetTitle
} from "@/components/ui/sheet";
import { DataTable, type DataTableColumn } from "@/components/rmos/DataTable";
import { DetailItem } from "@/components/rmos/Detail";
import { KpiStrip, type KpiItem } from "@/components/rmos/KpiStrip";
import { FilterSelect, ModuleToolbar } from "@/components/rmos/ModuleToolbar";
import { StatusChip } from "@/components/rmos/StatusChip";
import { useModuleData } from "@/hooks/useModuleData";
import { useLocalList } from "@/hooks/useLocalList";
import { useToast } from "@/store/toast";
import { downloadCsv } from "@/lib/export";
import { priorityChip, taskStatusChip } from "@/lib/status";
import {
    formatCurrency,
    formatDate,
    formatDuration,
    isOverdue,
    relativeTime
} from "@/lib/format";
import { DEMO_DEPARTMENTS } from "@/data/demo/mockData";
import type { MaintenanceTask } from "@/types/models";

const STATUS_OPTIONS = [
    { value: "all", label: "All statuses" },
    { value: "pending", label: "Pending" },
    { value: "scheduled", label: "Scheduled" },
    { value: "in-progress", label: "In progress" },
    { value: "completed", label: "Completed" }
];

const DEPARTMENT_OPTIONS = [
    { value: "all", label: "All departments" },
    ...DEMO_DEPARTMENTS.map((department) => ({ value: department, label: department }))
];

const CRITICALITY_OPTIONS = [
    { value: "all", label: "All criticality" },
    { value: "critical", label: "Critical" },
    { value: "high", label: "High" },
    { value: "medium", label: "Medium" },
    { value: "low", label: "Low" }
];

export function MaintenancePage({ record }: { record?: string | null }) {
    const { tasks: tasksState, sections, refresh } = useModuleData();
    const { toast } = useToast();

    const { items: tasks, update: updateTask } = useLocalList<MaintenanceTask>(
        tasksState.data,
        (task) => task.id
    );

    const [search, setSearch] = useState("");
    const [department, setDepartment] = useState("all");
    const [status, setStatus] = useState("all");
    const [criticality, setCriticality] = useState("all");
    const [selectedId, setSelectedId] = useState<string | null>(null);

    const selected = tasks.find((task) => task.id === selectedId) ?? null;

    // Notification deep link (?record=TRC-103): open the linked work order's
    // detail sheet once the queue has loaded, or surface a clear message when
    // the referenced record does not exist in the data.
    const appliedRecord = useRef<string | null>(null);
    useEffect(() => {
        const key = record?.trim();
        if (!key || appliedRecord.current === key || tasksState.isLoading) return;
        const match = tasks.find(
            (task) =>
                task.taskCode.toUpperCase() === key.toUpperCase() ||
                task.workOrder.toUpperCase() === key.toUpperCase()
        );
        appliedRecord.current = key;
        if (match) {
            setSelectedId(match.id);
        } else if (!tasksState.error) {
            toast(`Linked record ${key} was not found in the maintenance queue.`, "warning");
        }
    }, [record, tasks, tasksState.isLoading, tasksState.error, toast]);

    const sectionName = (sectionId: string) =>
        sections.data?.find((section) => section.id === sectionId)?.name ?? sectionId;

    const filtered = useMemo(() => {
        const query = search.trim().toLowerCase();
        return tasks.filter((task) => {
            if (department !== "all" && task.department !== department) return false;
            if (status !== "all" && task.status !== status) return false;
            if (criticality !== "all" && task.criticality !== criticality) return false;
            if (!query) return true;
            return [
                task.taskCode,
                task.workOrder,
                task.assetType,
                task.sectionId,
                task.assignedCrew,
                task.sourceSystem
            ]
                .join(" ")
                .toLowerCase()
                .includes(query);
        });
    }, [tasks, search, department, status, criticality]);

    const overdue = tasks.filter(
        (task) => task.status !== "completed" && isOverdue(task.dueDate)
    );
    const active = tasks.filter(
        (task) => task.status === "in-progress" || task.status === "scheduled"
    );
    const workforceHours = Math.round(
        tasks
            .filter((task) => task.status !== "completed")
            .reduce((total, task) => total + task.durationMinutes * task.workforce, 0) / 60
    );

    const kpis: KpiItem[] = [
        { label: "Total Tasks", value: tasks.length, hint: "Across all departments", icon: ListTodo },
        { label: "Overdue", value: overdue.length, hint: "Past due date, open", icon: AlertTriangle, tone: overdue.length ? "red" : "green" },
        { label: "In Progress", value: tasks.filter((task) => task.status === "in-progress").length, hint: "Field execution active", icon: PlayCircle, tone: "blue" },
        { label: "Scheduled", value: tasks.filter((task) => task.status === "scheduled").length, hint: "Window assigned", icon: CalendarClock },
        { label: "Completed", value: tasks.filter((task) => task.status === "completed").length, hint: "Closed in this run", icon: CheckCircle2, tone: "green" },
        { label: "Labour Planned", value: `${workforceHours} h`, hint: "Duration × workforce", icon: Users }
    ];

    const columns: DataTableColumn<MaintenanceTask>[] = [
        {
            key: "taskCode",
            header: "Task / Work order",
            sortValue: (task) => task.taskCode,
            cell: (task) => (
                <div className="leading-tight">
                    <div className="font-mono text-[12.5px] font-semibold flex items-center gap-1.5">
                        {task.taskCode}
                        {isOverdue(task.dueDate) && task.status !== "completed" && (
                            <AlertTriangle className="w-3.5 h-3.5 text-red" />
                        )}
                    </div>
                    <div className="text-[11px] text-muted font-mono">{task.workOrder}</div>
                </div>
            )
        },
        {
            key: "department",
            header: "Department",
            sortValue: (task) => task.department,
            cell: (task) => <span className="text-[12.5px]">{task.department}</span>
        },
        {
            key: "assetType",
            header: "Asset",
            sortValue: (task) => task.assetType,
            cell: (task) => (
                <div className="leading-tight">
                    <div className="text-[12.5px]">{task.assetType}</div>
                    <div className="text-[11px] text-muted">{sectionName(task.sectionId)}</div>
                </div>
            )
        },
        {
            key: "criticality",
            header: "Criticality",
            sortValue: (task) => task.criticality,
            cell: (task) => (
                <StatusChip status={priorityChip(task.criticality)} showDot={false} className="text-[10px] px-1.5 py-0" />
            )
        },
        {
            key: "urgency",
            header: "Urgency",
            sortValue: (task) => task.urgency,
            cell: (task) => (
                <StatusChip status={priorityChip(task.urgency)} showDot={false} className="text-[10px] px-1.5 py-0" />
            )
        },
        {
            key: "crew",
            header: "Crew",
            sortValue: (task) => task.assignedCrew,
            cell: (task) => (
                <div className="leading-tight">
                    <div className="text-[12.5px]">{task.assignedCrew}</div>
                    <div className="text-[11px] text-muted">{task.workforce} on gang</div>
                </div>
            )
        },
        {
            key: "due",
            header: "Due",
            sortValue: (task) => new Date(task.dueDate).getTime(),
            cell: (task) => (
                <span
                    className={`text-[12.5px] whitespace-nowrap ${isOverdue(task.dueDate) && task.status !== "completed" ? "text-red font-semibold" : "text-muted"}`}
                >
                    {formatDate(task.dueDate)}
                </span>
            )
        },
        {
            key: "duration",
            header: "Duration",
            sortValue: (task) => task.durationMinutes,
            cell: (task) => <span className="text-[12.5px] font-mono">{formatDuration(task.durationMinutes)}</span>
        },
        {
            key: "progress",
            header: "Progress",
            sortValue: (task) => task.progressPercent,
            cell: (task) => (
                <div className="flex items-center gap-2 w-[92px]">
                    <div className="h-1.5 flex-1 rounded-full bg-slate-200 overflow-hidden">
                        <div
                            className={`h-full ${task.progressPercent === 100 ? "bg-green" : task.status === "in-progress" ? "bg-blue" : "bg-slate-400"}`}
                            style={{ width: `${task.progressPercent}%` }}
                        />
                    </div>
                    <span className="text-[11px] font-mono text-muted tabular-nums w-8 text-right">
                        {task.progressPercent}%
                    </span>
                </div>
            )
        },
        {
            key: "status",
            header: "Status",
            sortValue: (task) => task.status,
            cell: (task) => (
                <StatusChip status={taskStatusChip(task.status)} showDot={false} className="text-[10px] px-1.5 py-0" />
            )
        }
    ];

    const exportTasks = () =>
        downloadCsv(
            "rmos-maintenance-tasks",
            [
                { key: "taskCode", header: "Task", value: (task) => task.taskCode },
                { key: "workOrder", header: "Work order", value: (task) => task.workOrder },
                { key: "department", header: "Department", value: (task) => task.department },
                { key: "assetType", header: "Asset", value: (task) => task.assetType },
                { key: "sectionId", header: "Section", value: (task) => task.sectionId },
                { key: "criticality", header: "Criticality", value: (task) => task.criticality },
                { key: "urgency", header: "Urgency", value: (task) => task.urgency },
                { key: "dueDate", header: "Due", value: (task) => task.dueDate },
                { key: "durationMinutes", header: "Duration (min)", value: (task) => task.durationMinutes },
                { key: "assignedCrew", header: "Crew", value: (task) => task.assignedCrew },
                { key: "status", header: "Status", value: (task) => task.status },
                { key: "progressPercent", header: "Progress %", value: (task) => task.progressPercent },
                { key: "sourceSystem", header: "Source system", value: (task) => task.sourceSystem }
            ],
            filtered
        );

    const advance = (task: MaintenanceTask) => {
        if (task.status === "pending") {
            updateTask(task.id, { status: "scheduled" });
            toast(`${task.taskCode} moved to SCHEDULED.`);
        } else if (task.status === "scheduled") {
            updateTask(task.id, { status: "in-progress", progressPercent: 10 });
            toast(`${task.taskCode} started by ${task.assignedCrew}.`, "info");
        } else if (task.status === "in-progress") {
            updateTask(task.id, { status: "completed", progressPercent: 100 });
            toast(`${task.taskCode} closed and marked complete.`);
        } else {
            updateTask(task.id, { status: "pending", progressPercent: 0 });
            toast(`${task.taskCode} reopened for re-planning.`, "warning");
        }
    };

    const nextLabel = (task: MaintenanceTask) =>
        task.status === "pending"
            ? "Schedule task"
            : task.status === "scheduled"
              ? "Start work"
              : task.status === "in-progress"
                ? "Mark complete"
                : "Reopen task";

    return (
        <div className="flex flex-col gap-4">
            <KpiStrip items={kpis} />

            <ModuleToolbar
                search={search}
                onSearchChange={setSearch}
                placeholder="Search task, work order, crew, section…"
                resultLabel={`${filtered.length} of ${tasks.length} tasks`}
                onRefresh={refresh}
                onExport={exportTasks}
                exportDisabled={filtered.length === 0}
                filters={
                    <>
                        <FilterSelect label="Dept" value={department} onChange={setDepartment} options={DEPARTMENT_OPTIONS} />
                        <FilterSelect label="Status" value={status} onChange={setStatus} options={STATUS_OPTIONS} />
                        <FilterSelect label="Risk" value={criticality} onChange={setCriticality} options={CRITICALITY_OPTIONS} />
                    </>
                }
            />

            <Card>
                <CardHeader className="pb-3">
                    <div className="flex items-center justify-between gap-3 flex-wrap">
                        <div>
                            <CardTitle className="text-[15px] flex items-center gap-2">
                                <Wrench className="w-4 h-4 text-primary" />
                                Maintenance Work Queue
                            </CardTitle>
                            <CardDescription>
                                Click a row to open the work order detail and advance its status.
                            </CardDescription>
                        </div>
                        <div className="flex items-center gap-2 text-[11.5px] text-muted">
                            <Timer className="w-3.5 h-3.5" />
                            {active.length} active · {overdue.length} overdue
                        </div>
                    </div>
                </CardHeader>
                <CardContent>
                    <DataTable
                        columns={columns}
                        rows={filtered}
                        rowId={(task) => task.id}
                        onRowClick={(task) => setSelectedId(task.id)}
                        selectedRowId={selectedId}
                        isLoading={tasksState.isLoading}
                        errorMessage={tasksState.error?.message ?? null}
                        emptyMessage="No maintenance tasks match the selected filters."
                        initialSort={{ key: "due", direction: "asc" }}
                    />
                </CardContent>
            </Card>

            <Sheet open={!!selected} onOpenChange={(open) => !open && setSelectedId(null)}>
                <SheetContent className="w-full sm:max-w-md overflow-y-auto">
                    {selected && (
                        <>
                            <SheetHeader className="mb-4">
                                <div className="flex items-center gap-2 flex-wrap">
                                    <Badge variant="outline">{selected.department}</Badge>
                                    <StatusChip status={taskStatusChip(selected.status)} showDot={false} />
                                    <StatusChip status={priorityChip(selected.criticality)} showDot={false} />
                                </div>
                                <SheetTitle className="text-xl font-mono">{selected.taskCode}</SheetTitle>
                                <SheetDescription>
                                    {selected.assetType} maintenance in {sectionName(selected.sectionId)} · work order{" "}
                                    <span className="font-mono">{selected.workOrder}</span>
                                </SheetDescription>
                            </SheetHeader>

                            <div className="space-y-5 px-4 pb-4">
                                <div>
                                    <div className="flex items-center justify-between text-[11.5px] text-muted mb-1.5">
                                        <span className="uppercase tracking-[0.4px] font-semibold">Execution progress</span>
                                        <span className="font-mono">{selected.progressPercent}%</span>
                                    </div>
                                    <div className="h-2 rounded-full bg-slate-200 overflow-hidden">
                                        <div
                                            className={`h-full ${selected.progressPercent === 100 ? "bg-green" : "bg-blue"}`}
                                            style={{ width: `${selected.progressPercent}%` }}
                                        />
                                    </div>
                                </div>

                                <div className="grid grid-cols-2 gap-3">
                                    <DetailItem label="Urgency" value={<StatusChip status={priorityChip(selected.urgency)} showDot={false} />} />
                                    <DetailItem label="Duration" value={formatDuration(selected.durationMinutes)} />
                                    <DetailItem label="Due" value={formatDate(selected.dueDate)} />
                                    <DetailItem label="Crew" value={`${selected.assignedCrew} (${selected.workforce})`} />
                                    <DetailItem label="Priority rank" value={`#${selected.priorityRank}`} />
                                    <DetailItem label="Est. cost" value={formatCurrency(selected.estimatedCostInr)} />
                                </div>

                                <div className="border rounded-md p-3 bg-slate-50 space-y-1.5 text-[12.5px]">
                                    <div className="flex justify-between gap-3">
                                        <span className="text-muted">Section</span>
                                        <span>{sectionName(selected.sectionId)}</span>
                                    </div>
                                    <div className="flex justify-between gap-3">
                                        <span className="text-muted">Source system</span>
                                        <span className="font-mono">{selected.sourceSystem}</span>
                                    </div>
                                    <div className="flex justify-between gap-3">
                                        <span className="text-muted">Source record</span>
                                        <span className="font-mono">{selected.sourceRecordId}</span>
                                    </div>
                                    <div className="flex justify-between gap-3">
                                        <span className="text-muted">Freshness</span>
                                        <span>{relativeTime(selected.sourceTimestamp)}</span>
                                    </div>
                                </div>

                                {selected.dependencies && selected.dependencies.length > 0 && (
                                    <div className="space-y-1.5">
                                        <div className="text-[11.5px] uppercase tracking-[0.4px] font-semibold text-muted">
                                            Dependencies
                                        </div>
                                        <div className="flex gap-1.5 flex-wrap">
                                            {selected.dependencies.map((dependency) => (
                                                <Badge key={dependency} variant="secondary" className="font-mono text-[11px]">
                                                    {dependency}
                                                </Badge>
                                            ))}
                                        </div>
                                    </div>
                                )}

                                {selected.notes && (
                                    <p className="text-[12.5px] text-muted border-l-2 border-blue-line pl-3">
                                        {selected.notes}
                                    </p>
                                )}

                                <div className="flex gap-2 pt-1">
                                    <Button className="flex-1" onClick={() => advance(selected)}>
                                        {nextLabel(selected)}
                                    </Button>
                                    <Button
                                        variant="outline"
                                        onClick={() => {
                                            updateTask(selected.id, { status: "pending", progressPercent: 0 });
                                            toast(`${selected.taskCode} reset to pending.`, "warning");
                                        }}
                                    >
                                        Reset
                                    </Button>
                                </div>
                            </div>
                        </>
                    )}
                </SheetContent>
            </Sheet>
        </div>
    );
}


