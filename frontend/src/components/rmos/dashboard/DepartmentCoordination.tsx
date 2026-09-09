import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { useData } from "@/hooks/useData";
import { getRepository } from "@/services/api";
import { Network, Wand2, Clock } from "lucide-react";

export function DepartmentCoordination() {
    const { data: requests, isLoading, error } = useData(() => getRepository().getDepartmentRequests());

    return (
        <Card className="h-full">
            <CardHeader>
                <div className="flex items-center justify-between">
                    <div>
                        <CardTitle className="flex items-center gap-2">
                            <Network className="h-5 w-5 text-primary" />
                            Multi-Department Coordination
                        </CardTitle>
                        <CardDescription>Consolidated maintenance block requests</CardDescription>
                    </div>
                    <Badge variant="outline" className="bg-blue-50 text-blue-700 hover:bg-blue-50 border-blue-200">
                        Section A12
                    </Badge>
                </div>
            </CardHeader>
            <CardContent>
                <div className="space-y-4">
                    <div className="space-y-3">
                        <h4 className="text-sm font-semibold text-slate-500 uppercase tracking-wider">Individual Requests</h4>
                        {isLoading && <div className="text-sm text-slate-500 py-4 text-center">Loading requests...</div>}
                        {error && <div className="text-sm text-red-500 py-4 text-center">Failed to load requests</div>}
                        {!isLoading && !error && (!requests || requests.length === 0) && (
                            <div className="text-sm text-slate-500 py-4 text-center">No department requests.</div>
                        )}
                        {!isLoading && !error && requests && requests.map((req) => (
                            <div key={req.id} className="flex flex-col sm:flex-row sm:items-center justify-between p-3 border rounded-md bg-white text-sm shadow-sm">
                                <div>
                                    <div className="font-medium">{req.department}</div>
                                    <div className="text-muted-foreground text-xs">{req.activity}</div>
                                </div>
                                <div className="flex items-center gap-2 mt-2 sm:mt-0 bg-slate-100 px-2 py-1 rounded text-slate-700 font-mono text-xs">
                                    <Clock className="h-3 w-3" />
                                    {req.requestedWindowStart} – {req.requestedWindowEnd}
                                </div>
                            </div>
                        ))}
                    </div>

                    <div className="pt-4 mt-2 border-t">
                        <div className="bg-gradient-to-r from-blue-50 to-indigo-50 border border-blue-100 rounded-lg p-4">
                            <div className="flex items-start justify-between">
                                <div>
                                    <div className="flex items-center gap-2 text-indigo-700 font-semibold mb-1">
                                        <Wand2 className="h-4 w-4" />
                                        Recommended Integrated Block
                                    </div>
                                    <div className="text-sm text-indigo-900/80">
                                        Demo recommendation: Engineering + Traction + S&T
                                    </div>
                                </div>
                                <div className="bg-white border border-indigo-200 shadow-sm px-3 py-1.5 rounded-md font-mono text-sm font-bold text-indigo-700">
                                    02:30 – 04:30
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </CardContent>
        </Card>
    );
}
