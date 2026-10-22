import { useEffect, useMemo, useRef, useState } from "react";
import {
    CheckCircle2,
    Download,
    FileSpreadsheet,
    FileText,
    PlayCircle,
    Printer,
    RefreshCcw,
    Timer,
    XCircle
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
import { reportStatusChip } from "@/lib/status";
import { formatDateTime, relativeTime } from "@/lib/format";
import type { ReportItem } from "@/types/models";

const CATEGORY_OPTIONS = [
    { value: "all", label: "All categories" },
    { value: "Operational", label: "Operational" },
    { value: "Maintenance", label: "Maintenance" },
    { value: "Planning", label: "Planning" },
    { value: "Compliance", label: "Compliance" },
    { value: "Financial", label: "Financial" }
];

const STATUS_OPTIONS = [
    { value: "all", label: "All states" },
    { value: "ready", label: "Ready" },
    { value: "scheduled", label: "Scheduled" },
    { value: "generating", label: "Generating" },
    { value: "failed", label: "Failed" }
];

export function ReportsPage({ record }: { record?: string | null }) {
    const { reports: reportsState, refresh } = useModuleData();
    const { toast } = useToast();

    const { items: reports, update: updateReport } = useLocalList<ReportItem>(
        reportsState.data,
        (report) => report.id
    );

    const [search, setSearch] = useState("");
    const [category, setCategory] = useState("all");
    const [status, setStatus] = useState("all");
    const [selectedId, setSelectedId] = useState<string | null>(null);

    const selected = reports.find((report) => report.id === selectedId) ?? null;

    // Notification deep link (?record=RPT-202603): open the referenced report's
    // detail sheet once the library has loaded, or explain when it is missing.
    const appliedRecord = useRef<string | null>(null);
    useEffect(() => {
        const key = record?.trim();
        if (!key || appliedRecord.current === key || reportsState.isLoading) return;
        const match = reports.find(
            (report) => report.reportCode.toUpperCase() === key.toUpperCase()
        );
        appliedRecord.current = key;
        if (match) {
            setSelectedId(match.id);
        } else if (!reportsState.error) {
            toast(`Linked record ${key} was not found in the report library.`, "warning");
        }
    }, [record, reports, reportsState.isLoading, reportsState.error, toast]);

    const filtered = useMemo(() => {
        const query = search.trim().toLowerCase();
        return reports.filter((report) => {
            if (category !== "all" && report.category !== category) return false;
            if (status !== "all" && report.status !== status) return false;
            if (!query) return true;
            return [report.reportCode, report.title, report.author, report.period]
                .join(" ")
                .toLowerCase()
                .includes(query);
        });
    }, [reports, search, category, status]);

    const ready = reports.filter((report) => report.status === "ready");
    const scheduled = reports.filter((report) => report.status === "scheduled");
    const failed = reports.filter((report) => report.status === "failed");
    const generating = reports.filter((report) => report.status === "generating");
    const storageKb = reports.reduce((total, report) => total + report.sizeKb, 0);

    const runReport = (report: ReportItem) => {
        updateReport(report.id, { status: "generating" });
        toast(`${report.reportCode} generation started.`, "info");
        window.setTimeout(() => {
            updateReport(report.id, {
                status: "ready",
                generatedAt: new Date().toISOString(),
                sizeKb: 60 + Math.round(report.sizeKb * 0.6)
            });
            toast(`${report.reportCode} is ready to download.`);
        }, 1400);
    };

    const downloadReport = (report: ReportItem) => {
        downloadCsv(
            report.reportCode,
            [
                { key: "field", header: "Field", value: (row: Record<string, string>) => row.field },
                { key: "value", header: "Value", value: (row: Record<string, string>) => row.value }
            ],
            [
                { field: "Report", value: report.title },
                { field: "Category", value: report.category },
                { field: "Period", value: report.period },
                { field: "Status", value: report.status },
                { field: "Generated", value: report.generatedAt },
                { field: "Author", value: report.author },
                { field: "Source system", value: report.sourceSystem },
                { field: "Data mode", value: report.isSimulation ? "Simulation / demo" : "Live" }
            ]
        );
        toast(`${report.reportCode} downloaded as CSV.`, "info");
    };

    const kpis: KpiItem[] = [
        { label: "Ready", value: ready.length, hint: "Downloadable now", icon: CheckCircle2, tone: "green" },
        { label: "Scheduled", value: scheduled.length, hint: "Queued by cron plan", icon: Timer, tone: "blue" },
        { label: "Generating", value: generating.length, hint: "In progress", icon: RefreshCcw },
        { label: "Failed", value: failed.length, hint: "Needs re-run", icon: XCircle, tone: failed.length ? "red" : "green" },
        { label: "Archive Size", value: `${(storageKb / 1024).toFixed(1)} MB`, hint: "Across all reports", icon: FileSpreadsheet },
        { label: "Categories", value: new Set(reports.map((report) => report.category)).size, hint: "Report families", icon: FileText }
    ];

    const columns: DataTableColumn<ReportItem>[] = [
        {
            key: "reportCode",
            header: "Report",
            sortValue: (report) => report.reportCode,
            cell: (report) => (
                <div className="leading-tight">
                    <div className="font-mono text-[12.5px] font-semibold">{report.reportCode}</div>
                    <div className="text-[11px] text-muted">{report.author}</div>
                </div>
            )
        },
        {
            key: "title",
            header: "Title",
            sortValue: (report) => report.title,
            cell: (report) => (
                <span className="text-[12.5px] max-w-[300px] inline-block truncate">{report.title}</span>
            )
        },
        {
            key: "category",
            header: "Category",
            sortValue: (report) => report.category,
            cell: (report) => <span className="text-[12.5px]">{report.category}</span>
        },
        {
            key: "period",
            header: "Period",
            sortValue: (report) => report.period,
            cell: (report) => <span className="text-[12px] text-muted">{report.period}</span>
        },
        {
            key: "format",
            header: "Format",
            sortValue: (report) => report.format,
            cell: (report) => (
                <Badge variant="outline" className="font-mono text-[10.5px]">
                    {report.format}
                </Badge>
            )
        },
        {
            key: "size",
            header: "Size",
            sortValue: (report) => report.sizeKb,
            cell: (report) => <span className="text-[12px] font-mono">{report.sizeKb} KB</span>
        },
        {
            key: "generated",
            header: "Generated",
            sortValue: (report) => new Date(report.generatedAt).getTime(),
            cell: (report) => <span className="text-[11.5px] text-muted">{relativeTime(report.generatedAt)}</span>
        },
        {
            key: "status",
            header: "State",
            sortValue: (report) => report.status,
            cell: (report) => (
                <StatusChip status={reportStatusChip(report.status)} showDot={false} className="text-[10px] px-1.5 py-0" />
            )
        },
        {
            key: "actions",
            header: "",
            headerClassName: "w-[150px]",
            cell: (report) => (
                <div className="flex items-center gap-1.5">
                    <Button
                        size="xs"
                        variant="outline"
                        disabled={report.status === "generating"}
                        onClick={(event) => {
                            event.stopPropagation();
                            runReport(report);
                        }}
                    >
                        <PlayCircle className="w-3 h-3" />
                        Run
                    </Button>
                    <Button
                        size="xs"
                        variant="ghost"
                        disabled={report.status !== "ready"}
                        onClick={(event) => {
                            event.stopPropagation();
                            downloadReport(report);
                        }}
                    >
                        <Download className="w-3 h-3" />
                        CSV
                    </Button>
                </div>
            )
        }
    ];

    const exportIndex = () =>
        downloadCsv(
            "rmos-report-index",
            [
                { key: "reportCode", header: "Code", value: (report) => report.reportCode },
                { key: "title", header: "Title", value: (report) => report.title },
                { key: "category", header: "Category", value: (report) => report.category },
                { key: "period", header: "Period", value: (report) => report.period },
                { key: "format", header: "Format", value: (report) => report.format },
                { key: "sizeKb", header: "Size (KB)", value: (report) => report.sizeKb },
                { key: "generatedAt", header: "Generated", value: (report) => report.generatedAt },
                { key: "status", header: "Status", value: (report) => report.status }
            ],
            filtered
        );

    return (
        <div className="flex flex-col gap-4">
            <KpiStrip items={kpis} />

            <ModuleToolbar
                search={search}
                onSearchChange={setSearch}
                placeholder="Search report code, title, author…"
                resultLabel={`${filtered.length} of ${reports.length} reports`}
                onRefresh={refresh}
                onExport={exportIndex}
                exportDisabled={filtered.length === 0}
                filters={
                    <>
                        <FilterSelect label="Category" value={category} onChange={setCategory} options={CATEGORY_OPTIONS} />
                        <FilterSelect label="State" value={status} onChange={setStatus} options={STATUS_OPTIONS} />
                    </>
                }
                actions={
                    <Button
                        size="sm"
                        className="h-8"
                        onClick={() => {
                            const target =
                                reports.find((report) => report.status === "scheduled") ??
                                reports.find((report) => report.status === "failed") ??
                                reports[0];
                            if (target) runReport(target);
                        }}
                    >
                        <Printer className="w-3.5 h-3.5" />
                        Run next scheduled
                    </Button>
                }
            />

            <Card>
                <CardHeader className="pb-3">
                    <div className="flex items-center justify-between gap-3 flex-wrap">
                        <div>
                            <CardTitle className="text-[15px] flex items-center gap-2">
                                <FileText className="w-4 h-4 text-primary" />
                                Report Library
                            </CardTitle>
                            <CardDescription>
                                Generated artefacts are synthetic demo files · run or download directly from the table.
                            </CardDescription>
                        </div>
                        <Badge variant="outline" className="font-mono text-[11px]">
                            {ready.length} ready · {scheduled.length} scheduled
                        </Badge>
                    </div>
                </CardHeader>
                <CardContent>
                    <DataTable
                        columns={columns}
                        rows={filtered}
                        rowId={(report) => report.id}
                        onRowClick={(report) => setSelectedId(report.id)}
                        selectedRowId={selectedId}
                        isLoading={reportsState.isLoading}
                        errorMessage={reportsState.error?.message ?? null}
                        emptyMessage="No reports match the selected filters."
                        initialSort={{ key: "generated", direction: "desc" }}
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
                                    <Badge variant="secondary" className="font-mono text-[11px]">
                                        {selected.format}
                                    </Badge>
                                    <StatusChip status={reportStatusChip(selected.status)} showDot={false} />
                                </div>
                                <SheetTitle className="text-[18px]">{selected.title}</SheetTitle>
                                <SheetDescription>
                                    <span className="font-mono">{selected.reportCode}</span> · {selected.period}
                                </SheetDescription>
                            </SheetHeader>

                            <div className="space-y-5 px-4 pb-4">
                                <div className="grid grid-cols-2 gap-3">
                                    <DetailItem label="Size" value={`${selected.sizeKb} KB`} />
                                    <DetailItem label="Author" value={selected.author} />
                                    <DetailItem label="Generated" value={formatDateTime(selected.generatedAt)} />
                                    <DetailItem label="Freshness" value={relativeTime(selected.generatedAt)} />
                                </div>

                                <div className="border rounded-md p-3 bg-slate-50 space-y-1.5 text-[12.5px]">
                                    <div className="flex justify-between gap-3">
                                        <span className="text-muted">Source system</span>
                                        <span className="font-mono">{selected.sourceSystem}</span>
                                    </div>
                                    <div className="flex justify-between gap-3">
                                        <span className="text-muted">Record id</span>
                                        <span className="font-mono">{selected.sourceRecordId}</span>
                                    </div>
                                    <div className="flex justify-between gap-3">
                                        <span className="text-muted">Data mode</span>
                                        <span>{selected.isSimulation ? "Simulation / demo" : "Live"}</span>
                                    </div>
                                </div>

                                <div className="flex gap-2">
                                    <Button
                                        className="flex-1"
                                        disabled={selected.status === "generating"}
                                        onClick={() => runReport(selected)}
                                    >
                                        <PlayCircle className="w-4 h-4" />
                                        {selected.status === "generating" ? "Generating…" : "Re-run report"}
                                    </Button>
                                    <Button
                                        variant="outline"
                                        className="flex-1"
                                        disabled={selected.status !== "ready"}
                                        onClick={() => downloadReport(selected)}
                                    >
                                        <Download className="w-4 h-4" />
                                        Download CSV
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
