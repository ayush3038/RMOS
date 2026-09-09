export type EventType = "maintenance_updated" | "train_movement_updated" | "corridor_status_changed" | "block_changed" | "conflict_created" | "conflict_resolved" | "operational_activity_added";

export interface RealtimeEvent<T = unknown> {
    eventId: string;
    eventType: EventType;
    timestamp: string;
    sourceSystem: string;
    sourceRecordId?: string;
    entityType?: string;
    entityId?: string;
    payload?: T;
    version?: number;
    isSimulation?: boolean;
}
