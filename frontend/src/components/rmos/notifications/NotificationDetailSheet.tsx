import { AlertTriangle, ExternalLink } from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import {
    Sheet,
    SheetContent,
    SheetDescription,
    SheetHeader,
    SheetTitle
} from "@/components/ui/sheet";
import { DetailItem } from "@/components/rmos/Detail";
import { StatusChip } from "@/components/rmos/StatusChip";
import { formatDateTime, relativeTime } from "@/lib/format";
import { eventSeverityLabel } from "@/lib/status";
import { EVENT_TYPE_LABELS, type EventLink } from "@/lib/eventLinks";
import type { OperationalEvent } from "@/types/models";

interface NotificationDetailSheetProps {
    /** Notification to describe, or null when the sheet is closed. */
    event: OperationalEvent | null;
    /** Resolved link for the event (module, optional linked record). */
    link: EventLink | null;
    onOpenChange: (open: boolean) => void;
    /** Explicit "open the module" action from the fallback view. */
    onOpenModule: (link: EventLink) => void;
}

/**
 * Fallback detail view for notifications that cannot be linked to a specific
 * record (no usable identifier in the message, or the module has no
 * record-level detail view). Shows the full notification instead of silently
 * redirecting to the generic module page.
 */
export function NotificationDetailSheet({
    event,
    link,
    onOpenChange,
    onOpenModule
}: NotificationDetailSheetProps) {
    return (
        <Sheet open={!!event} onOpenChange={onOpenChange}>
            <SheetContent className="w-full sm:max-w-md overflow-y-auto">
                {event && (
                    <>
                        <SheetHeader className="mb-1">
                            <div className="flex items-center gap-2 flex-wrap">
                                <StatusChip
                                    status={eventSeverityLabel(event.severity)}
                                    showDot={false}
                                    className="text-[10.5px]"
                                />
                                <Badge variant="outline" className="text-[10.5px]">
                                    {EVENT_TYPE_LABELS[event.type]}
                                </Badge>
                                {event.isSimulation && (
                                    <Badge variant="secondary" className="text-[10.5px]">
                                        Simulation
                                    </Badge>
                                )}
                            </div>
                            <SheetTitle className="text-lg">Notification details</SheetTitle>
                            <SheetDescription className="text-[13px] text-ink leading-relaxed">
                                {event.message}
                            </SheetDescription>
                        </SheetHeader>

                        <div className="px-4 pb-4 space-y-4">
                            <div className="grid grid-cols-2 gap-3">
                                <DetailItem label="Received" value={formatDateTime(event.timestamp)} />
                                <DetailItem label="When" value={relativeTime(event.timestamp)} />
                                <DetailItem label="Raised by" value={event.actor} />
                                <DetailItem label="Module" value={link?.pageLabel ?? "—"} />
                                {(link?.reference ?? event.sectionId) && (
                                    <DetailItem
                                        label="Linked entity"
                                        value={link?.reference ?? `Section ${event.sectionId}`}
                                    />
                                )}
                            </div>

                            <div className="border rounded-md px-2.5 py-2 bg-paper flex items-start gap-2">
                                <AlertTriangle
                                    className="w-3.5 h-3.5 text-amber shrink-0 mt-0.5"
                                    aria-hidden="true"
                                />
                                <p className="text-[12px] text-muted leading-relaxed">
                                    This notification does not link to a specific
                                    record. Open the module to review it in context.
                                </p>
                            </div>

                            {link && (
                                <Button
                                    size="sm"
                                    className="w-full"
                                    onClick={() => onOpenModule(link)}
                                >
                                    <ExternalLink className="h-4 w-4" />
                                    Open {link.pageLabel}
                                </Button>
                            )}
                        </div>
                    </>
                )}
            </SheetContent>
        </Sheet>
    );
}
