import { useMemo, useState } from "react";
import {
    Activity,
    BarChart3,
    Gauge,
    Layers,
    ListTodo,
    ShieldAlert,
    Train
} from "lucide-react";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { KpiStrip, type KpiItem } from "@/components/rmos/KpiStrip";
import { ModuleToolbar } from "@/components/rmos/ModuleToolbar";
import { useModuleData } from "@/hooks/useModuleData";
import { downloadCsv } from "@/lib/export";
import { DEMO_WEEKLY_TREND } from "@/data/demo/mockData";

type TrendSeries = "planned" | "completed" | "delays";

const SERIES_LABEL: Record<TrendSeries, string> = {
    planned: "Planned",
    completed: "Completed",
    delays: "Delays"
};

const SERIES_COLOR: Record<TrendSeries, string> = {
    planned: "#2A5C8A",
    completed: "#2A6E4E",
    delays: "#A6332C"
};

interface BarDatum {
    label: string;
    value: number;
    tone?: string;
}

function BarChart({ title, description, data, suffix }: { title: string; description: string; data: BarDatum[]; suffix?: string }) {
    const max = Math.max(1, ...data.map((entry) => entry.value));

    return (
        <Card>
            <CardHeader className="pb-3">
                <CardTitle className="text-[14px]">{title}</CardTitle>
                <CardDescription>{description}</CardDescription>
            </CardHeader>
            <CardContent className="space-y-2.5">
                {data.map((entry) => (
                    <div key={entry.label} className="flex items-center gap-3">
                        <span className="text-[12px] w-[128px] shrink-0 truncate">{entry.label}</span>
                        <div className="flex-1 h-3.5 rounded-sm bg-slate-100 overflow-hidden">
                            <div
                                className={`h-full ${entry.tone ?? "bg-blue"}`}
                                style={{ width: `${(entry.value / max) * 100}%` }}
                            />
                        </div>
                        <span className="text-[12px] font-mono tabular-nums w-10 text-right">
                            {entry.value}
                            {suffix ?? ""}
                        </span>
                    </div>
                ))}
            </CardContent>
        </Card>
    );
}

export function AnalyticsPage() {
    const {
        kpis: kpisState,
        tasks: tasksState,
        conflicts: conflictsState,
        trains: trainsState,
        assets: assetsState,
        sections: sectionsState,
        refresh
    } = useModuleData();

    const [search, setSearch] = useState("");
    const [series, setSeries] = useState<TrendSeries>("planned");

    const tasks = useMemo(() => tasksState.data ?? [], [tasksState.data]);
    const conflicts = useMemo(() => conflictsState.data ?? [], [conflictsState.data]);
    const trains = useMemo(() => trainsState.data ?? [], [trainsState.data]);
    const assets = useMemo(() => assetsState.data ?? [], [assetsState.data]);
    const sections = useMemo(() => sectionsState.data ?? [], [sectionsState.data]);
    const kpis = kpisState.data;

    const query = search.trim().toLowerCase();
    const matches = (value: string) => !query || value.toLowerCase().includes(query);

    const byDepartment = useMemo(() => {
        const map = new Map<string, number>();
        tasks.filter((task) => matches(task.department) || matches(task.taskCode)).forEach((task) => {
            map.set(task.department, (map.get(task.department) ?? 0) + 1);
        });
        return Array.from(map.entries())
            .map(([label, value]) => ({ label, value }))
            .sort((a, b) => b.value - a.value);
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [tasks, query]);

    const byStatus = useMemo(() => {
        const groups: Array<{ label: string; tone: string; statuses: string[] }> = [
            { label: "Pending", tone: "bg-amber", statuses: ["pending"] },
            { label: "Scheduled", tone: "bg-blue", statuses: ["scheduled"] },
            { label: "In progress", tone: "bg-navy-600", statuses: ["in-progress"] },
            { label: "Completed", tone: "bg-green", statuses: ["completed"] }
        ];
        return groups.map((group) => ({
            label: group.label,
            tone: group.tone,
            value: tasks.filter((task) => group.statuses.includes(task.status)).length
        }));
    }, [tasks]);

    const conflictsByCategory = useMemo(() => {
        const map = new Map<string, number>();
        conflicts
            .filter((conflict) => matches(conflict.category) || matches(conflict.title))
            .forEach((conflict) => map.set(conflict.category, (map.get(conflict.category) ?? 0) + 1));
        return Array.from(map.entries())
            .map(([label, value]) => ({ label, value, tone: "bg-red" }))
            .sort((a, b) => b.value - a.value);
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [conflicts, query]);

    const assetHealth = useMemo(() => {
        const groups = [
            { label: "Good", tone: "bg-green", conditions: ["good"] },
            { label: "Fair", tone: "bg-blue", conditions: ["fair"] },
            { label: "Poor", tone: "bg-amber", conditions: ["poor"] },
            { label: "Critical", tone: "bg-red", conditions: ["critical"] }
        ];
        return groups.map((group) => ({
            label: group.label,
            tone: group.tone,
            value: assets.filter((asset) => group.conditions.includes(asset.condition)).length
        }));
    }, [assets]);

    const corridorUtilization = useMemo(
        () =>
            Array.from(
                sections.reduce((map, section) => {
                    const list = map.get(section.corridor) ?? [];
                    list.push(section.utilizationPercent);
                    map.set(section.corridor, list);
                    return map;
                }, new Map<string, number[]>())
            ).map(([label, values]) => ({
                label,
                value: Math.round(values.reduce((total, value) => total + value, 0) / values.length),
                tone: "bg-navy-600"
            })),
        [sections]
    );

    const delayed = trains.filter((train) => train.status === "delayed").length;
    const onTime = trains.length - delayed;

    const trendData = DEMO_WEEKLY_TREND.map((entry) => ({
        label: entry.day,
        value: entry[series]
    }));

    const kpiItems: KpiItem[] = [
        { label: "Asset Availability", value: `${kpis?.assetAvailabilityPercent ?? 0}%`, hint: "Non-critical assets", icon: Gauge, tone: "green" },
        { label: "Block Utilization", value: `${kpis?.blockUtilizationPercent ?? 0}%`, hint: "Granted windows used", icon: Layers },
        { label: "On-time Rate", value: `${kpis?.onTimePercent ?? 0}%`, hint: "Services without delay", icon: Activity, tone: (kpis?.onTimePercent ?? 0) >= 85 ? "green" : "amber" },
        { label: "Critical Maintenance", value: kpis?.criticalMaintenance ?? 0, hint: "Highest risk queue", icon: ListTodo, tone: "red" },
        { label: "Active Conflicts", value: kpis?.activeConflicts ?? 0, hint: "Open rule breaches", icon: ShieldAlert, tone: (kpis?.activeConflicts ?? 0) ? "red" : "green" },
        { label: "Completed Tasks", value: kpis?.tasksCompletedToday ?? 0, hint: "Closed in this run", icon: BarChart3, tone: "blue" }
    ];

    const exportSnapshot = () =>
        downloadCsv(
            "rmos-analytics-snapshot",
            [
                { key: "metric", header: "Metric", value: (row: Record<string, string | number>) => row.metric },
                { key: "value", header: "Value", value: (row: Record<string, string | number>) => row.value }
            ],
            [
                { metric: "Asset availability %", value: kpis?.assetAvailabilityPercent ?? 0 },
                { metric: "Block utilization %", value: kpis?.blockUtilizationPercent ?? 0 },
                { metric: "On-time %", value: kpis?.onTimePercent ?? 0 },
                { metric: "Critical maintenance", value: kpis?.criticalMaintenance ?? 0 },
                { metric: "Active conflicts", value: kpis?.activeConflicts ?? 0 },
                { metric: "Tasks completed", value: kpis?.tasksCompletedToday ?? 0 },
                { metric: "Trains running", value: kpis?.trainsRunning ?? 0 },
                { metric: "Open approvals", value: kpis?.openApprovalRequests ?? 0 }
            ]
        );

    return (
        <div className="flex flex-col gap-4">
            <KpiStrip items={kpiItems} />

            <ModuleToolbar
                search={search}
                onSearchChange={setSearch}
                placeholder="Filter departments, conflicts, sections…"
                resultLabel={`${tasks.length} tasks · ${conflicts.length} conflicts · ${trains.length} services`}
                onRefresh={refresh}
                onExport={exportSnapshot}
                filters={
                    <div className="flex items-center gap-1.5">
                        <span className="text-[11.5px] font-semibold uppercase tracking-[0.4px] text-muted">
                            Trend
                        </span>
                        {(Object.keys(SERIES_LABEL) as TrendSeries[]).map((key) => (
                            <button
                                key={key}
                                type="button"
                                onClick={() => setSeries(key)}
                                className={`h-8 px-2.5 rounded-md border text-[12.5px] font-medium transition-colors ${
                                    series === key
                                        ? "border-blue bg-blue-bg text-blue"
                                        : "border-input text-muted hover:text-ink"
                                }`}
                            >
                                {SERIES_LABEL[key]}
                            </button>
                        ))}
                    </div>
                }
            />

            <Card>
                <CardHeader className="pb-3">
                    <div className="flex items-center justify-between gap-3 flex-wrap">
                        <div>
                            <CardTitle className="text-[15px] flex items-center gap-2">
                                <BarChart3 className="w-4 h-4 text-primary" />
                                Seven-Day Planning Trend
                            </CardTitle>
                            <CardDescription>
                                Daily {SERIES_LABEL[series].toLowerCase()} volume for the current planning run.
                            </CardDescription>
                        </div>
                        <span className="text-[12px] font-mono text-muted">
                            total {trendData.reduce((total, entry) => total + entry.value, 0)}
                        </span>
                    </div>
                </CardHeader>
                <CardContent>
                    <TrendChart data={trendData} color={SERIES_COLOR[series]} />
                </CardContent>
            </Card>

            <div className="grid gap-4 lg:grid-cols-2">
                <BarChart
                    title="Tasks by department"
                    description="Maintenance load distribution"
                    data={byDepartment}
                />
                <BarChart
                    title="Tasks by status"
                    description="Where the queue currently stands"
                    data={byStatus}
                />
                <BarChart
                    title="Conflicts by category"
                    description="Which rule families fire most"
                    data={conflictsByCategory}
                />
                <BarChart
                    title="Asset health distribution"
                    description="Condition of registered assets"
                    data={assetHealth}
                />
                <BarChart
                    title="Corridor utilization"
                    description="Average capacity used per corridor"
                    data={corridorUtilization}
                    suffix="%"
                />
                <Card>
                    <CardHeader className="pb-3">
                        <CardTitle className="text-[14px] flex items-center gap-2">
                            <Train className="w-4 h-4 text-primary" />
                            Service punctuality
                        </CardTitle>
                        <CardDescription>On-time versus delayed services</CardDescription>
                    </CardHeader>
                    <CardContent className="space-y-3">
                        <div className="flex h-4 rounded overflow-hidden bg-slate-100">
                            <div
                                className="bg-green"
                                style={{ width: `${(onTime / Math.max(1, trains.length)) * 100}%` }}
                            />
                            <div
                                className="bg-red"
                                style={{ width: `${(delayed / Math.max(1, trains.length)) * 100}%` }}
                            />
                        </div>
                        <div className="flex items-center justify-between text-[12.5px]">
                            <span className="flex items-center gap-1.5">
                                <span className="w-2.5 h-2.5 rounded-sm bg-green" /> On time:{" "}
                                <strong className="font-mono">{onTime}</strong>
                            </span>
                            <span className="flex items-center gap-1.5">
                                <span className="w-2.5 h-2.5 rounded-sm bg-red" /> Delayed:{" "}
                                <strong className="font-mono">{delayed}</strong>
                            </span>
                        </div>
                        <p className="text-[11.5px] text-muted">
                            Punctuality is calculated from the synthetic service board and refreshes with the module
                            data.
                        </p>
                    </CardContent>
                </Card>
            </div>
        </div>
    );
}

function TrendChart({ data, color }: { data: Array<{ label: string; value: number }>; color: string }) {
    const width = 720;
    const height = 200;
    const paddingX = 34;
    const paddingY = 22;
    const max = Math.max(1, ...data.map((entry) => entry.value));
    const step = (width - paddingX * 2) / Math.max(1, data.length - 1);

    const points = data.map((entry, index) => {
        const x = paddingX + index * step;
        const y = height - paddingY - (entry.value / max) * (height - paddingY * 2);
        return { ...entry, x, y };
    });

    const path = points.map((point, index) => `${index === 0 ? "M" : "L"}${point.x},${point.y}`).join(" ");
    const area = `${path} L${points[points.length - 1].x},${height - paddingY} L${points[0].x},${height - paddingY} Z`;

    return (
        <div className="w-full overflow-hidden">
            <svg viewBox={`0 0 ${width} ${height}`} className="w-full h-[200px]" role="img" aria-label="Trend chart">
                {[0, 0.25, 0.5, 0.75, 1].map((ratio) => {
                    const y = paddingY + ratio * (height - paddingY * 2);
                    return (
                        <g key={ratio}>
                            <line
                                x1={paddingX}
                                x2={width - paddingX}
                                y1={y}
                                y2={y}
                                stroke="#E7EBEF"
                                strokeWidth={1}
                            />
                            <text x={4} y={y + 4} fontSize={10} fill="#98A2AE">
                                {Math.round(max * (1 - ratio))}
                            </text>
                        </g>
                    );
                })}

                <path d={area} fill={color} opacity={0.1} />
                <path d={path} fill="none" stroke={color} strokeWidth={2.5} strokeLinejoin="round" />

                {points.map((point) => (
                    <g key={point.label}>
                        <circle cx={point.x} cy={point.y} r={4} fill="#FFFFFF" stroke={color} strokeWidth={2.5} />
                        <text x={point.x} y={height - 6} fontSize={11} fill="#6B7686" textAnchor="middle">
                            {point.label}
                        </text>
                        <text
                            x={point.x}
                            y={point.y - 12}
                            fontSize={11}
                            fill="#111B26"
                            textAnchor="middle"
                            fontWeight={600}
                        >
                            {point.value}
                        </text>
                    </g>
                ))}
            </svg>
        </div>
    );
}
