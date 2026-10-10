import { useMemo, useState } from "react";
import {
    CheckCircle2,
    Clock,
    Network,
    Repeat2,
    ThumbsDown,
    ThumbsUp,
    Users,
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
import { useToast, type ToastTone } from "@/store/toast";
import { downloadCsv } from "@/lib/export";
import { priorityChip, requestStatusChip } from "@/lib/status";
import { formatDuration, relativeTime } from "@/lib/format";
import { DEMO_DEPARTMENTS } from "@/data/demo/mockData";
import type { DepartmentRequest } from "@/types/models";

const STATUS_OPTIONS = [
    { value: "all", label: "All states" },
    { value: "proposed", label: "Awaiting review" },
    { value: "approved", label: "Approved" },
    { value: "modified", label: "Modified" },
    { value: "rejected", label: "Rejected" }
];

const DEPARTMENT_OPTIONS = [
    { value: "all", label: "All departments" },
    ...DEMO_DEPARTMENTS.map((department) => ({ value: department, label: department }))
];

export function DepartmentsPage() {
    const { requests: requestsState, sections, refresh } = useModuleData();
    const { toast } = useToast();

    const { items: requests, update: updateRequest } = useLocalList<DepartmentRequest>(
        requestsState.data,
        (request) => request.id
    );

    const [search, setSearch] = useState("");
    const [department, setDepartment] = useState("all");
    const [status, setStatus] = useState("all");
    const [selectedId, setSelectedId] = useState<string | null>(null);

    const selected = requests.find((request) => request.id === selectedId) ?? null;

    const sectionName = (sectionId: string) =>
        sections.data?.find((section) => section.id === sectionId)?.name ?? sectionId;

    const filtered = useMemo(() => {
        const query = search.trim().toLowerCase();
        return requests.filter((request) => {
            if (department !== "all" && request.department !== department) return false;
            if (status !== "all" && request.status !== status) return false;
            if (!query) return true;
            return [
                request.activity,
                request.department,
                request.sectionId,
                request.requestedBy,
                request.requestedDate
            ]
                .join(" ")
                .toLowerCase()
                .includes(query);
        });
    }, [requests, search, department, status]);

    const proposed = requests.filter((request) => request.status === "proposed");
    const approved = requests.filter((request) => request.status === "approved");
    const rejected = requests.filter((request) => request.status === "rejected");
    const modified = requests.filter((request) => request.status === "modified");
    const requestedHours = Math.round(
        requests.reduce((total, request) => total + request.durationMinutes * request.workforce, 0) / 60
    );

    const kpis: KpiItem[] = [
        { label: "Awaiting Review", value: proposed.length, hint: "Cross-department asks", icon: Clock, tone: proposed.length ? "amber" : "green" },
        { label: "Approved", value: approved.length, hint: "Cleared for planning", icon: CheckCircle2, tone: "green" },
        { label: "Modified", value: modified.length, hint: "Window shifted", icon: Repeat2, tone: "blue" },
        { label: "Rejected", value: rejected.length, hint: "Returned with reason", icon: XCircle, tone: rejected.length ? "red" : "default" },
        { label: "Labour Requested", value: `${requestedHours} h`, hint: "Duration × workforce", icon: Users },
        { label: "Departments", value: new Set(requests.map((request) => request.department)).size, hint: "Contributing agencies", icon: Network }
    ];

    const review = (
        request: DepartmentRequest,
        next: DepartmentRequest["status"],
        message: string,
        tone?: ToastTone
    ) => {
        updateRequest(request.id, {
            status: next,
            reviewNote:
                next === "rejected"
                    ? "Rejected: clashes with sanctioned block."
                    : next === "modified"
                      ? "Shifted by 30 minutes to protect express path."
                      : "Reviewed and cleared by planner."
        });
        toast(
            message,
            tone ?? (next === "rejected" ? "danger" : next === "modified" ? "info" : "success")
        );
    };

    const columns: DataTableColumn<DepartmentRequest>[] = [
        {
            key: "activity",
            header: "Request",
            sortValue: (request) => request.activity,
            cell: (request) => (
                <div className="leading-tight">
                    <div className="text-[12.5px] font-medium">{request.activity}</div>
                    <div className="text-[11px] text-muted">
                        {request.requestedBy} · {relativeTime(request.sourceTimestamp)}
                    </div>
                </div>
            )
        },
        {
            key: "department",
            header: "Department",
            sortValue: (request) => request.department,
            cell: (request) => <span className="text-[12.5px]">{request.department}</span>
        },
        {
            key: "section",
            header: "Section",
            sortValue: (request) => request.sectionId,
            cell: (request) => <span className="text-[12.5px]">{sectionName(request.sectionId)}</span>
        },
        {
            key: "date",
            header: "Date",
            sortValue: (request) => request.requestedDate,
            cell: (request) => <span className="text-[12.5px] text-muted">{request.requestedDate}</span>
        },
        {
            key: "window",
            header: "Window",
            sortValue: (request) => request.requestedWindowStart,
            cell: (request) => (
                <span className="text-[12.5px] font-mono">
                    {request.requestedWindowStart}–{request.requestedWindowEnd}
                </span>
            )
        },
        {
            key: "duration",
            header: "Duration",
            sortValue: (request) => request.durationMinutes,
            cell: (request) => <span className="text-[12.5px] font-mono">{formatDuration(request.durationMinutes)}</span>
        },
        {
            key: "workforce",
            header: "Crew",
            sortValue: (request) => request.workforce,
            cell: (request) => <span className="text-[12.5px] font-mono">{request.workforce}</span>
        },
        {
            key: "priority",
            header: "Priority",
            sortValue: (request) => request.priority,
            cell: (request) => (
                <StatusChip status={priorityChip(request.priority)} showDot={false} className="text-[10px] px-1.5 py-0" />
            )
        },
        {
            key: "status",
            header: "State",
            sortValue: (request) => request.status,
            cell: (request) => (
                <StatusChip status={requestStatusChip(request.status)} showDot={false} className="text-[10px] px-1.5 py-0" />
            )
        },
        {
            key: "actions",
            header: "",
            headerClassName: "w-[168px]",
            cell: (request) =>
                request.status === "proposed" ? (
                    <div className="flex items-center gap-1.5">
                        <Button
                            size="xs"
                            onClick={(event) => {
                                event.stopPropagation();
                                review(request, "approved", `${request.department} request approved for ${request.requestedDate}.`);
                            }}
                        >
                            <ThumbsUp className="w-3 h-3" />
                            Approve
                        </Button>
                        <Button
                            size="xs"
                            variant="outline"
                            onClick={(event) => {
                                event.stopPropagation();
                                review(
                                    request,
                                    "modified",
                                    `${request.department} window shifted by 30 minutes.`,
                                    "info"
                                );
                            }}
                        >
                            <Repeat2 className="w-3 h-3" />
                        </Button>
                        <Button
                            size="xs"
                            variant="outline"
                            onClick={(event) => {
                                event.stopPropagation();
                                review(
                                    request,
                                    "rejected",
                                    `${request.department} request rejected.`,
                                    "danger"
                                );
                            }}
                        >
                            <ThumbsDown className="w-3 h-3" />
                        </Button>
                    </div>
                ) : (
                    <span className="text-[11.5px] text-muted">
                        {request.reviewNote ? "Reviewed" : "No action"}
                    </span>
                )
        }
    ];

    const exportRequests = () =>
        downloadCsv(
            "rmos-department-requests",
            [
                { key: "activity", header: "Activity", value: (request) => request.activity },
                { key: "department", header: "Department", value: (request) => request.department },
                { key: "sectionId", header: "Section", value: (request) => request.sectionId },
                { key: "requestedDate", header: "Date", value: (request) => request.requestedDate },
                { key: "requestedWindowStart", header: "Window start", value: (request) => request.requestedWindowStart },
                { key: "requestedWindowEnd", header: "Window end", value: (request) => request.requestedWindowEnd },
                { key: "priority", header: "Priority", value: (request) => request.priority },
                { key: "workforce", header: "Workforce", value: (request) => request.workforce },
                { key: "status", header: "Status", value: (request) => request.status }
            ],
            filtered
        );

    return (
        <div className="flex flex-col gap-4">
            <KpiStrip items={kpis} />

            <ModuleToolbar
                search={search}
                onSearchChange={setSearch}
                placeholder="Search activity, department, requester…"
                resultLabel={`${filtered.length} of ${requests.length} requests`}
                onRefresh={refresh}
                onExport={exportRequests}
                exportDisabled={filtered.length === 0}
                filters={
                    <>
                        <FilterSelect label="Dept" value={department} onChange={setDepartment} options={DEPARTMENT_OPTIONS} />
                        <FilterSelect label="State" value={status} onChange={setStatus} options={STATUS_OPTIONS} />
                    </>
                }
            />

            <Card>
                <CardHeader className="pb-3">
                    <div className="flex items-center justify-between gap-3 flex-wrap">
                        <div>
                            <CardTitle className="text-[15px] flex items-center gap-2">
                                <Network className="w-4 h-4 text-primary" />
                                Cross-Department Request Desk
                            </CardTitle>
                            <CardDescription>
                                Every approval, modification and rejection is recorded in the demo audit trail.
                            </CardDescription>
                        </div>
                        <Badge variant="outline" className="font-mono text-[11px]">
                            {proposed.length} pending review
                        </Badge>
                    </div>
                </CardHeader>
                <CardContent>
                    <DataTable
                        columns={columns}
                        rows={filtered}
                        rowId={(request) => request.id}
                        onRowClick={(request) => setSelectedId(request.id)}
                        selectedRowId={selectedId}
                        isLoading={requestsState.isLoading}
                        errorMessage={requestsState.error?.message ?? null}
                        emptyMessage="No department requests match the selected filters."
                        initialSort={{ key: "date", direction: "desc" }}
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
                                    <StatusChip status={requestStatusChip(selected.status)} showDot={false} />
                                    <StatusChip status={priorityChip(selected.priority)} showDot={false} />
                                </div>
                                <SheetTitle className="text-[18px]">{selected.activity}</SheetTitle>
                                <SheetDescription>
                                    Raised by {selected.requestedBy} for {sectionName(selected.sectionId)} ·{" "}
                                    <span className="font-mono">{selected.sourceRecordId}</span>
                                </SheetDescription>
                            </SheetHeader>

                            <div className="space-y-5 px-4 pb-4">
                                <DetailItem
                                    label="Requested window"
                                    value={
                                        <span className="font-mono">
                                            {selected.requestedDate} · {selected.requestedWindowStart}–
                                            {selected.requestedWindowEnd}
                                        </span>
                                    }
                                />

                                <div className="grid grid-cols-2 gap-3">
                                    <DetailItem label="Section" value={sectionName(selected.sectionId)} />
                                    <DetailItem label="Priority" value={<StatusChip status={priorityChip(selected.priority)} showDot={false} />} />
                                    <DetailItem label="Duration" value={formatDuration(selected.durationMinutes)} />
                                    <DetailItem label="Workforce" value={`${selected.workforce} staff`} />
                                    <DetailItem label="Requester" value={selected.requestedBy} />
                                    <DetailItem label="Submitted" value={relativeTime(selected.sourceTimestamp)} />
                                </div>

                                {selected.reviewNote && (
                                    <div className="border-l-2 border-blue-line bg-blue-bg/50 px-3 py-2 text-[12.5px] text-blue">
                                        Review note: {selected.reviewNote}
                                    </div>
                                )}

                                <div className="border rounded-md p-3 bg-slate-50 space-y-1.5 text-[12.5px]">
                                    <div className="flex justify-between gap-3">
                                        <span className="text-muted">Source system</span>
                                        <span className="font-mono">{selected.sourceSystem}</span>
                                    </div>
                                    <div className="flex justify-between gap-3">
                                        <span className="text-muted">Data mode</span>
                                        <span>{selected.isSimulation ? "Simulation / demo" : "Live"}</span>
                                    </div>
                                </div>

                                <div className="flex gap-2">
                                    <Button
                                        className="flex-1"
                                        disabled={selected.status === "approved"}
                                        onClick={() =>
                                            review(
                                                selected,
                                                "approved",
                                                `${selected.department} request approved for ${selected.requestedDate}.`
                                            )
                                        }
                                    >
                                        <CheckCircle2 className="w-4 h-4" />
                                        Approve
                                    </Button>
                                    <Button
                                        variant="outline"
                                        className="flex-1"
                                        disabled={selected.status === "modified"}
                                        onClick={() =>
                                            review(
                                                selected,
                                                "modified",
                                                `${selected.department} window shifted by 30 minutes.`,
                                                "info"
                                            )
                                        }
                                    >
                                        <Repeat2 className="w-4 h-4" />
                                        Modify
                                    </Button>
                                    <Button
                                        variant="destructive"
                                        disabled={selected.status === "rejected"}
                                        onClick={() =>
                                            review(selected, "rejected", `${selected.department} request rejected.`, "danger")
                                        }
                                    >
                                        <XCircle className="w-4 h-4" />
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
