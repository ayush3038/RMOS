import { useMemo, useState } from "react";
import {
    CheckCircle2,
    CircleAlert,
    Eye,
    ShieldAlert,
    TimerReset,
    Zap
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
import { conflictStatusChip, priorityChip } from "@/lib/status";
import { relativeTime } from "@/lib/format";
import type { Conflict } from "@/types/models";

const SEVERITY_OPTIONS = [
    { value: "all", label: "All severities" },
    { value: "critical", label: "Critical" },
    { value: "high", label: "High" },
    { value: "medium", label: "Medium" },
    { value: "low", label: "Low" }
];

const STATUS_OPTIONS = [
    { value: "all", label: "All states" },
    { value: "open", label: "Open" },
    { value: "acknowledged", label: "Acknowledged" },
    { value: "resolved", label: "Resolved" }
];

const CATEGORY_OPTIONS = [
    { value: "all", label: "All categories" },
    { value: "Block Conflict", label: "Block" },
    { value: "Train Conflict", label: "Train" },
    { value: "Resource Conflict", label: "Resource" },
    { value: "Safety Conflict", label: "Safety" }
];

export function ConflictsPage() {
    const { conflicts: conflictsState, sections, refresh } = useModuleData();
    const { toast } = useToast();

    const { items: conflicts, update: updateConflict } = useLocalList<Conflict>(
        conflictsState.data,
        (conflict) => conflict.id
    );

    const [search, setSearch] = useState("");
    const [severity, setSeverity] = useState("all");
    const [status, setStatus] = useState("all");
    const [category, setCategory] = useState("all");
    const [selectedId, setSelectedId] = useState<string | null>(null);

    const selected = conflicts.find((conflict) => conflict.id === selectedId) ?? null;

    const sectionName = (sectionId: string) =>
        sections.data?.find((section) => section.id === sectionId)?.name ?? sectionId;

    const filtered = useMemo(() => {
        const query = search.trim().toLowerCase();
        return conflicts.filter((conflict) => {
            if (severity !== "all" && conflict.severity !== severity) return false;
            if (status !== "all" && conflict.status !== status) return false;
            if (category !== "all" && conflict.category !== category) return false;
            if (!query) return true;
            return [conflict.title, conflict.description, conflict.ruleId, conflict.recommendedAction]
                .join(" ")
                .toLowerCase()
                .includes(query);
        });
    }, [conflicts, search, severity, status, category]);

    const open = conflicts.filter((conflict) => conflict.status === "open");
    const acknowledged = conflicts.filter((conflict) => conflict.status === "acknowledged");
    const resolved = conflicts.filter((conflict) => conflict.status === "resolved");
    const criticalOpen = open.filter((conflict) => conflict.severity === "critical");
    const trainsAffected = open.reduce((total, conflict) => total + conflict.affectedTrains, 0);

    const kpis: KpiItem[] = [
        { label: "Open", value: open.length, hint: "Awaiting planner decision", icon: ShieldAlert, tone: open.length ? "red" : "green" },
        { label: "Critical Open", value: criticalOpen.length, hint: "Hard rule breaches", icon: CircleAlert, tone: criticalOpen.length ? "red" : "green" },
        { label: "Acknowledged", value: acknowledged.length, hint: "Owner assigned", icon: Eye, tone: "amber" },
        { label: "Resolved", value: resolved.length, hint: "Closed this run", icon: CheckCircle2, tone: "green" },
        { label: "Trains Affected", value: trainsAffected, hint: "Paths at risk", icon: TimerReset, tone: trainsAffected ? "amber" : "green" },
        { label: "Rules Triggered", value: new Set(conflicts.map((conflict) => conflict.ruleId)).size, hint: "Detection rules fired", icon: Zap }
    ];

    const changeStatus = (conflict: Conflict, next: Conflict["status"], message: string) => {
        updateConflict(conflict.id, {
            status: next,
            resolutionNote:
                next === "resolved"
                    ? conflict.recommendedAction
                    : next === "acknowledged"
                      ? "Acknowledged by planner; mitigation in progress."
                      : undefined
        });
        toast(message, next === "resolved" ? "success" : next === "acknowledged" ? "info" : "warning");
    };

    const columns: DataTableColumn<Conflict>[] = [
        {
            key: "title",
            header: "Conflict",
            sortValue: (conflict) => conflict.title,
            cell: (conflict) => (
                <div className="leading-tight max-w-[340px]">
                    <div className="text-[12.5px] font-medium">{conflict.title}</div>
                    <div className="text-[11px] text-muted font-mono">{conflict.ruleId}</div>
                </div>
            )
        },
        {
            key: "category",
            header: "Category",
            sortValue: (conflict) => conflict.category,
            cell: (conflict) => <span className="text-[12.5px]">{conflict.category}</span>
        },
        {
            key: "severity",
            header: "Severity",
            sortValue: (conflict) => conflict.severity,
            cell: (conflict) => (
                <StatusChip status={priorityChip(conflict.severity)} showDot={false} className="text-[10px] px-1.5 py-0" />
            )
        },
        {
            key: "status",
            header: "State",
            sortValue: (conflict) => conflict.status,
            cell: (conflict) => (
                <StatusChip status={conflictStatusChip(conflict.status)} showDot={false} className="text-[10px] px-1.5 py-0" />
            )
        },
        {
            key: "sections",
            header: "Sections",
            sortValue: (conflict) => conflict.sectionIds.join(","),
            cell: (conflict) => (
                <div className="flex gap-1 flex-wrap">
                    {conflict.sectionIds.map((sectionId) => (
                        <Badge key={sectionId} variant="secondary" className="font-mono text-[10.5px]">
                            {sectionId}
                        </Badge>
                    ))}
                </div>
            )
        },
        {
            key: "trains",
            header: "Trains",
            sortValue: (conflict) => conflict.affectedTrains,
            cell: (conflict) => <span className="text-[12.5px] font-mono">{conflict.affectedTrains}</span>
        },
        {
            key: "detected",
            header: "Detected",
            sortValue: (conflict) => new Date(conflict.detectedAt).getTime(),
            cell: (conflict) => <span className="text-[11.5px] text-muted">{relativeTime(conflict.detectedAt)}</span>
        },
        {
            key: "actions",
            header: "",
            headerClassName: "w-[176px]",
            cell: (conflict) => (
                <div className="flex items-center gap-1.5">
                    {conflict.status === "open" && (
                        <Button
                            size="xs"
                            variant="outline"
                            onClick={(event) => {
                                event.stopPropagation();
                                changeStatus(conflict, "acknowledged", `${conflict.title} acknowledged.`);
                            }}
                        >
                            <Eye className="w-3 h-3" />
                            Acknowledge
                        </Button>
                    )}
                    {conflict.status !== "resolved" && (
                        <Button
                            size="xs"
                            onClick={(event) => {
                                event.stopPropagation();
                                changeStatus(conflict, "resolved", `${conflict.title} resolved: ${conflict.recommendedAction}`);
                            }}
                        >
                            <CheckCircle2 className="w-3 h-3" />
                            Resolve
                        </Button>
                    )}
                    {conflict.status === "resolved" && (
                        <Button
                            size="xs"
                            variant="ghost"
                            onClick={(event) => {
                                event.stopPropagation();
                                changeStatus(conflict, "open", `${conflict.title} reopened for review.`, );
                            }}
                        >
                            Reopen
                        </Button>
                    )}
                </div>
            )
        }
    ];

    const exportConflicts = () =>
        downloadCsv(
            "rmos-active-conflicts",
            [
                { key: "title", header: "Conflict", value: (conflict) => conflict.title },
                { key: "category", header: "Category", value: (conflict) => conflict.category },
                { key: "severity", header: "Severity", value: (conflict) => conflict.severity },
                { key: "status", header: "Status", value: (conflict) => conflict.status },
                { key: "ruleId", header: "Rule", value: (conflict) => conflict.ruleId },
                { key: "sectionIds", header: "Sections", value: (conflict) => conflict.sectionIds.join(" ") },
                { key: "affectedTrains", header: "Affected trains", value: (conflict) => conflict.affectedTrains },
                { key: "detectedAt", header: "Detected", value: (conflict) => conflict.detectedAt },
                { key: "recommendedAction", header: "Recommended action", value: (conflict) => conflict.recommendedAction }
            ],
            filtered
        );

    return (
        <div className="flex flex-col gap-4">
            <KpiStrip items={kpis} />

            <ModuleToolbar
                search={search}
                onSearchChange={setSearch}
                placeholder="Search conflict, rule id, action…"
                resultLabel={`${filtered.length} of ${conflicts.length} conflicts`}
                onRefresh={refresh}
                onExport={exportConflicts}
                exportDisabled={filtered.length === 0}
                filters={
                    <>
                        <FilterSelect label="Severity" value={severity} onChange={setSeverity} options={SEVERITY_OPTIONS} />
                        <FilterSelect label="State" value={status} onChange={setStatus} options={STATUS_OPTIONS} />
                        <FilterSelect label="Type" value={category} onChange={setCategory} options={CATEGORY_OPTIONS} />
                    </>
                }
            />

            <Card>
                <CardHeader className="pb-3">
                    <div className="flex items-center justify-between gap-3 flex-wrap">
                        <div>
                            <CardTitle className="text-[15px] flex items-center gap-2 text-red">
                                <ShieldAlert className="w-4 h-4" />
                                Conflict Register
                            </CardTitle>
                            <CardDescription>
                                Rule-engine output only. RMOS recommends; the planner decides.
                            </CardDescription>
                        </div>
                        <Badge variant="outline" className="font-mono text-[11px]">
                            {open.length} open · {resolved.length} resolved
                        </Badge>
                    </div>
                </CardHeader>
                <CardContent>
                    <DataTable
                        columns={columns}
                        rows={filtered}
                        rowId={(conflict) => conflict.id}
                        onRowClick={(conflict) => setSelectedId(conflict.id)}
                        selectedRowId={selectedId}
                        isLoading={conflictsState.isLoading}
                        errorMessage={conflictsState.error?.message ?? null}
                        emptyMessage="No conflicts match the selected filters — try widening the state filter."
                        initialSort={{ key: "severity", direction: "asc" }}
                    />
                </CardContent>
            </Card>

            <Sheet open={!!selected} onOpenChange={(openState) => !openState && setSelectedId(null)}>
                <SheetContent className="w-full sm:max-w-md overflow-y-auto">
                    {selected && (
                        <>
                            <SheetHeader className="mb-4">
                                <div className="flex items-center gap-2 flex-wrap">
                                    <Badge variant="outline">{selected.category}</Badge>
                                    <StatusChip status={priorityChip(selected.severity)} showDot={false} />
                                    <StatusChip status={conflictStatusChip(selected.status)} showDot={false} />
                                </div>
                                <SheetTitle className="text-[18px]">{selected.title}</SheetTitle>
                                <SheetDescription>
                                    {selected.description}
                                </SheetDescription>
                            </SheetHeader>

                            <div className="space-y-5 px-4 pb-4">
                                <div className="grid grid-cols-2 gap-3">
                                    <DetailItem label="Rule" value={<span className="font-mono">{selected.ruleId}</span>} />
                                    <DetailItem label="Trains affected" value={String(selected.affectedTrains)} />
                                    <DetailItem label="Detected" value={relativeTime(selected.detectedAt)} />
                                    <DetailItem label="Sections" value={selected.sectionIds.map(sectionName).join(", ")} />
                                </div>

                                <div className="border-l-2 border-amber-line bg-amber-bg/60 px-3 py-2.5">
                                    <div className="flex items-center gap-1.5 text-[11.5px] font-semibold uppercase tracking-[0.4px] text-amber">
                                        <Zap className="w-3.5 h-3.5" />
                                        Recommended action
                                    </div>
                                    <p className="text-[13px] text-ink mt-1">{selected.recommendedAction}</p>
                                </div>

                                {selected.resolutionNote && (
                                    <div className="border-l-2 border-green-line bg-green-bg/60 px-3 py-2.5 text-[12.5px] text-green">
                                        Resolution recorded: {selected.resolutionNote}
                                    </div>
                                )}

                                <div className="border rounded-md p-3 bg-slate-50 space-y-1.5 text-[12.5px]">
                                    <div className="flex justify-between gap-3">
                                        <span className="text-muted">Detection source</span>
                                        <span className="font-mono">{selected.sourceSystem}</span>
                                    </div>
                                    <div className="flex justify-between gap-3">
                                        <span className="text-muted">Record id</span>
                                        <span className="font-mono">{selected.sourceRecordId}</span>
                                    </div>
                                </div>

                                <div className="flex gap-2">
                                    <Button
                                        className="flex-1"
                                        disabled={selected.status === "resolved"}
                                        onClick={() =>
                                            changeStatus(selected, "resolved", `${selected.title} resolved.`)
                                        }
                                    >
                                        <CheckCircle2 className="w-4 h-4" />
                                        Mark resolved
                                    </Button>
                                    <Button
                                        variant="outline"
                                        className="flex-1"
                                        disabled={selected.status === "acknowledged"}
                                        onClick={() =>
                                            changeStatus(selected, "acknowledged", `${selected.title} acknowledged.`)
                                        }
                                    >
                                        <Eye className="w-4 h-4" />
                                        Acknowledge
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
