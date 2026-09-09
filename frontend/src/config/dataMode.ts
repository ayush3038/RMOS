/**
 * Data Source Mode Configuration
 * SIMULATION: Always returns static mock data from frontend demo datasets
 * INTEGRATED: Connects to actual REST APIs and Realtime backends
 */
export type DataSourceMode = "SIMULATION" | "INTEGRATED";

export const DATA_MODE: DataSourceMode = (import.meta.env.VITE_DATA_MODE as DataSourceMode) || "SIMULATION";
