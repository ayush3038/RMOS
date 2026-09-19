/**
 * API Client Foundation
 * Configured to connect to future REST API
 */

class ApiClient {
    private baseUrl: string;

    constructor() {
        this.baseUrl = import.meta.env.VITE_API_BASE_URL || "/api";
    }

    private getHeaders(customHeaders?: HeadersInit): HeadersInit {
        const headers: Record<string, string> = {
            "Content-Type": "application/json",
        };
        const token = localStorage.getItem("rmos_jwt");
        if (token) {
            headers["Authorization"] = `Bearer ${token}`;
        }
        return { ...headers, ...(customHeaders as Record<string, string>) };
    }

    async get<T>(endpoint: string, options?: RequestInit): Promise<T> {
        return this.request<T>(endpoint, { ...options, method: "GET" });
    }

    async post<T>(endpoint: string, data?: any, options?: RequestInit): Promise<T> {
        return this.request<T>(endpoint, {
            ...options,
            method: "POST",
            body: data ? JSON.stringify(data) : undefined,
        });
    }

    private async request<T>(endpoint: string, options: RequestInit): Promise<T> {
        try {
            const controller = new AbortController();
            const timeoutId = setTimeout(() => controller.abort(), 10000); // 10s default timeout

            const response = await fetch(`${this.baseUrl}${endpoint}`, {
                ...options,
                headers: this.getHeaders(options.headers),
                signal: options.signal || controller.signal,
            });

            clearTimeout(timeoutId);

            if (!response.ok) {
                if (response.status === 401 || response.status === 403) {
                    // Quick auth handling (could emit event to redirect)
                    console.warn("Authentication failed, routing to login");
                    localStorage.removeItem("rmos_jwt");
                    window.dispatchEvent(new Event("auth:unauthorized"));
                }
                throw new Error(`API Error: ${response.status} - ${response.statusText}`);
            }

            const isJson = response.headers.get("content-type")?.includes("application/json");
            return isJson ? await response.json() as T : (await response.text() as unknown as T);
        } catch (error) {
            console.error("API Request Failed:", error);
            throw error;
        }
    }
}

export const apiClient = new ApiClient();
