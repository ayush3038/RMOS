import { useMemo, useState } from "react";
import {
    CalendarRange,
    ChevronLeft,
    ChevronRight,
    ClipboardList,
    Flame,
    Layers,
    ListChecks
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
import { WEEKDAY_LABELS, monthGrid, monthLabel, startOfMonth, toDayKey } from "@/lib/calendar";
import { formatDuration } from "@/lib/format";
import { DEMO_DEPARTMENTS } from "@/data/demo/mockData";
import type { MaintenanceTask } from "@/types/models";

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

const HEAT_CLASSES = ["bg-surface", "bg-blue-bg", "bg-blue-line", "bg-amber-line", "bg-red-line"];

const heatLevel = (minutes: number): number => {
    if (minutes === 0) return 0;
    if (minutes <= 120) return 1;
    if (minutes <= 300) return 2;
    if (minutes <= 540) return 3;
    return 4;
};

export function MonthlyPage() {
    const { tasks: tasksState, blocks: blocksState, sections, refresh } = useModuleData();

    const [monthOffset, setMonthOffset] = useState(0);
    const [department, setDepartment] = useState("all");
    const [criticality, setCriticality] = useState("all");
    const [search, setSearch] = useState("");
    const [selectedDay, setSelectedDay] = useState<string>(() => toDayKey(new Date()));

    const monthDate = useMemo(
        () => startOfMonth(new Date(new Date().getFullYear(), new Date().getMonth() + monthOffset, 1)),
        [monthOffset]
    );
    const cells = useMemo(() => monthGrid(monthDate), [monthDate]);

    const sectionName = (sectionId: string) =>
        sections.data?.find((section) => section.id === sectionId)?.name ?? sectionId;

    const tasks = useMemo(() => {
        const query = search.trim().toLowerCase();
        return (tasksState.data ?? []).filter((task) => {
            if (department !== "all" && task.department !== department) return false;
            if (criticality !== "all" && task.criticality !== criticality) return false;
            if (!query) return true;
            return [task.taskCode, task.assetType, task.sectionId, task.assignedCrew]
                .join(" ")
                .toLowerCase()
                .includes(query);
        });
    }, [tasksState.data, department, criticality, search]);

    const blocks = useMemo(() => blocksState.data ?? [], [blocksState.data]);

    const monthTasks = useMemo(
        () =>
            tasks.filter((task) => {
                const date = new Date(task.dueDate);
                return (
                    date.getMonth() === monthDate.getMonth() &&
                    date.getFullYear() === monthDate.getFullYear()
                );
            }),
        [tasks, monthDate]
    );

    const monthBlocks = useMemo(
        () =>
            blocks.filter((block) => {
                const date = new Date(`${block.date}T00:00:00`);
                return (
                    date.getMonth() === monthDate.getMonth() &&
                    date.getFullYear() === monthDate.getFullYear()
                );
            }),
        [blocks, monthDate]
    );

    const loadByDay = useMemo(() => {
        const map = new Map<string, number>();
        monthTasks.forEach((task) => {
            const key = toDayKey(new Date(task.dueDate));
            map.set(key, (map.get(key) ?? 0) + task.durationMinutes);
        });
        return map;
    }, [monthTasks]);

    const countByDay = useMemo(() => {
        const map = new Map<string, MaintenanceTask[]>();
        monthTasks.forEach((task) => {
            const key = toDayKey(new Date(task.dueDate));
            map.set(key, [...(map.get(key) ?? []), task]);
        });
        return map;
    }, [monthTasks]);

    const busiest = Array.from(countByDay.entries()).sort((a, b) => b[1].length - a[1].length)[0];
    const busiestLabel = busiest
        ? new Date(`${busiest[0]}T00:00:00`).toLocaleDateString(undefined, {
              day: "2-digit",
              month: "short"
          })
        : "—";
    const freeDays = Array.from(countByDay.keys()).length;

    const selectedTasks = countByDay.get(selectedDay) ?? [];
    const selectedBlocks = monthBlocks.filter((block) => block.date === selectedDay);

    const kpis: KpiItem[] = [
        { label: "Tasks In Month", value: monthTasks.length, hint: monthLabel(monthDate), icon: ListChecks },
        { label: "Block Windows", value: monthBlocks.length, hint: "Possessions on calendar", icon: Layers, tone: "blue" },
        { label: "Critical Tasks", value: monthTasks.filter((task) => task.criticality === "critical").length, hint: "Highest risk work", icon: Flame, tone: "amber" },
        { label: "Busiest Day", value: busiestLabel, hint: busiest ? `${busiest[1].length} tasks due` : "No tasks", icon: CalendarRange },
        { label: "Active Days", value: freeDays, hint: "Days carrying work", icon: ClipboardList, tone: "green" },
        { label: "Planned Hours", value: `${Math.round(monthTasks.reduce((total, task) => total + task.durationMinutes, 0) / 60)} h`, hint: "Gross task duration", icon: ClipboardList }
    ];

    const exportMonth = () =>
        downloadCsv(
            "rmos-monthly-plan",
            [
                { key: "taskCode", header: "Task", value: (task) => task.taskCode },
                { key: "dueDate", header: "Due", value: (task) => task.dueDate },
                { key: "department", header: "Department", value: (task) => task.department },
                { key: "sectionId", header: "Section", value: (task) => task.sectionId },
                { key: "criticality", header: "Criticality", value: (task) => task.criticality },
                { key: "durationMinutes", header: "Duration (min)", value: (task) => task.durationMinutes },
                { key: "status", header: "Status", value: (task) => task.status }
            ],
            monthTasks
        );

    return (
        <div className="flex flex-col gap-4">
            <KpiStrip items={kpis} />

            <ModuleToolbar
                search={search}
                onSearchChange={setSearch}
                placeholder="Search task or section in this month…"
                resultLabel={`${monthTasks.length} tasks · ${monthBlocks.length} blocks`}
                onRefresh={refresh}
                onExport={exportMonth}
                exportDisabled={monthTasks.length === 0}
                filters={
                    <>
                        <FilterSelect label="Dept" value={department} onChange={setDepartment} options={DEPARTMENT_OPTIONS} />
                        <FilterSelect label="Risk" value={criticality} onChange={setCriticality} options={CRITICALITY_OPTIONS} />
                    </>
                }
                actions={
                    <div className="flex items-center gap-1.5">
                        <Button
                            variant="outline"
                            size="icon-xs"
                            onClick={() => setMonthOffset((value) => value - 1)}
                            aria-label="Previous month"
                        >
                            <ChevronLeft className="w-3.5 h-3.5" />
                        </Button>
                        <Button
                            variant="outline"
                            size="sm"
                            className="h-8"
                            onClick={() => {
                                setMonthOffset(0);
                                setSelectedDay(toDayKey(new Date()));
                            }}
                        >
                            This month
                        </Button>
                        <Button
                            variant="outline"
                            size="icon-xs"
                            onClick={() => setMonthOffset((value) => value + 1)}
                            aria-label="Next month"
                        >
                            <ChevronRight className="w-3.5 h-3.5" />
                        </Button>
                        <span className="text-[12.5px] font-semibold text-ink min-w-[150px] text-center">
                            {monthLabel(monthDate)}
                        </span>
                    </div>
                }
            />

            <Card>
                <CardHeader className="pb-3">
                    <div className="flex items-center justify-between gap-3 flex-wrap">
                        <div>
                            <CardTitle className="text-[15px] flex items-center gap-2">
                                <CalendarRange className="w-4 h-4 text-primary" />
                                Monthly Load Heatmap
                            </CardTitle>
                            <CardDescription>
                                Shading shows planned minutes per day · click any day for its work list.
                            </CardDescription>
                        </div>
                        <div className="flex items-center gap-2 text-[11px] text-muted">
                            <span>Light</span>
                            {HEAT_CLASSES.map((className) => (
                                <span key={className} className={`w-5 h-3.5 rounded-sm border border-border ${className}`} />
                            ))}
                            <span>Heavy</span>
                        </div>
                    </div>
                </CardHeader>
                <CardContent>
                    <div className="grid grid-cols-7 gap-1.5 mb-1.5">
                        {WEEKDAY_LABELS.map((label) => (
                            <div
                                key={label}
                                className="text-[11px] uppercase tracking-[0.5px] text-muted text-center font-semibold"
                            >
                                {label}
                            </div>
                        ))}
                    </div>

                    <div className="grid grid-cols-7 gap-1.5">
                        {cells.map((day) => {
                            const key = toDayKey(day);
                            const dayTasks = countByDay.get(key) ?? [];
                            const load = loadByDay.get(key) ?? 0;
                            const inMonth = day.getMonth() === monthDate.getMonth();
                            const isSelected = key === selectedDay;
                            const isToday = key === toDayKey(new Date());

                            return (
                                <button
                                    key={key}
                                    type="button"
                                    onClick={() => setSelectedDay(key)}
                                    className={`relative text-left border rounded-md p-1.5 min-h-[78px] transition-all ${HEAT_CLASSES[heatLevel(load)]} ${
                                        isSelected
                                            ? "border-blue ring-2 ring-blue-line"
                                            : "border-border hover:border-blue-line"
                                    } ${inMonth ? "opacity-100" : "opacity-45"}`}
                                >
                                    <div className="flex items-start justify-between gap-1">
                                        <span
                                            className={`text-[12px] font-semibold leading-none ${isToday ? "text-blue" : "text-ink"}`}
                                        >
                                            {day.getDate()}
                                        </span>
                                        {dayTasks.length > 0 && (
                                            <span className="text-[10px] font-mono text-muted">
                                                {dayTasks.length}
                                            </span>
                                        )}
                                    </div>

                                    <div className="mt-1.5 flex gap-0.5 flex-wrap">
                                        {dayTasks.slice(0, 5).map((task) => (
                                            <span
                                                key={task.id}
                                                className={`w-1.5 h-1.5 rounded-full ${
                                                    task.criticality === "critical"
                                                        ? "bg-red"
                                                        : task.criticality === "high"
                                                          ? "bg-amber"
                                                          : task.criticality === "medium"
                                                            ? "bg-blue"
                                                            : "bg-muted"
                                                }`}
                                            />
                                        ))}
                                    </div>

                                    {load > 0 && (
                                        <div className="absolute bottom-1.5 left-1.5 right-1.5">
                                            <div className="h-1 rounded-full bg-white/70 overflow-hidden">
                                                <div
                                                    className="h-full bg-ink/50"
                                                    style={{ width: `${Math.min(100, (load / 720) * 100)}%` }}
                                                />
                                            </div>
                                        </div>
                                    )}
                                </button>
                            );
                        })}
                    </div>
                </CardContent>
            </Card>

            <Card>
                <CardHeader className="pb-3">
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
                        {formatDuration(selectedTasks.reduce((total, task) => total + task.durationMinutes, 0))} of
                        planned work
                    </CardDescription>
                </CardHeader>
                <CardContent className="space-y-3">
                    {selectedTasks.length === 0 && selectedBlocks.length === 0 && (
                        <div className="border border-dashed rounded-md py-8 text-center text-sm text-muted">
                            No work planned on this day.
                            {selectedTasks.length === 0 && monthTasks.length > 0 && (
                                <div className="mt-1 text-[12px]">
                                    {monthTasks.length} tasks exist elsewhere in {monthLabel(monthDate)}.
                                </div>
                            )}
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
                                    {task.department} · {task.assetType} · {sectionName(task.sectionId)}
                                </div>
                            </div>
                            <div className="text-right shrink-0">
                                <div className="text-[12.5px] font-mono">{formatDuration(task.durationMinutes)}</div>
                                <div className="text-[11px] text-muted">{task.assignedCrew}</div>
                            </div>
                        </div>
                    ))}

                    {selectedBlocks.map((block) => (
                        <div
                            key={block.id}
                            className="flex items-center justify-between gap-3 border border-blue-line bg-blue-bg/40 rounded-md px-3 py-2.5"
                        >
                            <div>
                                <div className="font-mono text-[12.5px] font-semibold">{block.blockCode}</div>
                                <div className="text-[11.5px] text-muted">
                                    {sectionName(block.sectionId)} · {block.department}
                                </div>
                            </div>
                            <div className="flex items-center gap-2">
                                <Badge variant="outline" className="font-mono text-[10.5px]">
                                    {block.status}
                                </Badge>
                                <span className="font-mono text-[12.5px]">
                                    {block.start}–{block.end}
                                </span>
                            </div>
                        </div>
                    ))}
                </CardContent>
            </Card>
        </div>
    );
}
