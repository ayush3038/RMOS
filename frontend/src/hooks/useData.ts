import { useEffect, useRef, useState } from "react";

export interface DataState<T> {
    data: T | null;
    isLoading: boolean;
    error: Error | null;
}

export function useData<T>(fetcher: () => Promise<T>): DataState<T> {
    const fetcherRef = useRef(fetcher);

    // Keep the latest fetcher available without restarting the data-loading effect.
    useEffect(() => {
        fetcherRef.current = fetcher;
    }, [fetcher]);

    const [state, setState] = useState<DataState<T>>({
        data: null,
        isLoading: true,
        error: null,
    });

    useEffect(() => {
        let isMounted = true;

        fetcherRef.current()
            .then((data) => {
                if (isMounted) {
                    setState({
                        data,
                        isLoading: false,
                        error: null,
                    });
                }
            })
            .catch((error) => {
                if (isMounted) {
                    setState({
                        data: null,
                        isLoading: false,
                        error: error instanceof Error
                            ? error
                            : new Error("Failed to load data"),
                    });
                }
            });

        return () => {
            isMounted = false;
        };
    }, []);

    return state;
}