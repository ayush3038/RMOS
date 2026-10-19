import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { useData } from "@/hooks/useData";
import { getRepository } from "@/services/api";
import { cn } from "@/lib/utils";
import { Map, Train, Wrench, AlertOctagon } from "lucide-react";

interface CorridorOverviewProps {
    /** Passed through so each row can open the section's detail sheet in the
     * Corridor module (?record=<section.id>). Optional: without it the rows
     * stay plain display rows. */
    onNavigate?: (page: string, record?: string) => void;
}

export function CorridorOverview({ onNavigate }: CorridorOverviewProps) {
    const { data: sections, isLoading, error } = useData(() => getRepository().getCorridorSections());

    return (
        <Card>
            <CardHeader>
                <div className="flex items-center justify-between">
                    <div>
                        <CardTitle className="flex items-center gap-2">
                            <Map className="h-5 w-5 text-primary" />
                            Corridor Overview
                        </CardTitle>
                        <CardDescription>Real-time synthetic state of critical network sections</CardDescription>
                    </div>
                </div>
            </CardHeader>
            <CardContent>
                {isLoading && <div className="text-sm text-slate-500 py-3 text-center">Loading corridors...</div>}
                {error && <div className="text-sm text-red-500 py-3 text-center">Failed to load corridors</div>}
                {!isLoading && !error && (!sections || sections.length === 0) && (
                    <div className="text-sm text-slate-500 py-3 text-center">No corridor data available.</div>
                )}
                {!isLoading && !error && sections && (
                    <div className="space-y-3">
                        {sections.map((section) => {
                            // The Corridor module opens a section's detail sheet
                            // from ?record=<section.id>, so each row deep-links
                            // there. Badges inside are display-only, so a real
                            // <button> row introduces no nested interactives.
                            const rowContent = (
                                <>
                                    <div className="w-24 font-medium text-sm">
                                        {section.name}
                                    </div>

                                    <div className="flex-1">
                                        <div className="h-2 w-full rounded-full bg-slate-200 overflow-hidden flex">
                                            {section.availabilityStatus === 'available' && <div className="h-full w-full bg-green-500" />}
                                            {section.availabilityStatus === 'restricted' && <div className="h-full w-full bg-amber-500" />}
                                            {section.availabilityStatus === 'maintenance' && <div className="h-full w-full bg-blue-500" />}
                                            {section.availabilityStatus === 'blocked' && <div className="h-full w-full bg-red-500" />}
                                        </div>
                                    </div>

                                    <div className="flex items-center gap-3 w-40 justify-end">
                                        {section.currentTrains > 0 && (
                                            <Badge variant="outline" className="flex items-center gap-1 font-mono text-xs text-slate-600">
                                                <Train className="h-3 w-3" />
                                                {section.currentTrains}
                                            </Badge>
                                        )}
                                        {section.upcomingMaintenanceCode && (
                                            <Badge variant="secondary" className="flex items-center gap-1 font-mono text-xs bg-blue-100 text-blue-700">
                                                <Wrench className="h-3 w-3" />
                                                {section.upcomingMaintenanceCode}
                                            </Badge>
                                        )}
                                        {section.availabilityStatus === 'blocked' && (
                                            <Badge variant="destructive" className="flex items-center gap-1 text-xs">
                                                <AlertOctagon className="h-3 w-3" />
                                                Blocked
                                            </Badge>
                                        )}
                                    </div>
                                </>
                            );
                            const rowClass = cn(
                                "w-full flex items-center gap-4 p-3 border rounded-lg bg-slate-50/50",
                                onNavigate &&
                                    "cursor-pointer text-left transition-colors hover:bg-slate-100/70 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring/60"
                            );

                            return onNavigate ? (
                                <button
                                    key={section.id}
                                    type="button"
                                    onClick={() => onNavigate("corridor", section.id)}
                                    title={`Open details for ${section.name}`}
                                    className={rowClass}
                                >
                                    {rowContent}
                                </button>
                            ) : (
                                <div key={section.id} className={rowClass}>
                                    {rowContent}
                                </div>
                            );
                        })}
                    </div>
                )}

                <div className="flex items-center gap-4 mt-4 text-xs text-muted-foreground justify-end">
                    <div className="flex items-center gap-1"><div className="w-2 h-2 rounded-full bg-green-500" /> Available</div>
                    <div className="flex items-center gap-1"><div className="w-2 h-2 rounded-full bg-blue-500" /> Maintenance</div>
                    <div className="flex items-center gap-1"><div className="w-2 h-2 rounded-full bg-amber-500" /> Restricted</div>
                    <div className="flex items-center gap-1"><div className="w-2 h-2 rounded-full bg-red-500" /> Blocked</div>
                </div>
            </CardContent>
        </Card>
    );
}
