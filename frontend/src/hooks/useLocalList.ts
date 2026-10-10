import { useCallback, useState } from "react";

/**
 * Holds a locally mutable copy of a fetched list so demo actions
 * (approve, resolve, reschedule, ...) can update the UI immediately.
 *
 * The incoming list is copied during render (React's supported
 * "adjust state when props change" pattern) instead of in an effect.
 */
export function useLocalList<T>(data: T[] | null | undefined, getId: (item: T) => string) {
    const [items, setItems] = useState<T[]>([]);
    const [source, setSource] = useState<T[] | null | undefined>(undefined);

    if (data !== source) {
        setSource(data);
        if (data) setItems(data);
    }

    const update = useCallback(
        (id: string, patch: Partial<T>) => {
            setItems((previous) =>
                previous.map((item) => (getId(item) === id ? { ...item, ...patch } : item))
            );
        },
        [getId]
    );

    const remove = useCallback(
        (id: string) => {
            setItems((previous) => previous.filter((item) => getId(item) !== id));
        },
        [getId]
    );

    return { items, setItems, update, remove };
}
