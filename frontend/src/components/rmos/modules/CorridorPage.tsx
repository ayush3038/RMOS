import { useMemo, useState } from "react";
import {
    Activity,
    Gauge,
    Layers,
    Map as MapIcon,
    Ruler,
    ShieldAlert,
    TrafficCone
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
import { availabilityChip } from "@/lib/status";
import { formatShortDate } from "@/lib/format";
import type { AvailabilityStatus, CorridorSection } from "@/types/models";

const STATUS_OPTIONS = [
    { value: "all", label: "All states" },
    { value: "available", label: "Available" },
    { value: "restricted", label: "Restricted" },
    { value: "maintenance", label: "Maintenance" },
    { value: "blocked", label: "Blocked" }
];

const STATUS_COLOR: Record<AvailabilityStatus, string> = {
    available: "bg-green",
    restricted: "bg-amber",
    maintenance: "bg-blue",
    blocked: "bg-red"
};

export function CorridorPage() {
    const { sections: sectionsState, refresh } = useModuleData();
    const { toast } = useToast();

    const { items: sections, update: updateSection } = useLocalList<CorridorSection>(
        sectionsState.data,
        (section) => section.id
    );

    const [search, setSearch] = useState("");
    const [corridor, setCorridor] = useState("all");
    const [status, setStatus] = useState("all");
    const [selectedId, setSelectedId] = useState<string | null>(null);

    const selected = sections.find((section) => section.id === selectedId) ?? null;

    const corridorOptions = useMemo(
        () => [
            { value: "all", label: "All corridors" },
            ...Array.from(new Set(sections.map((section) => section.corridor))).map((value) => ({
                value,
                label: value
            }))
        ],
        [sections]
    );

    const filtered = useMemo(() => {
        const query = search.trim().toLowerCase();
        return sections.filter((section) => {
            if (corridor !== "all" && section.corridor !== corridor) return false;
            if (status !== "all" && section.availabilityStatus !== status) return false;
            if (!query) return true;
            return [section.id, section.name, section.corridor, section.line, section.upcomingMaintenanceCode ?? ""]
                .join(" ")
                .toLowerCase()
                .includes(query);
        });
    }, [sections, search, corridor, status]);

    const summary = useMemo(() => {
        const groups = new Map<string, CorridorSection[]>();
        sections.forEach((section) => {
            const list = groups.get(section.corridor) ?? [];
            list.push(section);
            groups.set(section.corridor, list);
        });
        return Array.from(groups.entries()).map(([name, entries]) => ({
            name,
            entries,
            available: entries.filter((entry) => entry.availabilityStatus === "available").length,
            blocked: entries.filter(
                (entry) => entry.availabilityStatus === "blocked" || entry.availabilityStatus === "maintenance"
            ).length,
            utilization: Math.round(
                entries.reduce((total, entry) => total + entry.utilizationPercent, 0) / entries.length
            ),
            trains: entries.reduce((total, entry) => total + entry.currentTrains, 0)
        }));
    }, [sections]);

    const availableCount = sections.filter((section) => section.availabilityStatus === "available").length;
    const blockedCount = sections.filter((section) => section.availabilityStatus === "blocked").length;
    const maintenanceCount = sections.filter((section) => section.availabilityStatus === "maintenance").length;
    const restrictedCount = sections.filter((section) => section.availabilityStatus === "restricted").length;
    const averageUtilization = sections.length
        ? Math.round(sections.reduce((total, section) => total + section.utilizationPercent, 0) / sections.length)
        : 0;
    const routeLength = sections.reduce((total, section) => total + section.lengthKm, 0);

    const kpis: KpiItem[] = [
        { label: "Sections", value: sections.length, hint: `${restrictedCount} restricted · ${summary.length} corridors`, icon: Layers },
        { label: "Available", value: availableCount, hint: "Clear for normal pathing", icon: MapIcon, tone: "green" },
        { label: "Maintenance", value: maintenanceCount, hint: "Possession active", icon: TrafficCone, tone: "blue" },
        { label: "Blocked", value: blockedCount, hint: "No path available", icon: ShieldAlert, tone: blockedCount ? "red" : "green" },
        { label: "Avg Utilization", value: `${averageUtilization}%`, hint: "Trains vs capacity", icon: Activity },
        { label: "Route Length", value: `${routeLength.toFixed(1)} km`, hint: "Across listed sections", icon: Ruler }
    ];

    const columns: DataTableColumn<CorridorSection>[] = [
        {
            key: "section",
            header: "Section",
            sortValue: (section) => section.id,
            cell: (section) => (
                <div className="leading-tight">
                    <div className="font-semibold text-[12.5px]">{section.name}</div>
                    <div className="text-[11px] text-muted font-mono">{section.id}</div>
                </div>
            )
        },
        {
            key: "corridor",
            header: "Corridor / Line",
            sortValue: (section) => section.corridor,
            cell: (section) => (
                <div className="leading-tight">
                    <div className="text-[12.5px]">{section.corridor}</div>
                    <div className="text-[11px] text-muted">{section.line}</div>
                </div>
            )
        },
        {
            key: "status",
            header: "State",
            sortValue: (section) => section.availabilityStatus,
            cell: (section) => (
                <StatusChip
                    status={availabilityChip(section.availabilityStatus)}
                    showDot={false}
                    className="text-[10px] px-1.5 py-0"
                />
            )
        },
        {
            key: "trains",
            header: "Trains",
            sortValue: (section) => section.currentTrains,
            cell: (section) => (
                <span className="text-[12.5px] font-mono">
                    {section.currentTrains}/{section.capacityTrains}
                </span>
            )
        },
        {
            key: "utilization",
            header: "Utilization",
            sortValue: (section) => section.utilizationPercent,
            cell: (section) => (
                <div className="flex items-center gap-2 w-[120px]">
                    <div className="h-1.5 flex-1 rounded-full bg-slate-200 overflow-hidden">
                        <div
                            className={`h-full ${section.utilizationPercent > 80 ? "bg-red" : section.utilizationPercent > 50 ? "bg-amber" : "bg-green"}`}
                            style={{ width: `${section.utilizationPercent}%` }}
                        />
                    </div>
                    <span className="text-[11px] font-mono text-muted tabular-nums w-8 text-right">
                        {section.utilizationPercent}%
                    </span>
                </div>
            )
        },
        {
            key: "length",
            header: "Length",
            sortValue: (section) => section.lengthKm,
            cell: (section) => <span className="text-[12.5px] font-mono">{section.lengthKm} km</span>
        },
        {
            key: "speed",
            header: "Speed cap",
            sortValue: (section) => section.speedLimitKph,
            cell: (section) => (
                <span className="text-[12.5px] font-mono flex items-center gap-1">
                    <Gauge className="w-3.5 h-3.5 text-muted" />
                    {section.speedLimitKph}
                </span>
            )
        },
        {
            key: "maintenance",
            header: "Upcoming work",
            sortValue: (section) => section.upcomingMaintenanceCode ?? "zzz",
            cell: (section) =>
                section.upcomingMaintenanceCode ? (
                    <div className="leading-tight">
                        <div className="font-mono text-[11.5px]">{section.upcomingMaintenanceCode}</div>
                        <div className="text-[11px] text-muted">
                            {section.nextWindowStart ? formatShortDate(section.nextWindowStart) : "window pending"}
                        </div>
                    </div>
                ) : (
                    <span className="text-[11.5px] text-muted">—</span>
                )
        },
        {
            key: "inspection",
            header: "Last inspection",
            sortValue: (section) => section.lastInspectionDate,
            cell: (section) => (
                <span className="text-[12.5px] text-muted">{section.lastInspectionDate}</span>
            )
        }
    ];

    const exportSections = () =>
        downloadCsv(
            "rmos-corridor-availability",
            [
                { key: "id", header: "Section", value: (section) => section.id },
                { key: "name", header: "Name", value: (section) => section.name },
                { key: "corridor", header: "Corridor", value: (section) => section.corridor },
                { key: "line", header: "Line", value: (section) => section.line },
                { key: "availabilityStatus", header: "Status", value: (section) => section.availabilityStatus },
                { key: "currentTrains", header: "Trains", value: (section) => section.currentTrains },
                { key: "capacityTrains", header: "Capacity", value: (section) => section.capacityTrains },
                { key: "utilizationPercent", header: "Utilization %", value: (section) => section.utilizationPercent },
                { key: "lengthKm", header: "Length (km)", value: (section) => section.lengthKm },
                { key: "speedLimitKph", header: "Speed cap", value: (section) => section.speedLimitKph },
                { key: "lastInspectionDate", header: "Last inspection", value: (section) => section.lastInspectionDate }
            ],
            filtered
        );

    return (
        <div className="flex flex-col gap-4">
            <KpiStrip items={kpis} />

            <div className="grid gap-3 md:grid-cols-3">
                {summary.map((group) => (
                    <button
                        key={group.name}
                        type="button"
                        onClick={() => setCorridor(corridor === group.name ? "all" : group.name)}
                        className={`text-left border rounded-md bg-surface px-3.5 py-3 transition-colors hover:border-blue-line ${
                            corridor === group.name ? "border-blue ring-2 ring-blue-line" : ""
                        }`}
                    >
                        <div className="flex items-center justify-between gap-2">
                            <span className="text-[13px] font-semibold">{group.name}</span>
                            <Badge variant="outline" className="font-mono text-[10.5px]">
                                {group.entries.length} sections
                            </Badge>
                        </div>
                        <div className="flex items-center gap-3 mt-2 text-[11.5px] text-muted">
                            <span className="text-green font-semibold">{group.available} open</span>
                            <span className="text-red font-semibold">{group.blocked} closed</span>
                            <span>{group.trains} trains</span>
                        </div>
                        <div className="mt-2 h-1.5 rounded-full bg-slate-200 overflow-hidden">
                            <div
                                className={`h-full ${group.utilization > 70 ? "bg-amber" : "bg-green"}`}
                                style={{ width: `${group.utilization}%` }}
                            />
                        </div>
                        <div className="text-[11px] text-muted mt-1 font-mono">
                            {group.utilization}% utilized
                        </div>
                    </button>
                ))}
            </div>

            <ModuleToolbar
                search={search}
                onSearchChange={setSearch}
                placeholder="Search section, corridor, work code…"
                resultLabel={`${filtered.length} of ${sections.length} sections`}
                onRefresh={refresh}
                onExport={exportSections}
                exportDisabled={filtered.length === 0}
                filters={
                    <>
                        <FilterSelect label="Corridor" value={corridor} onChange={setCorridor} options={corridorOptions} />
                        <FilterSelect label="State" value={status} onChange={setStatus} options={STATUS_OPTIONS} />
                    </>
                }
            />

            <Card>
                <CardHeader className="pb-3">
                    <CardTitle className="text-[15px] flex items-center gap-2">
                        <MapIcon className="w-4 h-4 text-primary" />
                        Section Availability Register
                    </CardTitle>
                    <CardDescription>
                        Availability, capacity and inspection state per section · click a row for detail.
                    </CardDescription>
                </CardHeader>
                <CardContent>
                    <DataTable
                        columns={columns}
                        rows={filtered}
                        rowId={(section) => section.id}
                        onRowClick={(section) => setSelectedId(section.id)}
                        selectedRowId={selectedId}
                        isLoading={sectionsState.isLoading}
                        errorMessage={sectionsState.error?.message ?? null}
                        emptyMessage="No sections match the selected filters."
                        initialSort={{ key: "section", direction: "asc" }}
                    />
                </CardContent>
            </Card>

            <Sheet open={!!selected} onOpenChange={(open) => !open && setSelectedId(null)}>
                <SheetContent className="w-full sm:max-w-md overflow-y-auto">
                    {selected && (
                        <>
                            <SheetHeader className="mb-4">
                                <div className="flex items-center gap-2 flex-wrap">
                                    <Badge variant="outline">{selected.corridor}</Badge>
                                    <StatusChip
                                        status={availabilityChip(selected.availabilityStatus)}
                                        showDot={false}
                                    />
                                </div>
                                <SheetTitle className="text-xl">{selected.name}</SheetTitle>
                                <SheetDescription>
                                    {selected.line} · {selected.lengthKm} km · speed cap {selected.speedLimitKph} km/h
                                </SheetDescription>
                            </SheetHeader>

                            <div className="space-y-5 px-4 pb-4">
                                <div>
                                    <div className="flex items-center justify-between text-[11.5px] text-muted mb-1.5">
                                        <span className="uppercase tracking-[0.4px] font-semibold">Capacity used</span>
                                        <span className="font-mono">{selected.utilizationPercent}%</span>
                                    </div>
                                    <div className="h-2 rounded-full bg-slate-200 overflow-hidden">
                                        <div
                                            className={`h-full ${STATUS_COLOR[selected.availabilityStatus]}`}
                                            style={{ width: `${selected.utilizationPercent}%` }}
                                        />
                                    </div>
                                    <div className="text-[11.5px] text-muted mt-1.5">
                                        {selected.currentTrains} of {selected.capacityTrains} train paths in use
                                    </div>
                                </div>

                                <div className="grid grid-cols-2 gap-3">
                                    <DetailItem label="Trains now" value={String(selected.currentTrains)} />
                                    <DetailItem label="Capacity" value={String(selected.capacityTrains)} />
                                    <DetailItem label="Length" value={`${selected.lengthKm} km`} />
                                    <DetailItem label="Speed cap" value={`${selected.speedLimitKph} km/h`} />
                                    <DetailItem label="Last inspection" value={selected.lastInspectionDate} />
                                    <DetailItem
                                        label="Upcoming work"
                                        value={selected.upcomingMaintenanceCode ?? "None scheduled"}
                                    />
                                </div>

                                <div className="border rounded-md p-3 bg-slate-50 space-y-1.5 text-[12.5px]">
                                    <div className="flex justify-between gap-3">
                                        <span className="text-muted">Source system</span>
                                        <span className="font-mono">{selected.sourceSystem}</span>
                                    </div>
                                    <div className="flex justify-between gap-3">
                                        <span className="text-muted">Freshness</span>
                                        <span>{selected.sourceTimestamp.slice(0, 16).replace("T", " ")}</span>
                                    </div>
                                </div>

                                <div className="flex gap-2">
                                    <Button
                                        className="flex-1"
                                        disabled={selected.availabilityStatus === "available"}
                                        onClick={() => {
                                            updateSection(selected.id, {
                                                availabilityStatus: "available",
                                                currentTrains: Math.max(1, selected.currentTrains),
                                                utilizationPercent: Math.max(20, selected.utilizationPercent)
                                            });
                                            toast(`${selected.name} released for normal traffic.`);
                                        }}
                                    >
                                        Release section
                                    </Button>
                                    <Button
                                        variant="outline"
                                        className="flex-1"
                                        disabled={selected.availabilityStatus === "blocked"}
                                        onClick={() => {
                                            updateSection(selected.id, {
                                                availabilityStatus: "blocked",
                                                currentTrains: 0,
                                                utilizationPercent: 0
                                            });
                                            toast(`${selected.name} blocked for engineering possession.`, "warning");
                                        }}
                                    >
                                        Impose possession
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

