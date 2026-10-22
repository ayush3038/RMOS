import type { OperationalEvent } from "@/types/models";

/**
 * Resolves an operational event (notification) to the module it belongs to and,
 * when the event references a specific record, the natural key of that record.
 *
 * The `OperationalEvent` model carries no explicit entity id, so identifiers are
 * extracted from the message text (which the simulation data builds from real
 * record codes such as task codes, train numbers or report codes) and from the
 * event's own `sectionId` field. Nothing is fabricated: if no identifier can be
 * read from the event, `record` stays undefined and callers fall back to a
 * notification-detail view instead of navigating blindly.
 */
export interface EventLink {
    /** Module page id (matches the sidebar / route ids). */
    page: string;
    /** Human label of the module, used in the notification-detail fallback. */
    pageLabel: string;
    /** Natural key of the linked record, when a module detail view can open it. */
    record?: string;
    /** Human label for the linked record ("Task TRC-103", "Section A12"). */
    recordLabel?: string;
    /** Identifier kept for display even when the module has no record-level detail view. */
    reference?: string;
}

const PAGE_BY_TYPE: Record<OperationalEvent["type"], { page: string; pageLabel: string }> = {
    conflict_detected: { page: "conflicts", pageLabel: "Conflicts" },
    conflict_resolved: { page: "conflicts", pageLabel: "Conflicts" },
    request_added: { page: "departments", pageLabel: "Departments" },
    request_reviewed: { page: "departments", pageLabel: "Departments" },
    maintenance_updated: { page: "maintenance", pageLabel: "Maintenance" },
    block_changed: { page: "planner", pageLabel: "Block Planner" },
    block_approved: { page: "planner", pageLabel: "Block Planner" },
    train_delayed: { page: "trains", pageLabel: "Train Operations" },
    train_scheduled: { page: "trains", pageLabel: "Train Operations" },
    asset_flagged: { page: "corridor", pageLabel: "Corridor Availability" },
    report_generated: { page: "reports", pageLabel: "Reports" }
};

export const EVENT_TYPE_LABELS: Record<OperationalEvent["type"], string> = {
    conflict_detected: "Conflict detected",
    conflict_resolved: "Conflict resolved",
    request_added: "Department request added",
    request_reviewed: "Department request reviewed",
    maintenance_updated: "Maintenance updated",
    block_changed: "Block window changed",
    block_approved: "Block window approved",
    train_delayed: "Train delayed",
    train_scheduled: "Train scheduled",
    asset_flagged: "Asset flagged",
    report_generated: "Report generated"
};

/** "TRC-103", "ENG-105" … task / work-order style codes in maintenance messages. */
const TASK_CODE_PATTERN = /\b([A-Z]{2,4}-\d{2,4})\b/;
/** Train numbers appear as "Train 12037 …" or "… for 12037 …" in train messages. */
const TRAIN_NUMBER_PATTERN = /\b(?:Train\s+|for\s+)(\d{4,6})\b/;
/** "Report RPT-202603" … report codes (the feed also uses "report RPT-…"). */
const REPORT_CODE_PATTERN = /\bReport\s+([A-Z]{2,6}-\d{2,6})\b/i;
/** "Block BLK-202603" … block codes in block-approval messages. */
const BLOCK_CODE_PATTERN = /\bBlock\s+([A-Z]{2,6}-\d{2,6})\b/;
/** "Section A12" … section ids referenced in some messages. */
const SECTION_PATTERN = /\bSection\s+([A-Z]{1,2}\d{1,3})\b/i;

/** Section id of the event, falling back to the one mentioned in the message. */
function sectionReference(event: OperationalEvent): string | undefined {
    return event.sectionId ?? event.message.match(SECTION_PATTERN)?.[1];
}

export function resolveEventLink(event: OperationalEvent): EventLink {
    const base = PAGE_BY_TYPE[event.type];

    switch (event.type) {
        // The maintenance queue and its work-order detail sheet key on taskCode / workOrder.
        case "maintenance_updated": {
            const code = event.message.match(TASK_CODE_PATTERN)?.[1];
            return code
                ? { ...base, record: code, recordLabel: `Task / work order ${code}` }
                : { ...base };
        }
        case "train_delayed":
        case "train_scheduled": {
            const number = event.message.match(TRAIN_NUMBER_PATTERN)?.[1];
            return number
                ? { ...base, record: number, recordLabel: `Train ${number}` }
                : { ...base };
        }
        case "report_generated": {
            const code = event.message.match(REPORT_CODE_PATTERN)?.[1];
            return code
                ? { ...base, record: code, recordLabel: `Report ${code}` }
                : { ...base };
        }
        // Conflict, request and asset events identify their record by section.
        case "conflict_detected":
        case "conflict_resolved":
        case "request_added":
        case "request_reviewed":
        case "asset_flagged": {
            const sectionId = sectionReference(event);
            return sectionId
                ? { ...base, record: sectionId, recordLabel: `Section ${sectionId}` }
                : { ...base };
        }
        // The Block Planner shows block windows as cards without a record-level
        // detail sheet, so block events keep their identifier for display only.
        case "block_changed": {
            const sectionId = sectionReference(event);
            return sectionId ? { ...base, reference: `Section ${sectionId}` } : { ...base };
        }
        case "block_approved": {
            const code = event.message.match(BLOCK_CODE_PATTERN)?.[1];
            return code ? { ...base, reference: `Block ${code}` } : { ...base };
        }
    }
}
