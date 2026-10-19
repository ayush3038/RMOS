import { useMemo, useState } from "react";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { useData } from "@/hooks/useData";
import { getRepository } from "@/services/api";
import { Clock, Network, Wand2, ThumbsUp, CheckCircle2 } from "lucide-react";
import { StatusChip } from "@/components/rmos/StatusChip";
import { requestStatusChip } from "@/lib/status";
import { useToast } from "@/store/toast";
import type { DepartmentRequest } from "@/types/models";

interface DepartmentCoordinationProps {
    /** Passed through so each request card can open that request's detail
     * sheet in the Departments module (?record=<request.id>). */
    onNavigate?: (page: string, record?: string) => void;
}

export function DepartmentCoordination({ onNavigate }: DepartmentCoordinationProps) {
    const repository = getRepository();
    const { data: requests, isLoading, error, refetch } = useData(() =>
        repository.getDepartmentRequests()
    );
    const { toast } = useToast();

    const [section, setSection] = useState<string>("all");
    const [overrides, setOverrides] = useState<Record<string, DepartmentRequest["status"]>>({});

    const all = useMemo(
        () =>
            (requests ?? []).map((request) => ({
                ...request,
                status: overrides[request.id] ?? request.status
            })),
        [requests, overrides]
    );

    const sections = useMemo(
        () => Array.from(new Set(all.map((request) => request.sectionId))).sort(),
        [all]
    );

    const busiest = useMemo(() => {
        const counts = new Map<string, number>();
        all.forEach((request) => counts.set(request.sectionId, (counts.get(request.sectionId) ?? 0) + 1));
        return Array.from(counts.entries()).sort((a, b) => b[1] - a[1])[0]?.[0] ?? "—";
    }, [all]);

    const visible = useMemo(
        () =>
            all
                .filter((request) => (section === "all" ? request.sectionId === busiest : request.sectionId === section))
                .slice(0, 6),
        [all, section, busiest]
    );

    const recommendation = useMemo(() => {
        const pool = all.filter(
            (request) =>
                request.sectionId === (section === "all" ? busiest : section) &&
                request.status !== "rejected"
        );
        if (pool.length === 0) return null;
        const start = pool.map((request) => request.requestedWindowStart).sort()[0];
        const end = pool.map((request) => request.requestedWindowEnd).sort().at(-1) ?? start;
        const departments = Array.from(new Set(pool.map((request) => request.department)));
        return { start, end, departments, count: pool.length };
    }, [all, section, busiest]);

    const review = (request: DepartmentRequest, status: DepartmentRequest["status"]) => {
        setOverrides((previous) => ({ ...previous, [request.id]: status }));
        toast(
            `${request.department} request ${status}${status === "approved" ? " — recorded in the demo audit trail." : "."}`,
            status === "approved" ? "success" : status === "rejected" ? "danger" : "info"
        );
    };

    const pending = all.filter((request) => request.status === "proposed").length;

    return (
        <Card>
            <CardHeader>
                <div className="flex items-center justify-between gap-2">
                    <div>
                        <CardTitle className="flex items-center gap-2">
                            <Network className="h-5 w-5 text-primary" />
                            Multi-Department Coordination
                        </CardTitle>
                        <CardDescription>Consolidated maintenance block requests</CardDescription>
                    </div>
                    <Badge variant="outline" className="bg-blue-50 text-blue-700 hover:bg-blue-50 border-blue-200">
                        {section === "all" ? `Busiest: ${busiest}` : section}
                    </Badge>
                </div>
            </CardHeader>
            <CardContent>
                <div className="space-y-4">
                    <div className="flex items-center justify-between gap-2 flex-wrap">
                        <h4 className="text-sm font-semibold text-slate-500 uppercase tracking-wider">
                            Individual Requests
                        </h4>
                        <div className="flex items-center gap-1.5">
                            <select
                                value={section}
                                onChange={(event) => setSection(event.target.value)}
                                className="h-7 rounded-md border border-input bg-surface px-2 text-[12px] outline-none focus:border-ring"
                                aria-label="Filter requests by section"
                            >
                                <option value="all">Busiest section</option>
                                {sections.map((value) => (
                                    <option key={value} value={value}>
                                        Section {value}
                                    </option>
                                ))}
                            </select>
                            <Button variant="ghost" size="xs" onClick={refetch} title="Reload requests">
                                Refresh
                            </Button>
                        </div>
                    </div>

                    {isLoading && <div className="text-sm text-slate-500 py-3 text-center">Loading requests...</div>}
                    {error && <div className="text-sm text-red-500 py-3 text-center">Failed to load requests</div>}
                    {!isLoading && !error && visible.length === 0 && (
                        <div className="text-sm text-slate-500 py-3 text-center">No department requests.</div>
                    )}

                    {!isLoading &&
                        !error &&
                        visible.map((req) => {
                            // The clickable region is the request info itself,
                            // kept as a sibling of the Approve/Reject buttons so
                            // no interactive element nests inside another and
                            // the review actions never trigger navigation.
                            const info = (
                                <>
                                    <div className="flex items-center gap-2">
                                        <span className="font-medium">{req.department}</span>
                                        <StatusChip
                                            status={requestStatusChip(req.status)}
                                            showDot={false}
                                            className="text-[10px] px-1.5 py-0"
                                        />
                                    </div>
                                    <div className="text-muted-foreground text-xs overflow-hidden h-6 py-1" title={req.activity}>
                                        {req.activity}
                                    </div>
                                </>
                            );

                            return (
                                <div
                                    key={req.id}
                                    className="flex flex-col p-3 border rounded-md bg-white text-sm shadow-sm gap-2 min-w-0"
                                >
                                    {onNavigate ? (
                                        <button
                                            type="button"
                                            onClick={() => onNavigate("departments", req.id)}
                                            title="Open request details"
                                            className="w-full min-w-0 text-left cursor-pointer rounded transition-colors hover:bg-slate-50 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring/60"
                                        >
                                            {info}
                                        </button>
                                    ) : (
                                        <div className="min-w-0">{info}</div>
                                    )}

                                    <div className="flex flex-wrap items-center justify-end gap-2 min-w-0">
                                    <div className="flex items-center gap-1 bg-slate-100 px-2 py-1 rounded text-slate-700 font-mono text-xs">
                                        <Clock className="h-3 w-3" />
                                        {req.requestedWindowStart} – {req.requestedWindowEnd}
                                    </div>

                                    {req.status === "proposed" && (
                                        <div className="flex gap-1.5 max-w-full">
                                            <button
                                                type="button"
                                                onClick={() => review(req, "approved")}
                                                className="flex-1 inline-flex items-center gap-1 h-6 px-2 rounded bg-blue text-white text-[11px] font-semibold hover:opacity-90"
                                            >
                                                <ThumbsUp className="h-3 w-3" />
                                                Approve
                                            </button>
                                            <button
                                                type="button"
                                                onClick={() => review(req, "rejected")}
                                                className="flex-1 h-6 px-2 rounded border text-[11px] font-semibold text-red hover:bg-red-bg"
                                            >
                                                Reject
                                            </button>
                                        </div>
                                    )}
                                </div>
                            </div>
                            );
                        })}

                    <div className="pt-3 border-t">
                        <div className="bg-gradient-to-r from-blue-50 to-indigo-50 border border-blue-100 rounded-lg p-4">
                            <div className="flex items-start justify-between gap-3">
                                <div className="min-w-0">
                                    <div className="flex items-center gap-2 text-indigo-700 font-semibold mb-1">
                                        <Wand2 className="h-4 w-4" />
                                        Recommended Integrated Block
                                    </div>
                                    <div className="text-sm text-indigo-900/80">
                                        {recommendation
                                            ? `${recommendation.departments.join(" + ")} · ${recommendation.count} request${recommendation.count > 1 ? "s" : ""} merged`
                                            : "No open requests for this section."}
                                    </div>
                                </div>
                                <div className="bg-white border border-indigo-200 shadow-sm px-3 py-1.5 rounded-md font-mono text-sm font-bold text-indigo-700 shrink-0">
                                    {recommendation ? `${recommendation.start} – ${recommendation.end}` : "—"}
                                </div>
                            </div>
                        </div>

                        <div className="flex items-center justify-between gap-2 mt-2 text-[11.5px] text-muted">
                            <span>{pending} request(s) awaiting review</span>
                            <button
                                type="button"
                                onClick={() => toast("Full coordination report queued in the demo report library.", "info")}
                                className="inline-flex items-center gap-1 text-blue hover:underline"
                            >
                                <CheckCircle2 className="h-3.5 w-3.5" />
                                Open coordination report
                            </button>
                        </div>
                    </div>
                </div>
            </CardContent>
        </Card>
    );
}
