import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { DEMO_EVENTS } from "@/data/demo/mockData";
import { Activity, Bell, AlertTriangle, CalendarRange, Clock, Wrench } from "lucide-react";

export function ActivityFeed() {
    const getEventIcon = (type: string) => {
        switch (type) {
            case "conflict_detected": return <AlertTriangle className="h-4 w-4 text-red-500" />;
            case "request_added": return <Bell className="h-4 w-4 text-blue-500" />;
            case "train_delayed": return <Clock className="h-4 w-4 text-amber-500" />;
            case "block_changed": return <CalendarRange className="h-4 w-4 text-indigo-500" />;
            case "maintenance_updated": return <Wrench className="h-4 w-4 text-slate-500" />;
            default: return <Activity className="h-4 w-4 text-slate-500" />;
        }
    };

    const formatTime = (isoDate: string) => {
        const date = new Date(isoDate);
        return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    };

    return (
        <Card className="h-full">
            <CardHeader className="pb-4">
                <div className="flex items-center justify-between">
                    <div>
                        <CardTitle className="flex items-center gap-2">
                            <Activity className="h-5 w-5 text-primary" />
                            Recent Activity
                        </CardTitle>
                        <CardDescription>Latest simulated operational events</CardDescription>
                    </div>
                </div>
            </CardHeader>
            <CardContent>
                <div className="space-y-4">
                    {DEMO_EVENTS.map((event) => (
                        <div key={event.id} className="flex gap-3 relative pb-4 last:pb-0">
                            <div className="mt-0.5 shrink-0 bg-slate-100 p-1.5 rounded-full z-10 border border-white">
                                {getEventIcon(event.type)}
                            </div>

                            <div className="absolute left-[15px] top-8 bottom-0 w-px bg-slate-200 -z-0 last:hidden" />

                            <div>
                                <div className="text-sm font-medium text-slate-800">{event.message}</div>
                                <div className="text-xs text-muted-foreground mt-0.5">{formatTime(event.timestamp)}</div>
                            </div>
                        </div>
                    ))}
                </div>
            </CardContent>
        </Card>
    );
}
