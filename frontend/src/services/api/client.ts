/**
 * API Client Foundation
 * Configured to connect to the RMOS Spring Boot backend.
 */

/** Structured error carrying the backend's {status, code, message} payload. */
export class ApiError extends Error {
    readonly status: number;
    readonly code?: string;

    constructor(status: number, message: string, code?: string) {
        super(message);
        this.name = "ApiError";
        this.status = status;
        this.code = code;
    }
}

interface ApiRequestOptions extends RequestInit {
    /** Per-request timeout in milliseconds (default 10s, chat uses 60s). */
    timeoutMs?: number;
}

class ApiClient {
    private baseUrl: string;

    constructor() {
        this.baseUrl = (import.meta.env.VITE_API_BASE_URL || "/api/v1").replace(/\/+$/, "");
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

    async get<T>(endpoint: string, options?: ApiRequestOptions): Promise<T> {
        return this.request<T>(endpoint, { ...options, method: "GET" });
    }

    async post<T>(endpoint: string, data?: any, options?: ApiRequestOptions): Promise<T> {
        return this.request<T>(endpoint, {
            ...options,
            method: "POST",
            body: data ? JSON.stringify(data) : undefined,
        });
    }

    private async request<T>(endpoint: string, options: ApiRequestOptions): Promise<T> {
        const { timeoutMs = 10000, ...init } = options;
        try {
            const controller = new AbortController();
            const timeoutId = setTimeout(() => controller.abort(), timeoutMs);

            const response = await fetch(`${this.baseUrl}${endpoint}`, {
                ...init,
                headers: this.getHeaders(init.headers),
                signal: init.signal || controller.signal,
            });

            clearTimeout(timeoutId);

            if (!response.ok) {
                let body: any = null;
                try {
                    body = await response.json();
                } catch {
                    /* non-JSON error body */
                }

                if (response.status === 401 || response.status === 403) {
                    // Quick auth handling (could emit event to redirect)
                    console.warn("Authentication failed, routing to login");
                    localStorage.removeItem("rmos_jwt");
                    window.dispatchEvent(new Event("auth:unauthorized"));
                }

                const message =
                    body && (body.message || body.error)
                        ? String(body.message || body.error)
                        : `${response.status} ${response.statusText}`;
                throw new ApiError(response.status, message, body?.code);
            }

            const isJson = response.headers.get("content-type")?.includes("application/json");
            return isJson ? await response.json() as T : (await response.text() as unknown as T);
        } catch (error) {
            if (error instanceof ApiError) {
                throw error;
            }
            if (error instanceof DOMException && error.name === "AbortError") {
                throw new ApiError(0, "The request timed out. Please try again.");
            }
            console.error("API Request Failed:", error);
            throw error;
        }
    }
}

export const apiClient = new ApiClient();
