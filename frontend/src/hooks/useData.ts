import { useCallback, useEffect, useRef, useState } from "react";

export interface DataState<T> {
    data: T | null;
    isLoading: boolean;
    error: Error | null;
    /** Re-runs the fetcher and flips the state back to loading. */
    refetch: () => void;
}

/** Window event used by the workspace auto-refresh setting. */
export const AUTO_REFRESH_EVENT = "rmos:auto-refresh";

export function useData<T>(fetcher: () => Promise<T>): DataState<T> {
    const fetcherRef = useRef(fetcher);

    // Keep the latest fetcher available without restarting the data-loading effect.
    useEffect(() => {
        fetcherRef.current = fetcher;
    }, [fetcher]);

    const [version, setVersion] = useState(0);
    const backgroundRef = useRef(false);

    const refetch = useCallback(() => setVersion((value) => value + 1), []);

    const backgroundRefetch = useCallback(() => {
        backgroundRef.current = true;
        setVersion((value) => value + 1);
    }, []);

    const [state, setState] = useState<DataState<T>>({
        data: null,
        isLoading: true,
        error: null,
        refetch
    });

    useEffect(() => {
        let isMounted = true;
        const isBackground = backgroundRef.current;
        backgroundRef.current = false;

        if (!isBackground) {
            setState((previous) =>
                previous.isLoading && previous.data === null
                    ? previous
                    : { ...previous, isLoading: true, error: null }
            );
        }

        const onAutoRefresh = () => backgroundRefetch();

        window.addEventListener(AUTO_REFRESH_EVENT, onAutoRefresh);

        fetcherRef.current()
            .then((data) => {
                if (isMounted) {
                    setState({ data, isLoading: false, error: null, refetch });
                }
            })
            .catch((error) => {
                if (isMounted) {
                    setState({
                        data: null,
                        isLoading: false,
                        error:
                            error instanceof Error
                                ? error
                                : new Error("Failed to load data"),
                        refetch
                    });
                }
            });

        return () => {
            isMounted = false;
            window.removeEventListener(AUTO_REFRESH_EVENT, onAutoRefresh);
        };
    }, [version, refetch, backgroundRefetch]);

    return state;
}
