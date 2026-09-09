import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { useData } from "@/hooks/useData";
import { getRepository } from "@/services/api";
import { ShieldAlert, Info, Zap } from "lucide-react";
import { StatusChip } from "@/components/rmos/StatusChip";
import type { StatusLevel } from "@/components/rmos/StatusChip";

const capitalize = (str: string): StatusLevel => {
    return (str.charAt(0).toUpperCase() + str.slice(1)) as StatusLevel;
};

export function ConflictPanel() {
    const { data: conflicts, isLoading, error } = useData(() => getRepository().getConflicts());

    return (
        <Card className="h-full border-red-100">
            <CardHeader className="bg-red-50/50 pb-4">
                <div className="flex items-center justify-between">
                    <div>
                        <CardTitle className="flex items-center gap-2 text-red-700">
                            <ShieldAlert className="h-5 w-5" />
                            Active Conflicts
                        </CardTitle>
                        <CardDescription className="text-red-900/60">Automated conflict detection rules triggered</CardDescription>
                    </div>
                </div>
            </CardHeader>
            <CardContent className="pt-4">
                {isLoading && <div className="text-sm text-slate-500 py-4 text-center">Loading conflicts...</div>}
                {error && <div className="text-sm text-red-500 py-4 text-center">Failed to load conflicts</div>}
                {!isLoading && !error && (!conflicts || conflicts.length === 0) && (
                    <div className="text-sm text-emerald-600 py-4 text-center">No active conflicts detected.</div>
                )}
                {!isLoading && !error && conflicts && conflicts.length > 0 && (
                    <div className="space-y-4">
                        {conflicts.map((conflict) => (
                            <div key={conflict.id} className="p-3 border border-red-100 rounded-lg bg-white shadow-sm space-y-2">
                                <div className="flex items-start justify-between">
                                    <div className="flex items-center gap-2">
                                        <StatusChip
                                            status={capitalize(conflict.severity)}
                                            showDot={false}
                                            className="text-[10px] px-1.5 py-0 h-4"
                                        />
                                        <span className="text-xs font-semibold text-slate-600 uppercase tracking-wider">{conflict.category}</span>
                                    </div>
                                </div>
                                <div>
                                    <h4 className="text-sm font-semibold">{conflict.title}</h4>
                                    <p className="text-xs text-muted-foreground mt-1 leading-relaxed">
                                        {conflict.description}
                                    </p>
                                </div>
                                <div className="pt-2 border-t flex items-start gap-2 text-xs">
                                    <Zap className="h-3.5 w-3.5 text-amber-500 mt-0.5 shrink-0" />
                                    <span className="font-medium text-slate-700">Action:</span>
                                    <span className="text-slate-600">{conflict.recommendedAction}</span>
                                </div>
                            </div>
                        ))}
                    </div>
                )}

                <div className="mt-4 flex items-start gap-2 text-xs text-slate-500 bg-slate-50 p-2 rounded">
                    <Info className="h-4 w-4 shrink-0 text-slate-400" />
                    <p>These conflicts are generated based on simulation data and are for demonstration only.</p>
                </div>
            </CardContent>
        </Card>
    );
}
