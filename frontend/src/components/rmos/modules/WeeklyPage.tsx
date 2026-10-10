import { useMemo, useState } from "react";
import {
    CalendarDays,
    ChevronLeft,
    ChevronRight,
    ClipboardList,
    Layers,
    Timer,
    Users
} from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { KpiStrip, type KpiItem } from "@/components/rmos/KpiStrip";
import { FilterSelect, ModuleToolbar } from "@/components/rmos/ModuleToolbar";
import { StatusChip } from "@/components/rmos/StatusChip";
import { useModuleData } from "@/hooks/useModuleData";
import { downloadCsv } from "@/lib/export";
import { priorityChip, taskStatusChip } from "@/lib/status";
import {
    WEEKDAY_LABELS,
    addDays,
    startOfWeek,
    toDayKey,
    weekLabel
} from "@/lib/calendar";
import { formatDuration } from "@/lib/format";
import { DEMO_DEPARTMENTS } from "@/data/demo/mockData";
import type { BlockWindow, MaintenanceTask } from "@/types/models";

const DEPARTMENT_OPTIONS = [
    { value: "all", label: "All departments" },
    ...DEMO_DEPARTMENTS.map((department) => ({ value: department, label: department }))
];

const CRITICALITY_TONE: Record<MaintenanceTask["criticality"], string> = {
    critical: "border-l-red bg-red-bg",
    high: "border-l-amber bg-amber-bg",
    medium: "border-l-blue bg-blue-bg",
    low: "border-l-slate-400 bg-slate-50"
};

export function WeeklyPage() {
    const { tasks: tasksState, blocks: blocksState, sections, refresh } = useModuleData();

    const [weekOffset, setWeekOffset] = useState(0);
    const [department, setDepartment] = useState("all");
    const [search, setSearch] = useState("");
    const [selectedDay, setSelectedDay] = useState<string>(() => toDayKey(new Date()));

    const weekStart = useMemo(() => addDays(startOfWeek(new Date()), weekOffset * 7), [weekOffset]);
    const weekDays = useMemo(
        () => Array.from({ length: 7 }, (_, index) => addDays(weekStart, index)),
        [weekStart]
    );

    const sectionName = (sectionId: string) =>
        sections.data?.find((section) => section.id === sectionId)?.name ?? sectionId;

    const tasks = useMemo(() => {
        const query = search.trim().toLowerCase();
        return (tasksState.data ?? []).filter((task) => {
            if (department !== "all" && task.department !== department) return false;
            if (!query) return true;
            return [task.taskCode, task.assetType, task.sectionId, task.assignedCrew]
                .join(" ")
                .toLowerCase()
                .includes(query);
        });
    }, [tasksState.data, department, search]);

    const blocks = useMemo(() => blocksState.data ?? [], [blocksState.data]);

    const weekTasks = useMemo(
        () =>
            tasks.filter((task) => {
                const key = toDayKey(new Date(task.dueDate));
                return weekDays.some((day) => toDayKey(day) === key);
            }),
        [tasks, weekDays]
    );

    const weekBlocks = useMemo(
        () => blocks.filter((block) => weekDays.some((day) => toDayKey(day) === block.date)),
        [blocks, weekDays]
    );

    const tasksByDay = useMemo(() => {
        const map = new Map<string, MaintenanceTask[]>();
        weekTasks.forEach((task) => {
            const key = toDayKey(new Date(task.dueDate));
            map.set(key, [...(map.get(key) ?? []), task]);
        });
        return map;
    }, [weekTasks]);

    const blocksByDay = useMemo(() => {
        const map = new Map<string, BlockWindow[]>();
        weekBlocks.forEach((block) => {
            map.set(block.date, [...(map.get(block.date) ?? []), block]);
        });
        return map;
    }, [weekBlocks]);

    const loadByDay = useMemo(() => {
        const map = new Map<string, number>();
        weekTasks.forEach((task) => {
            const key = toDayKey(new Date(task.dueDate));
            map.set(key, (map.get(key) ?? 0) + task.durationMinutes);
        });
        return map;
    }, [weekTasks]);

    const labourHours = Math.round(
        weekTasks.reduce((total, task) => total + task.durationMinutes * task.workforce, 0) / 60
    );
    const peak = Array.from(loadByDay.entries()).sort((a, b) => b[1] - a[1])[0];
    const peakDayLabel = peak
        ? new Date(`${peak[0]}T00:00:00`).toLocaleDateString(undefined, { weekday: "long" })
        : "—";

    const selectedTasks = tasksByDay.get(selectedDay) ?? [];
    const selectedBlocks = blocksByDay.get(selectedDay) ?? [];

    const kpis: KpiItem[] = [
        { label: "Tasks This Week", value: weekTasks.length, hint: weekLabel(weekStart), icon: CalendarDays },
        { label: "Block Windows", value: weekBlocks.length, hint: "Possessions scheduled", icon: Layers, tone: "blue" },
        { label: "Labour Hours", value: `${labourHours} h`, hint: "Duration × workforce", icon: Users },
        { label: "Critical Tasks", value: weekTasks.filter((task) => task.criticality === "critical").length, hint: "Highest risk first", icon: ClipboardList, tone: "amber" },
        { label: "Peak Day", value: peakDayLabel.slice(0, 3), hint: peak ? formatDuration(peak[1]) + " of work" : "No work planned", icon: Timer },
        { label: "Completed", value: weekTasks.filter((task) => task.status === "completed").length, hint: "Closed within the week", icon: ClipboardList, tone: "green" }
    ];

    const exportWeek = () =>
        downloadCsv(
            "rmos-weekly-plan",
            [
                { key: "taskCode", header: "Task", value: (task) => task.taskCode },
                { key: "day", header: "Day", value: (task) => toDayKey(new Date(task.dueDate)) },
                { key: "department", header: "Department", value: (task) => task.department },
                { key: "sectionId", header: "Section", value: (task) => task.sectionId },
                { key: "criticality", header: "Criticality", value: (task) => task.criticality },
                { key: "durationMinutes", header: "Duration (min)", value: (task) => task.durationMinutes },
                { key: "status", header: "Status", value: (task) => task.status }
            ],
            weekTasks
        );

    return (
        <div className="flex flex-col gap-4">
            <KpiStrip items={kpis} />

            <ModuleToolbar
                search={search}
                onSearchChange={setSearch}
                placeholder="Search task or section in this week…"
                resultLabel={`${weekTasks.length} tasks · ${weekBlocks.length} blocks`}
                onRefresh={refresh}
                onExport={exportWeek}
                exportDisabled={weekTasks.length === 0}
                filters={
                    <FilterSelect
                        label="Dept"
                        value={department}
                        onChange={setDepartment}
                        options={DEPARTMENT_OPTIONS}
                    />
                }
                actions={
                    <div className="flex items-center gap-1.5">
                        <Button
                            variant="outline"
                            size="icon-xs"
                            onClick={() => {
                                setWeekOffset((value) => value - 1);
                                setSelectedDay(toDayKey(addDays(startOfWeek(new Date()), (weekOffset - 1) * 7)));
                            }}
                            aria-label="Previous week"
                        >
                            <ChevronLeft className="w-3.5 h-3.5" />
                        </Button>
                        <Button
                            variant="outline"
                            size="sm"
                            className="h-8"
                            onClick={() => {
                                setWeekOffset(0);
                                setSelectedDay(toDayKey(new Date()));
                            }}
                        >
                            This week
                        </Button>
                        <Button
                            variant="outline"
                            size="icon-xs"
                            onClick={() => {
                                setWeekOffset((value) => value + 1);
                                setSelectedDay(toDayKey(addDays(startOfWeek(new Date()), (weekOffset + 1) * 7)));
                            }}
                            aria-label="Next week"
                        >
                            <ChevronRight className="w-3.5 h-3.5" />
                        </Button>
                        <span className="text-[12.5px] font-semibold text-ink min-w-[190px] text-center">
                            {weekLabel(weekStart)}
                        </span>
                    </div>
                }
            />

            <Card>
                <CardHeader className="pb-3">
                    <div className="flex items-center justify-between gap-3 flex-wrap">
                        <div>
                            <CardTitle className="text-[15px] flex items-center gap-2">
                                <CalendarDays className="w-4 h-4 text-primary" />
                                Seven-Day Planning Board
                            </CardTitle>
                            <CardDescription>
                                Tasks are placed by due date, blocks by possession date. Click a day to inspect it.
                            </CardDescription>
                        </div>
                        <div className="flex items-center gap-3 text-[11.5px] text-muted">
                            <span className="flex items-center gap-1">
                                <span className="w-2 h-2 rounded-sm bg-red" /> Critical
                            </span>
                            <span className="flex items-center gap-1">
                                <span className="w-2 h-2 rounded-sm bg-amber" /> High
                            </span>
                            <span className="flex items-center gap-1">
                                <span className="w-2 h-2 rounded-sm bg-blue" /> Medium
                            </span>
                        </div>
                    </div>
                </CardHeader>
                <CardContent>
                    <div className="grid grid-cols-2 sm:grid-cols-4 lg:grid-cols-7 gap-2">
                        {weekDays.map((day, index) => {
                            const key = toDayKey(day);
                            const dayTasks = tasksByDay.get(key) ?? [];
                            const dayBlocks = blocksByDay.get(key) ?? [];
                            const load = loadByDay.get(key) ?? 0;
                            const loadPercent = Math.min(100, Math.round((load / 720) * 100));
                            const isSelected = key === selectedDay;
                            const isToday = key === toDayKey(new Date());

                            return (
                                <button
                                    key={key}
                                    type="button"
                                    onClick={() => setSelectedDay(key)}
                                    className={`text-left border rounded-md p-2.5 min-h-[168px] transition-colors flex flex-col gap-2 ${
                                        isSelected
                                            ? "border-blue ring-2 ring-blue-line bg-blue-bg/40"
                                            : "bg-surface hover:bg-slate-50"
                                    }`}
                                >
                                    <div className="flex items-center justify-between gap-1">
                                        <div>
                                            <div
                                                className={`text-[11px] uppercase tracking-[0.5px] ${isToday ? "text-blue font-bold" : "text-muted"}`}
                                            >
                                                {WEEKDAY_LABELS[index]}
                                            </div>
                                            <div className="text-[15px] font-bold leading-tight">
                                                {day.getDate()}
                                            </div>
                                        </div>
                                        <div className="flex flex-col items-end gap-1">
                                            <Badge
                                                variant="outline"
                                                className="font-mono text-[10px] px-1.5 py-0"
                                            >
                                                {dayTasks.length}T
                                            </Badge>
                                            {dayBlocks.length > 0 && (
                                                <Badge
                                                    variant="secondary"
                                                    className="font-mono text-[10px] px-1.5 py-0"
                                                >
                                                    {dayBlocks.length}B
                                                </Badge>
                                            )}
                                        </div>
                                    </div>

                                    <div className="h-1.5 rounded-full bg-slate-200 overflow-hidden">
                                        <div
                                            className={`h-full ${loadPercent > 75 ? "bg-red" : loadPercent > 40 ? "bg-amber" : "bg-green"}`}
                                            style={{ width: `${loadPercent}%` }}
                                        />
                                    </div>

                                    <div className="flex flex-col gap-1 overflow-hidden">
                                        {dayTasks.slice(0, 4).map((task) => (
                                            <div
                                                key={task.id}
                                                className={`border-l-2 rounded-sm px-1.5 py-1 text-[10.5px] leading-tight ${CRITICALITY_TONE[task.criticality]}`}
                                            >
                                                <span className="font-mono font-semibold">{task.taskCode}</span>
                                                <span className="text-muted"> · {task.department}</span>
                                            </div>
                                        ))}
                                        {dayTasks.length > 4 && (
                                            <span className="text-[10.5px] text-muted px-1">
                                                +{dayTasks.length - 4} more
                                            </span>
                                        )}
                                        {dayTasks.length === 0 && dayBlocks.length === 0 && (
                                            <span className="text-[10.5px] text-muted italic">No planned work</span>
                                        )}
                                    </div>
                                </button>
                            );
                        })}
                    </div>
                </CardContent>
            </Card>

            <Card>
                <CardHeader className="pb-3">
                    <div className="flex items-center justify-between gap-3 flex-wrap">
                        <div>
                            <CardTitle className="text-[15px] flex items-center gap-2">
                                <ClipboardList className="w-4 h-4 text-primary" />
                                {new Date(`${selectedDay}T00:00:00`).toLocaleDateString(undefined, {
                                    weekday: "long",
                                    day: "2-digit",
                                    month: "long",
                                    year: "numeric"
                                })}
                            </CardTitle>
                            <CardDescription>
                                {selectedTasks.length} tasks · {selectedBlocks.length} block windows ·{" "}
                                {formatDuration(loadByDay.get(selectedDay) ?? 0)} of planned work
                            </CardDescription>
                        </div>
                    </div>
                </CardHeader>
                <CardContent className="space-y-3">
                    {selectedTasks.length === 0 && selectedBlocks.length === 0 && (
                        <div className="border border-dashed rounded-md py-8 text-center text-sm text-muted">
                            Nothing planned for this day. Use the week navigation to pick another day.
                        </div>
                    )}

                    {selectedTasks.map((task) => (
                        <div
                            key={task.id}
                            className="flex items-center justify-between gap-3 border rounded-md px-3 py-2.5"
                        >
                            <div className="min-w-0">
                                <div className="flex items-center gap-2 flex-wrap">
                                    <span className="font-mono text-[12.5px] font-semibold">{task.taskCode}</span>
                                    <StatusChip status={priorityChip(task.criticality)} showDot={false} className="text-[10px] px-1.5 py-0" />
                                    <StatusChip status={taskStatusChip(task.status)} showDot={false} className="text-[10px] px-1.5 py-0" />
                                </div>
                                <div className="text-[11.5px] text-muted mt-0.5 truncate">
                                    {task.department} · {task.assetType} · {sectionName(task.sectionId)} ·{" "}
                                    {task.assignedCrew}
                                </div>
                            </div>
                            <div className="text-right shrink-0">
                                <div className="text-[12.5px] font-mono">{formatDuration(task.durationMinutes)}</div>
                                <div className="text-[11px] text-muted">{task.workforce} staff</div>
                            </div>
                        </div>
                    ))}

                    {selectedBlocks.map((block) => (
                        <div
                            key={block.id}
                            className="flex items-center justify-between gap-3 border border-blue-line bg-blue-bg/40 rounded-md px-3 py-2.5"
                        >
                            <div>
                                <div className="font-mono text-[12.5px] font-semibold">
                                    {block.blockCode}{" "}
                                    <span className="text-muted font-normal">· {sectionName(block.sectionId)}</span>
                                </div>
                                <div className="text-[11.5px] text-muted">
                                    {block.department} · {block.assignedTaskIds.length} linked tasks ·{" "}
                                    {block.notes ?? "no notes"}
                                </div>
                            </div>
                            <div className="font-mono text-[12.5px]">
                                {block.start}–{block.end}
                            </div>
                        </div>
                    ))}
                </CardContent>
            </Card>
        </div>
    );
}
