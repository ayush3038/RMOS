import { useState, useEffect } from "react";

export interface DataState<T> {
    data: T | null;
    isLoading: boolean;
    error: Error | null;
}

export function useData<T>(fetcher: () => Promise<T>): DataState<T> {
    const [state, setState] = useState<DataState<T>>({
        data: null,
        isLoading: true,
        error: null,
    });

    useEffect(() => {
        let isMounted = true;
        setState((prev) => ({ ...prev, isLoading: true, error: null }));

        fetcher()
            .then((data) => {
                if (isMounted) setState({ data, isLoading: false, error: null });
            })
            .catch((error) => {
                if (isMounted) setState({ data: null, isLoading: false, error });
            });

        return () => {
            isMounted = false;
        };
    }, [fetcher]);

    return state;
}
