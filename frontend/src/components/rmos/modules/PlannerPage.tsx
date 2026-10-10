import { useMemo, useState } from "react";
import { CalendarPlus, CheckCircle2, CalendarClock, Layers, Timer, ListChecks, XCircle } from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { DataTable, type DataTableColumn } from "@/components/rmos/DataTable";
import { KpiStrip, type KpiItem } from "@/components/rmos/KpiStrip";
import { FilterSelect, ModuleToolbar } from "@/components/rmos/ModuleToolbar";
import { StatusChip } from "@/components/rmos/StatusChip";
import { useModuleData } from "@/hooks/useModuleData";
import { useLocalList } from "@/hooks/useLocalList";
import { useToast } from "@/store/toast";
import { downloadCsv } from "@/lib/export";
import { blockStatusChip, priorityChip, taskStatusChip } from "@/lib/status";
import { formatDate, formatDuration } from "@/lib/format";
import { DEMO_DEPARTMENTS } from "@/data/demo/mockData";
import type { BlockWindow, MaintenanceTask } from "@/types/models";

const CRITICALITY_OPTIONS = [
    { value: "all", label: "All criticality" },
    { value: "critical", label: "Critical" },
    { value: "high", label: "High" },
    { value: "medium", label: "Medium" },
    { value: "low", label: "Low" }
];

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

export function PlannerPage() {
    const { tasks: tasksState, blocks: blocksState, sections, refresh } = useModuleData();
    const { toast } = useToast();

    const { items: tasks, update: updateTask } = useLocalList<MaintenanceTask>(
        tasksState.data,
        (task) => task.id
    );
    const { items: blocks, update: updateBlock } = useLocalList<BlockWindow>(
        blocksState.data,
        (block) => block.id
    );

    const [search, setSearch] = useState("");
    const [department, setDepartment] = useState("all");
    const [status, setStatus] = useState("all");
    const [criticality, setCriticality] = useState("all");

    const sectionName = (sectionId: string) =>
        sections.data?.find((section) => section.id === sectionId)?.name ?? sectionId;

    const filteredTasks = useMemo(() => {
        const query = search.trim().toLowerCase();
        return tasks.filter((task) => {
            if (department !== "all" && task.department !== department) return false;
            if (status !== "all" && task.status !== status) return false;
            if (criticality !== "all" && task.criticality !== criticality) return false;
            if (!query) return true;
            return [task.taskCode, task.assetType, task.sectionId, task.assignedCrew, task.workOrder]
                .join(" ")
                .toLowerCase()
                .includes(query);
        });
    }, [tasks, search, department, status, criticality]);

    const sortedBlocks = useMemo(
        () =>
            [...blocks].sort((first, second) =>
                `${first.date}${first.start}`.localeCompare(`${second.date}${second.start}`)
            ),
        [blocks]
    );

    const pendingTasks = tasks.filter((task) => task.status === "pending");
    const proposedBlocks = blocks.filter((block) => block.status === "proposed");
    const approvedBlocks = blocks.filter(
        (block) => block.status === "approved" || block.status === "utilized"
    );
    const candidateMinutes = pendingTasks.reduce((total, task) => total + task.durationMinutes, 0);

    const kpis: KpiItem[] = [
        { label: "Candidate Tasks", value: pendingTasks.length, hint: "Awaiting window assignment", icon: ListChecks },
        { label: "Proposed Windows", value: proposedBlocks.length, hint: "Pending approval", icon: CalendarClock, tone: "amber" },
        { label: "Approved Windows", value: approvedBlocks.length, hint: "Released to planning", icon: CheckCircle2, tone: "green" },
        { label: "Scheduled Work", value: tasks.filter((task) => task.status === "scheduled" || task.status === "in-progress").length, hint: "Committed in plan", icon: CalendarPlus, tone: "blue" },
        { label: "Workload Queued", value: formatDuration(candidateMinutes), hint: "Across candidate tasks", icon: Timer },
        { label: "Sections Covered", value: new Set(pendingTasks.map((task) => task.sectionId)).size, hint: "With open candidates", icon: Layers }
    ];

    const columns: DataTableColumn<MaintenanceTask>[] = [
        {
            key: "taskCode",
            header: "Task",
            sortValue: (task) => task.taskCode,
            cell: (task) => (
                <div className="leading-tight">
                    <div className="font-mono text-[12.5px] font-semibold">{task.taskCode}</div>
                    <div className="text-[11px] text-muted">{task.workOrder}</div>
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
            key: "asset",
            header: "Asset / Section",
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
            cell: (task) => <StatusChip status={priorityChip(task.criticality)} showDot={false} className="text-[10px] px-1.5 py-0" />
        },
        {
            key: "urgency",
            header: "Urgency",
            sortValue: (task) => task.urgency,
            cell: (task) => <StatusChip status={priorityChip(task.urgency)} showDot={false} className="text-[10px] px-1.5 py-0" />
        },
        {
            key: "duration",
            header: "Duration",
            sortValue: (task) => task.durationMinutes,
            cell: (task) => <span className="text-[12.5px] font-mono">{formatDuration(task.durationMinutes)}</span>
        },
        {
            key: "due",
            header: "Due",
            sortValue: (task) => new Date(task.dueDate).getTime(),
            cell: (task) => (
                <span className="text-[12.5px] text-muted whitespace-nowrap">{formatDate(task.dueDate)}</span>
            )
        },
        {
            key: "status",
            header: "State",
            sortValue: (task) => task.status,
            cell: (task) => <StatusChip status={taskStatusChip(task.status)} showDot={false} className="text-[10px] px-1.5 py-0" />
        },
        {
            key: "action",
            header: "",
            headerClassName: "w-[110px]",
            cell: (task) =>
                task.status === "pending" ? (
                    <Button
                        size="xs"
                        variant="outline"
                        onClick={(event) => {
                            event.stopPropagation();
                            updateTask(task.id, { status: "scheduled" });
                            toast(`${task.taskCode} assigned to the next available block window.`);
                        }}
                    >
                        Assign
                    </Button>
                ) : task.status === "scheduled" ? (
                    <Button
                        size="xs"
                        variant="ghost"
                        onClick={(event) => {
                            event.stopPropagation();
                            updateTask(task.id, { status: "in-progress", progressPercent: 15 });
                            toast(`${task.taskCode} released to field execution.`, "info");
                        }}
                    >
                        Release
                    </Button>
                ) : (
                    <span className="text-[11.5px] text-muted">—</span>
                )
        }
    ];

    const exportTasks = () =>
        downloadCsv(
            "rmos-block-planner-candidates",
            [
                { key: "taskCode", header: "Task", value: (task) => task.taskCode },
                { key: "department", header: "Department", value: (task) => task.department },
                { key: "assetType", header: "Asset", value: (task) => task.assetType },
                { key: "sectionId", header: "Section", value: (task) => task.sectionId },
                { key: "criticality", header: "Criticality", value: (task) => task.criticality },
                { key: "dueDate", header: "Due", value: (task) => task.dueDate },
                { key: "durationMinutes", header: "Duration (min)", value: (task) => task.durationMinutes },
                { key: "status", header: "Status", value: (task) => task.status }
            ],
            filteredTasks
        );

    return (
        <div className="flex flex-col gap-4">
            <KpiStrip items={kpis} />

            <ModuleToolbar
                search={search}
                onSearchChange={setSearch}
                placeholder="Search task, work order, section…"
                resultLabel={`${filteredTasks.length} of ${tasks.length} candidates`}
                onRefresh={refresh}
                onExport={exportTasks}
                exportDisabled={filteredTasks.length === 0}
                filters={
                    <>
                        <FilterSelect label="Dept" value={department} onChange={setDepartment} options={DEPARTMENT_OPTIONS} />
                        <FilterSelect label="Status" value={status} onChange={setStatus} options={STATUS_OPTIONS} />
                        <FilterSelect label="Risk" value={criticality} onChange={setCriticality} options={CRITICALITY_OPTIONS} />
                    </>
                }
            />

            <div className="grid gap-4 xl:grid-cols-3">
                <div className="xl:col-span-2">
                    <Card>
                        <CardHeader className="pb-3">
                            <CardTitle className="text-[15px] flex items-center gap-2">
                                <ListChecks className="w-4 h-4 text-primary" />
                                Candidate Planning Inputs
                            </CardTitle>
                            <CardDescription>
                                Ranked maintenance candidates ready to be placed into block windows.
                            </CardDescription>
                        </CardHeader>
                        <CardContent>
                            <DataTable
                                columns={columns}
                                rows={filteredTasks}
                                rowId={(task) => task.id}
                                isLoading={tasksState.isLoading}
                                errorMessage={tasksState.error?.message ?? null}
                                emptyMessage="No candidates match the selected filters."
                                initialSort={{ key: "due", direction: "asc" }}
                            />
                        </CardContent>
                    </Card>
                </div>

                <Card className="h-fit">
                    <CardHeader className="pb-3">
                        <div className="flex items-center justify-between gap-2">
                            <div>
                                <CardTitle className="text-[15px] flex items-center gap-2">
                                    <CalendarClock className="w-4 h-4 text-primary" />
                                    Block Windows
                                </CardTitle>
                                <CardDescription>Proposed and sanctioned possession windows.</CardDescription>
                            </div>
                            <Badge variant="outline" className="font-mono text-[11px]">
                                {blocks.length}
                            </Badge>
                        </div>
                    </CardHeader>
                    <CardContent className="space-y-2.5 max-h-[560px] overflow-y-auto pr-1">
                        {blocksState.isLoading && (
                            <div className="space-y-2">
                                {[1, 2, 3, 4].map((item) => (
                                    <div key={item} className="h-20 animate-pulse rounded-md bg-slate-100" />
                                ))}
                            </div>
                        )}
                        {!blocksState.isLoading &&
                            sortedBlocks.map((block) => {
                                const load = Math.round(
                                    (block.assignedTaskIds.length / Math.max(1, block.capacityTrains)) * 100
                                );
                                return (
                                    <div
                                        key={block.id}
                                        className="border rounded-md p-3 bg-slate-50/60 hover:bg-slate-50 transition-colors"
                                    >
                                        <div className="flex items-start justify-between gap-2">
                                            <div>
                                                <div className="font-mono text-[12.5px] font-semibold">
                                                    {block.blockCode}
                                                </div>
                                                <div className="text-[11.5px] text-muted">
                                                    {sectionName(block.sectionId)} · {block.department}
                                                </div>
                                            </div>
                                            <StatusChip
                                                status={blockStatusChip(block.status)}
                                                showDot={false}
                                                className="text-[10px] px-1.5 py-0"
                                            />
                                        </div>

                                        <div className="flex items-center justify-between gap-2 mt-2 text-[11.5px]">
                                            <span className="font-mono">
                                                {block.date} · {block.start}–{block.end}
                                            </span>
                                            <span className="text-muted">
                                                {block.assignedTaskIds.length} tasks linked
                                            </span>
                                        </div>

                                        <div className="mt-2 h-1.5 rounded-full bg-slate-200 overflow-hidden">
                                            <div
                                                className={`h-full ${load > 80 ? "bg-red" : load > 50 ? "bg-amber" : "bg-green"}`}
                                                style={{ width: `${Math.min(100, load)}%` }}
                                            />
                                        </div>

                                        <div className="flex items-center gap-2 mt-2.5">
                                            {block.status === "proposed" && (
                                                <>
                                                    <Button
                                                        size="xs"
                                                        onClick={() => {
                                                            updateBlock(block.id, {
                                                                status: "approved",
                                                                approvedBy: "You (Demo Planner)"
                                                            });
                                                            toast(`${block.blockCode} approved and released to the operating department.`);
                                                        }}
                                                    >
                                                        Approve
                                                    </Button>
                                                    <Button
                                                        size="xs"
                                                        variant="outline"
                                                        onClick={() => {
                                                            updateBlock(block.id, { status: "rejected" });
                                                            toast(`${block.blockCode} returned to the requesting department.`, "warning");
                                                        }}
                                                    >
                                                        <XCircle className="w-3 h-3" />
                                                        Return
                                                    </Button>
                                                </>
                                            )}
                                            {block.status === "approved" && (
                                                <span className="text-[11.5px] text-green">
                                                    Approved by {block.approvedBy ?? "planner"}
                                                </span>
                                            )}
                                            {(block.status === "rejected" || block.status === "released" || block.status === "utilized") && (
                                                <span className="text-[11.5px] text-muted">
                                                    {block.notes ?? "Closed window"}
                                                </span>
                                            )}
                                        </div>
                                    </div>
                                );
                            })}
                    </CardContent>
                </Card>
            </div>
        </div>
    );
}
