import { useMemo, useState } from "react";
import {
    CircleDot,
    Gauge,
    MapPin,
    Route,
    Timer,
    Train as TrainIcon,
    TriangleAlert
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
import { trainStatusChip } from "@/lib/status";
import { formatDateTime, formatTime, relativeTime } from "@/lib/format";
import type { Train } from "@/types/models";

const STATUS_OPTIONS = [
    { value: "all", label: "All states" },
    { value: "on-time", label: "On time" },
    { value: "delayed", label: "Delayed" },
    { value: "diverted", label: "Diverted" },
    { value: "scheduled", label: "Scheduled" },
    { value: "terminated", label: "Terminated" }
];

const TYPE_OPTIONS = [
    { value: "all", label: "All types" },
    { value: "Express", label: "Express" },
    { value: "Superfast", label: "Superfast" },
    { value: "Local", label: "Local" },
    { value: "Freight", label: "Freight" },
    { value: "Maintenance", label: "Maintenance" },
    { value: "Inspection", label: "Inspection" }
];

export function TrainsPage() {
    const { trains: trainsState, sections, refresh } = useModuleData();
    const { toast } = useToast();

    const { items: trains, update: updateTrain } = useLocalList<Train>(
        trainsState.data,
        (train) => train.id
    );

    const [search, setSearch] = useState("");
    const [status, setStatus] = useState("all");
    const [type, setType] = useState("all");
    const [selectedId, setSelectedId] = useState<string | null>(null);

    const selected = trains.find((train) => train.id === selectedId) ?? null;

    const sectionName = (sectionId: string) =>
        sections.data?.find((section) => section.id === sectionId)?.name ?? sectionId;

    const filtered = useMemo(() => {
        const query = search.trim().toLowerCase();
        return trains.filter((train) => {
            if (status !== "all" && train.status !== status) return false;
            if (type !== "all" && train.trainType !== type) return false;
            if (!query) return true;
            return [
                train.trainNumber,
                train.trainName,
                train.origin,
                train.destination,
                train.sectionId,
                train.rakeId
            ]
                .join(" ")
                .toLowerCase()
                .includes(query);
        });
    }, [trains, search, status, type]);

    const running = trains.filter(
        (train) => train.status === "on-time" || train.status === "delayed" || train.status === "diverted"
    );
    const delayed = trains.filter((train) => train.status === "delayed");
    const onTimePercent = trains.length
        ? Math.round(((trains.length - delayed.length) / trains.length) * 1000) / 10
        : 0;
    const averageDelay = delayed.length
        ? Math.round(delayed.reduce((total, train) => total + train.delayMinutes, 0) / delayed.length)
        : 0;

    const kpis: KpiItem[] = [
        { label: "Services", value: trains.length, hint: "In current planning run", icon: TrainIcon },
        { label: "Running", value: running.length, hint: "On path right now", icon: CircleDot, tone: "green" },
        { label: "Delayed", value: delayed.length, hint: "Awaiting recovery", icon: TriangleAlert, tone: delayed.length ? "red" : "green" },
        { label: "On-time Rate", value: `${onTimePercent}%`, hint: "Division wide", icon: Gauge, tone: onTimePercent >= 85 ? "green" : "amber" },
        { label: "Avg Delay", value: `${averageDelay} m`, hint: "Across delayed services", icon: Timer, tone: averageDelay > 30 ? "amber" : "default" },
        { label: "Diverted", value: trains.filter((train) => train.status === "diverted").length, hint: "Path changed by control", icon: Route, tone: "amber" }
    ];

    const columns: DataTableColumn<Train>[] = [
        {
            key: "service",
            header: "Service",
            sortValue: (train) => train.trainNumber,
            cell: (train) => (
                <div className="leading-tight">
                    <div className="font-mono text-[12.5px] font-semibold">{train.trainNumber}</div>
                    <div className="text-[11px] text-muted">{train.trainName}</div>
                </div>
            )
        },
        {
            key: "type",
            header: "Type",
            sortValue: (train) => train.trainType,
            cell: (train) => <span className="text-[12.5px]">{train.trainType}</span>
        },
        {
            key: "route",
            header: "Route",
            sortValue: (train) => train.origin,
            cell: (train) => (
                <span className="text-[12.5px] font-mono">
                    {train.origin} <span className="text-muted">→</span> {train.destination}
                </span>
            )
        },
        {
            key: "section",
            header: "Section",
            sortValue: (train) => train.sectionId,
            cell: (train) => <span className="text-[12.5px]">{sectionName(train.sectionId)}</span>
        },
        {
            key: "status",
            header: "State",
            sortValue: (train) => train.status,
            cell: (train) => (
                <StatusChip status={trainStatusChip(train.status)} showDot={false} className="text-[10px] px-1.5 py-0" />
            )
        },
        {
            key: "delay",
            header: "Delay",
            sortValue: (train) => train.delayMinutes,
            cell: (train) => (
                <span
                    className={`text-[12.5px] font-mono ${train.delayMinutes > 0 ? "text-red font-semibold" : "text-green"}`}
                >
                    {train.delayMinutes > 0 ? `+${train.delayMinutes}m` : "0m"}
                </span>
            )
        },
        {
            key: "platform",
            header: "Platform",
            sortValue: (train) => train.platform,
            cell: (train) => <span className="text-[12.5px] font-mono">{train.platform}</span>
        },
        {
            key: "speed",
            header: "Speed",
            sortValue: (train) => train.speedKph,
            cell: (train) => <span className="text-[12.5px] font-mono">{train.speedKph} km/h</span>
        },
        {
            key: "arrival",
            header: "ETA",
            sortValue: (train) => new Date(train.scheduledArrival).getTime(),
            cell: (train) => (
                <span className="text-[12.5px] text-muted whitespace-nowrap">
                    {formatTime(train.scheduledArrival)}
                </span>
            )
        },
        {
            key: "priority",
            header: "Pri",
            sortValue: (train) => train.priorityCode,
            cell: (train) => (
                <Badge variant="outline" className="font-mono text-[10.5px]">
                    {train.priorityCode}
                </Badge>
            )
        }
    ];

    const exportTrains = () =>
        downloadCsv(
            "rmos-train-operations",
            [
                { key: "trainNumber", header: "Train", value: (train) => train.trainNumber },
                { key: "trainName", header: "Name", value: (train) => train.trainName },
                { key: "trainType", header: "Type", value: (train) => train.trainType },
                { key: "origin", header: "Origin", value: (train) => train.origin },
                { key: "destination", header: "Destination", value: (train) => train.destination },
                { key: "sectionId", header: "Section", value: (train) => train.sectionId },
                { key: "status", header: "Status", value: (train) => train.status },
                { key: "delayMinutes", header: "Delay (min)", value: (train) => train.delayMinutes },
                { key: "platform", header: "Platform", value: (train) => train.platform },
                { key: "speedKph", header: "Speed (km/h)", value: (train) => train.speedKph },
                { key: "rakeId", header: "Rake", value: (train) => train.rakeId }
            ],
            filtered
        );

    return (
        <div className="flex flex-col gap-4">
            <KpiStrip items={kpis} />

            <ModuleToolbar
                search={search}
                onSearchChange={setSearch}
                placeholder="Search train number, name, station…"
                resultLabel={`${filtered.length} of ${trains.length} services`}
                onRefresh={refresh}
                onExport={exportTrains}
                exportDisabled={filtered.length === 0}
                filters={
                    <>
                        <FilterSelect label="State" value={status} onChange={setStatus} options={STATUS_OPTIONS} />
                        <FilterSelect label="Type" value={type} onChange={setType} options={TYPE_OPTIONS} />
                    </>
                }
            />

            <Card>
                <CardHeader className="pb-3">
                    <div className="flex items-center justify-between gap-3 flex-wrap">
                        <div>
                            <CardTitle className="text-[15px] flex items-center gap-2">
                                <TrainIcon className="w-4 h-4 text-primary" />
                                Live Service Board
                            </CardTitle>
                            <CardDescription>
                                Synthetic train state across the division · click a service for path detail.
                            </CardDescription>
                        </div>
                        <div className="flex items-center gap-2 text-[11.5px] text-muted">
                            <MapPin className="w-3.5 h-3.5" />
                            {new Set(trains.map((train) => train.sectionId)).size} sections occupied
                        </div>
                    </div>
                </CardHeader>
                <CardContent>
                    <DataTable
                        columns={columns}
                        rows={filtered}
                        rowId={(train) => train.id}
                        onRowClick={(train) => setSelectedId(train.id)}
                        selectedRowId={selectedId}
                        isLoading={trainsState.isLoading}
                        errorMessage={trainsState.error?.message ?? null}
                        emptyMessage="No services match the selected filters."
                        initialSort={{ key: "delay", direction: "desc" }}
                    />
                </CardContent>
            </Card>

            <Sheet open={!!selected} onOpenChange={(open) => !open && setSelectedId(null)}>
                <SheetContent className="w-full sm:max-w-md overflow-y-auto">
                    {selected && (
                        <>
                            <SheetHeader className="mb-4">
                                <div className="flex items-center gap-2 flex-wrap">
                                    <Badge variant="outline">{selected.trainType}</Badge>
                                    <StatusChip status={trainStatusChip(selected.status)} showDot={false} />
                                    <Badge variant="secondary" className="font-mono text-[11px]">
                                        {selected.priorityCode}
                                    </Badge>
                                </div>
                                <SheetTitle className="text-xl font-mono">{selected.trainNumber}</SheetTitle>
                                <SheetDescription>
                                    {selected.trainName} · {selected.origin} to {selected.destination}
                                </SheetDescription>
                            </SheetHeader>

                            <div className="space-y-5 px-4 pb-4">
                                <div className="grid grid-cols-2 gap-3">
                                    <DetailItem label="Delay" value={`${selected.delayMinutes} min`} />
                                    <DetailItem label="Speed" value={`${selected.speedKph} km/h`} />
                                    <DetailItem label="Platform" value={selected.platform} />
                                    <DetailItem label="Rake" value={selected.rakeId} />
                                    <DetailItem label="Sched. departure" value={formatDateTime(selected.scheduledDeparture)} />
                                    <DetailItem label="Sched. arrival" value={formatDateTime(selected.scheduledArrival)} />
                                    <DetailItem label="Current section" value={sectionName(selected.sectionId)} />
                                    <DetailItem label="Telemetry" value={relativeTime(selected.sourceTimestamp)} />
                                </div>

                                <div className="border rounded-md p-3 bg-slate-50 space-y-1.5 text-[12.5px]">
                                    <div className="flex justify-between gap-3">
                                        <span className="text-muted">Source system</span>
                                        <span className="font-mono">{selected.sourceSystem}</span>
                                    </div>
                                    <div className="flex justify-between gap-3">
                                        <span className="text-muted">Source record</span>
                                        <span className="font-mono">{selected.sourceRecordId}</span>
                                    </div>
                                </div>

                                <div className="space-y-2">
                                    <Button
                                        className="w-full"
                                        onClick={() => {
                                            updateTrain(selected.id, { status: "delayed", delayMinutes: selected.delayMinutes + 15 });
                                            toast(`${selected.trainNumber} flagged delayed by 15 minutes.`, "warning");
                                        }}
                                    >
                                        Flag additional delay
                                    </Button>
                                    <Button
                                        variant="outline"
                                        className="w-full"
                                        onClick={() => {
                                            updateTrain(selected.id, { status: "on-time", delayMinutes: 0, speedKph: 72 });
                                            toast(`${selected.trainNumber} recovered onto scheduled path.`);
                                        }}
                                    >
                                        Recover to path
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

