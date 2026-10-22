import { describe, expect, it } from "vitest";
import { resolveEventLink } from "./eventLinks";
import { NAV_ITEMS } from "@/config/navigation";
import {
    DEMO_EVENTS,
    DEMO_REPORTS,
    DEMO_TASKS,
    DEMO_TRAINS,
    SECTION_IDS
} from "@/data/demo/mockData";

/**
 * Notification → record link resolution.
 *
 * OperationalEvent carries no explicit entity id, so identifiers are read from
 * the message text and the event's own sectionId. These tests pin that
 * extraction against the real simulation messages: every identifier used to
 * open a record must exist in the demo datasets (nothing is fabricated), and
 * events without a usable identifier must fall back (record === undefined)
 * instead of guessing a link.
 */
describe("resolveEventLink", () => {
    it("maps every demo notification to a real module page", () => {
        const pages = new Set(NAV_ITEMS.map((item) => item.id));
        expect(DEMO_EVENTS.length).toBeGreaterThan(0);
        for (const event of DEMO_EVENTS) {
            expect(pages.has(resolveEventLink(event).page)).toBe(true);
        }
    });

    it("extracts an existing task code for maintenance notifications", () => {
        const codes = new Set(DEMO_TASKS.map((task) => task.taskCode));
        const events = DEMO_EVENTS.filter((event) => event.type === "maintenance_updated");
        expect(events.length).toBeGreaterThan(0);
        for (const event of events) {
            const link = resolveEventLink(event);
            expect(link.page).toBe("maintenance");
            expect(link.record).toBeDefined();
            expect(codes.has(link.record as string)).toBe(true);
        }
    });

    it("extracts an existing train number for train notifications", () => {
        const numbers = new Set(DEMO_TRAINS.map((train) => train.trainNumber));
        const events = DEMO_EVENTS.filter(
            (event) => event.type === "train_delayed" || event.type === "train_scheduled"
        );
        expect(events.length).toBeGreaterThan(0);
        for (const event of events) {
            const link = resolveEventLink(event);
            expect(link.page).toBe("trains");
            expect(link.record).toBeDefined();
            expect(numbers.has(link.record as string)).toBe(true);
        }
    });

    it("extracts an existing report code for report notifications", () => {
        const codes = new Set(DEMO_REPORTS.map((report) => report.reportCode));
        const events = DEMO_EVENTS.filter((event) => event.type === "report_generated");
        expect(events.length).toBeGreaterThan(0);
        for (const event of events) {
            const link = resolveEventLink(event);
            expect(link.page).toBe("reports");
            expect(link.record).toBeDefined();
            expect(codes.has(link.record as string)).toBe(true);
        }
    });

    it("links conflict, request and asset notifications by the event section", () => {
        const expectedPages: Record<string, string> = {
            conflict_detected: "conflicts",
            conflict_resolved: "conflicts",
            request_added: "departments",
            request_reviewed: "departments",
            asset_flagged: "corridor"
        };
        const events = DEMO_EVENTS.filter((event) => event.type in expectedPages);
        expect(events.length).toBeGreaterThan(0);
        for (const event of events) {
            const link = resolveEventLink(event);
            expect(link.page).toBe(expectedPages[event.type]);
            expect(link.record).toBe(event.sectionId);
            expect(link.recordLabel).toBe(`Section ${event.sectionId}`);
            expect(SECTION_IDS).toContain(link.record);
        }
    });

    it("never deep-links block notifications, which have no record-level detail view", () => {
        const events = DEMO_EVENTS.filter(
            (event) => event.type === "block_changed" || event.type === "block_approved"
        );
        expect(events.length).toBeGreaterThan(0);
        for (const event of events) {
            const link = resolveEventLink(event);
            expect(link.page).toBe("planner");
            expect(link.record).toBeUndefined();
        }
    });

    it("falls back instead of guessing when a message carries no identifier", () => {
        const link = resolveEventLink({
            id: "event-manual",
            timestamp: new Date().toISOString(),
            type: "maintenance_updated",
            severity: "info",
            message: "Status refreshed by the planning team.",
            actor: "Planner Service",
            isSimulation: true
        });
        expect(link.page).toBe("maintenance");
        expect(link.record).toBeUndefined();
        expect(link.recordLabel).toBeUndefined();
    });
});
