/**
 * RMOS AI assistant chat API.
 *
 * All requests go through the authenticated API client; provider credentials
 * never reach the browser (the backend holds them server-side only).
 */
import { apiClient } from "./client";
import { hasSessionToken, setSessionUser, setToken } from "./session";

export type ChatRole = "user" | "assistant";

export interface ChatTurn {
    role: ChatRole;
    content: string;
}

export interface ChatSource {
    sourceReference?: string;
    documentType?: string;
    similarity?: number;
}

export interface ChatReply {
    reply: string;
    provider: string;
    model: string;
    retrievedContextCount: number;
    sources: ChatSource[];
    systemDataIncluded: boolean;
    generatedAt: string;
    requiresHumanReview: boolean;
    disclaimer: string;
}

export interface ChatProviderStatus {
    type: string;
    configured: boolean;
    description: string;
}

export interface ChatStatus {
    available: boolean;
    provider?: string;
    model?: string;
    message: string;
    providers: ChatProviderStatus[];
    setupInstructions: string[];
    retrievalConfigured: boolean;
}

export interface LoginResponse {
    accessToken: string;
    expiresIn: number;
    role: string;
}

const MAX_MESSAGE_CHARS = 4000;

export function getChatStatus(signal?: AbortSignal): Promise<ChatStatus> {
    return apiClient.get<ChatStatus>("/chat/status", { signal, timeoutMs: 15000 });
}

export function sendChatMessage(message: string, history: ChatTurn[]): Promise<ChatReply> {
    return apiClient.post<ChatReply>(
        "/chat/messages",
        { message: message.slice(0, MAX_MESSAGE_CHARS), history },
        { timeoutMs: 60000 }
    );
}

export function signIn(username: string, password: string): Promise<LoginResponse> {
    return apiClient.post<LoginResponse>("/auth/login", { username, password }).then((response) => {
        if (response?.accessToken) {
            setToken(response.accessToken);
            // Persist the identity so the profile menus can show name and role.
            setSessionUser({ username: username.trim(), role: response.role ?? "" });
        }
        return response;
    });
}

export { hasSessionToken };
