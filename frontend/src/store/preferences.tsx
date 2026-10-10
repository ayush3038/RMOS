import {
    createContext,
    useCallback,
    useContext,
    useEffect,
    useMemo,
    useState,
    type ReactNode
} from "react";

export type Density = "compact" | "comfortable";

interface PreferencesValue {
    density: Density;
    setDensity: (value: Density) => void;
    showDemoBanner: boolean;
    toggleDemoBanner: () => void;
    rowsPerPage: number;
    setRowsPerPage: (value: number) => void;
    autoRefreshSeconds: number;
    setAutoRefreshSeconds: (value: number) => void;
}

const STORAGE_KEY = "rmos.preferences.v1";

interface StoredPreferences {
    density: Density;
    showDemoBanner: boolean;
    rowsPerPage: number;
    autoRefreshSeconds: number;
}

const DEFAULTS: StoredPreferences = {
    density: "compact",
    showDemoBanner: true,
    rowsPerPage: 10,
    autoRefreshSeconds: 0
};

function readStored(): StoredPreferences {
    if (typeof window === "undefined") return DEFAULTS;
    try {
        const raw = window.localStorage.getItem(STORAGE_KEY);
        if (!raw) return DEFAULTS;
        return { ...DEFAULTS, ...(JSON.parse(raw) as Partial<StoredPreferences>) };
    } catch {
        return DEFAULTS;
    }
}

const PreferencesContext = createContext<PreferencesValue | null>(null);

export function PreferencesProvider({ children }: { children: ReactNode }) {
    const [preferences, setPreferences] = useState<StoredPreferences>(readStored);

    useEffect(() => {
        try {
            window.localStorage.setItem(STORAGE_KEY, JSON.stringify(preferences));
        } catch {
            /* preferences are best effort */
        }
    }, [preferences]);

    const setDensity = useCallback(
        (density: Density) => setPreferences((prev) => ({ ...prev, density })),
        []
    );
    const toggleDemoBanner = useCallback(
        () => setPreferences((prev) => ({ ...prev, showDemoBanner: !prev.showDemoBanner })),
        []
    );
    const setRowsPerPage = useCallback(
        (rowsPerPage: number) => setPreferences((prev) => ({ ...prev, rowsPerPage })),
        []
    );
    const setAutoRefreshSeconds = useCallback(
        (autoRefreshSeconds: number) => setPreferences((prev) => ({ ...prev, autoRefreshSeconds })),
        []
    );

    const value = useMemo<PreferencesValue>(
        () => ({
            density: preferences.density,
            setDensity,
            showDemoBanner: preferences.showDemoBanner,
            toggleDemoBanner,
            rowsPerPage: preferences.rowsPerPage,
            setRowsPerPage,
            autoRefreshSeconds: preferences.autoRefreshSeconds,
            setAutoRefreshSeconds
        }),
        [
            preferences.density,
            preferences.showDemoBanner,
            preferences.rowsPerPage,
            preferences.autoRefreshSeconds,
            setDensity,
            toggleDemoBanner,
            setRowsPerPage,
            setAutoRefreshSeconds
        ]
    );

    return <PreferencesContext.Provider value={value}>{children}</PreferencesContext.Provider>;
}

export function usePreferences(): PreferencesValue {
    const context = useContext(PreferencesContext);
    if (!context) {
        throw new Error("usePreferences must be used within PreferencesProvider");
    }
    return context;
}
