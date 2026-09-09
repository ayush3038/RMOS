/**
 * API Client Foundation
 * Configured to connect to future REST API
 */

export class ApiClient {
    private baseUrl: string;

    constructor() {
        this.baseUrl = import.meta.env.VITE_API_BASE_URL || "/api";
    }

    async get<T>(endpoint: string, options?: RequestInit): Promise<T> {
        try {
            const controller = new AbortController();
            const timeoutId = setTimeout(() => controller.abort(), 10000); // 10s default timeout

            const response = await fetch(`${this.baseUrl}${endpoint}`, {
                ...options,
                method: "GET",
                headers: {
                    ...options?.headers,
                    "Content-Type": "application/json"
                },
                signal: options?.signal || controller.signal,
            });

            clearTimeout(timeoutId);

            if (!response.ok) {
                throw new Error(`API Error: ${response.status} - ${response.statusText}`);
            }

            return await response.json() as T;
        } catch (error) {
            console.error("API Get Request Failed:", error);
            throw error;
        }
    }
}

export const apiClient = new ApiClient();
