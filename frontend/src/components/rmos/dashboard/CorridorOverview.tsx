import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { DEMO_SECTIONS } from "@/data/demo/mockData";
import { Map, Train, Wrench, AlertOctagon } from "lucide-react";

export function CorridorOverview() {
    return (
        <Card className="h-full">
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
                <div className="space-y-4">
                    {DEMO_SECTIONS.map((section) => (
                        <div key={section.id} className="flex items-center gap-4 p-3 border rounded-lg bg-slate-50/50">
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
                        </div>
                    ))}
                </div>

                <div className="flex items-center gap-4 mt-6 text-xs text-muted-foreground justify-end">
                    <div className="flex items-center gap-1"><div className="w-2 h-2 rounded-full bg-green-500" /> Available</div>
                    <div className="flex items-center gap-1"><div className="w-2 h-2 rounded-full bg-blue-500" /> Maintenance</div>
                    <div className="flex items-center gap-1"><div className="w-2 h-2 rounded-full bg-amber-500" /> Restricted</div>
                    <div className="flex items-center gap-1"><div className="w-2 h-2 rounded-full bg-red-500" /> Blocked</div>
                </div>
            </CardContent>
        </Card>
    );
}
