import type { RealtimeEvent, EventType } from "@/types/events";

/**
 * Realtime Client Boundary
 * Prepares the application to receive Server-Sent Events or WebSockets.
 */

export interface RealtimeSubscription {
    unsubscribe: () => void;
}

export interface RealtimeClient {
    connect(): void;
    disconnect(): void;
    subscribe<T = unknown>(eventType: EventType, callback: (event: RealtimeEvent<T>) => void): RealtimeSubscription;
}

export class DummyRealtimeClient implements RealtimeClient {
    connect() {
        console.log("[Realtime] Connected to simulation mode.");
    }

    disconnect() {
        console.log("[Realtime] Disconnected from simulation mode.");
    }

    subscribe<T = unknown>(_eventType: EventType, _callback: (event: RealtimeEvent<T>) => void): RealtimeSubscription {
        // Implementation for future mapping of events
        return {
            unsubscribe: () => { }
        };
    }
}

export const realtimeClient = new DummyRealtimeClient();
