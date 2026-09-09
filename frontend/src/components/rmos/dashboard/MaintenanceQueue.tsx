import { useState } from "react";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Sheet, SheetContent, SheetHeader, SheetTitle, SheetDescription } from "@/components/ui/sheet";
import { Badge } from "@/components/ui/badge";
import { useData } from "@/hooks/useData";
import { getRepository } from "@/services/api";
import type { MaintenanceTask } from "@/types/models";
import { StatusChip } from "@/components/rmos/StatusChip";
import type { StatusLevel } from "@/components/rmos/StatusChip";
import { ListTodo, Clock, CalendarDays } from "lucide-react";

const capitalize = (str: string): StatusLevel => {
    if (str === "in-progress") return "Scheduled"; // Fallback to blue/amber as needed
    return (str.charAt(0).toUpperCase() + str.slice(1)) as StatusLevel;
};

export function MaintenanceQueue() {
    const { data: tasks, isLoading, error } = useData(() => getRepository().getMaintenanceTasks());
    const [selectedTask, setSelectedTask] = useState<MaintenanceTask | null>(null);

    return (
        <Card className="col-span-full h-full">
            <CardHeader>
                <CardTitle className="flex items-center gap-2">
                    <ListTodo className="h-5 w-5 text-primary" />
                    Maintenance Priority Queue
                </CardTitle>
                <CardDescription>Filtered operational tasks pending scheduling or currently active.</CardDescription>
            </CardHeader>
            <CardContent>
                <div className="rounded-md border overflow-x-auto relative">
                    <Table>
                        <TableHeader>
                            <TableRow className="bg-slate-50">
                                <TableHead className="w-[100px]">Task ID</TableHead>
                                <TableHead>Department</TableHead>
                                <TableHead>Asset</TableHead>
                                <TableHead>Section</TableHead>
                                <TableHead>Criticality</TableHead>
                                <TableHead>Urgency</TableHead>
                                <TableHead>Due Date</TableHead>
                                <TableHead>Duration</TableHead>
                                <TableHead className="text-right">Status</TableHead>
                            </TableRow>
                        </TableHeader>
                        <TableBody>
                            {isLoading && (
                                <TableRow>
                                    <TableCell colSpan={9} className="h-24 text-center text-slate-500">Loading tasks...</TableCell>
                                </TableRow>
                            )}
                            {error && (
                                <TableRow>
                                    <TableCell colSpan={9} className="h-24 text-center text-red-500">Failed to load tasks</TableCell>
                                </TableRow>
                            )}
                            {!isLoading && !error && (!tasks || tasks.length === 0) && (
                                <TableRow>
                                    <TableCell colSpan={9} className="h-24 text-center text-slate-500">No maintenance tasks.</TableCell>
                                </TableRow>
                            )}
                            {!isLoading && !error && tasks && tasks.map((task) => (
                                <TableRow
                                    key={task.id}
                                    className="cursor-pointer hover:bg-slate-50 transition-colors"
                                    onClick={() => setSelectedTask(task)}
                                >
                                    <TableCell className="font-medium font-mono text-xs">{task.taskCode}</TableCell>
                                    <TableCell>{task.department}</TableCell>
                                    <TableCell>{task.assetType}</TableCell>
                                    <TableCell>{task.sectionId}</TableCell>
                                    <TableCell>
                                        <StatusChip
                                            status={capitalize(task.criticality)}
                                            showDot={false}
                                            className="text-[10px] px-1.5 py-0"
                                        />
                                    </TableCell>
                                    <TableCell>
                                        <StatusChip
                                            status={capitalize(task.urgency)}
                                            showDot={false}
                                            className="text-[10px] px-1.5 py-0"
                                        />
                                    </TableCell>
                                    <TableCell className="text-muted-foreground text-xs whitespace-nowrap">
                                        {new Date(task.dueDate).toLocaleDateString()}
                                    </TableCell>
                                    <TableCell className="text-xs">{task.durationMinutes}m</TableCell>
                                    <TableCell className="text-right">
                                        <StatusChip status={capitalize(task.status === "in-progress" ? "pending" : task.status)} />
                                    </TableCell>
                                </TableRow>
                            ))}
                        </TableBody>
                    </Table>
                </div>
            </CardContent>

            <Sheet open={!!selectedTask} onOpenChange={(open) => !open && setSelectedTask(null)}>
                <SheetContent className="w-full sm:max-w-md overflow-y-auto">
                    {selectedTask && (
                        <>
                            <SheetHeader className="mb-6">
                                <div className="flex items-center gap-2 mb-1">
                                    <Badge variant="outline">{selectedTask.department}</Badge>
                                    <StatusChip status={capitalize(selectedTask.status === "in-progress" ? "pending" : selectedTask.status)} />
                                </div>
                                <SheetTitle className="text-xl font-mono">{selectedTask.taskCode}</SheetTitle>
                                <SheetDescription>
                                    {selectedTask.assetType} maintenance in section {selectedTask.sectionId}.
                                </SheetDescription>
                            </SheetHeader>

                            <div className="space-y-6">
                                <div className="grid grid-cols-2 gap-4">
                                    <div className="space-y-1">
                                        <span className="text-xs text-muted-foreground uppercase tracking-wider font-semibold">Criticality</span>
                                        <div><StatusChip status={capitalize(selectedTask.criticality)} /></div>
                                    </div>
                                    <div className="space-y-1">
                                        <span className="text-xs text-muted-foreground uppercase tracking-wider font-semibold">Urgency</span>
                                        <div><StatusChip status={capitalize(selectedTask.urgency)} /></div>
                                    </div>
                                    <div className="space-y-1">
                                        <span className="text-xs text-muted-foreground uppercase tracking-wider font-semibold flex items-center gap-1"><CalendarDays className="h-3 w-3" /> Due</span>
                                        <div className="text-sm">{new Date(selectedTask.dueDate).toLocaleDateString()}</div>
                                    </div>
                                    <div className="space-y-1">
                                        <span className="text-xs text-muted-foreground uppercase tracking-wider font-semibold flex items-center gap-1"><Clock className="h-3 w-3" /> Duration</span>
                                        <div className="text-sm">{selectedTask.durationMinutes} mins</div>
                                    </div>
                                </div>

                                {selectedTask.dependencies && selectedTask.dependencies.length > 0 && (
                                    <div className="space-y-2">
                                        <h4 className="text-sm font-semibold">Dependencies</h4>
                                        <div className="flex gap-2 flex-wrap">
                                            {selectedTask.dependencies.map(dep => (
                                                <Badge key={dep} variant="secondary" className="font-mono text-xs cursor-pointer hover:bg-slate-200">
                                                    {dep}
                                                </Badge>
                                            ))}
                                        </div>
                                    </div>
                                )}

                                <div className="bg-slate-50 border rounded-lg p-4 space-y-2">
                                    <h4 className="text-sm font-medium">Source Information</h4>
                                    <div className="text-xs text-slate-500">
                                        System: <span className="font-mono">{selectedTask.sourceSystem}</span><br />
                                        Data Mode: {selectedTask.isSimulation ? "Simulation / Demo" : "Live"}
                                    </div>
                                </div>
                            </div>
                        </>
                    )}
                </SheetContent>
            </Sheet>
        </Card>
    );
}
