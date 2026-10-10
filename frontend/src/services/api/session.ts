/**
 * RMOS client session store.
 *
 * Single source of truth for the authenticated session so logout behaves
 * identically wherever it is triggered (top-right profile menu and the
 * bottom-left sidebar user section).
 *
 * Only session material is stored here. Workspace preferences live in their own
 * `rmos.preferences.v1` key and are deliberately left untouched by logout.
 */

export const TOKEN_KEY = "rmos_jwt";
export const SESSION_KEY = "rmos.session";

export interface SessionUser {
    username: string;
    role: string;
}

function readJson<T>(key: string): T | null {
    if (typeof window === "undefined") return null;
    try {
        const raw = window.localStorage.getItem(key);
        return raw ? (JSON.parse(raw) as T) : null;
    } catch {
        return null;
    }
}

function writeJson(key: string, value: unknown): void {
    if (typeof window === "undefined") return;
    try {
        window.localStorage.setItem(key, JSON.stringify(value));
    } catch {
        /* storage is best effort */
    }
}

function remove(key: string): void {
    if (typeof window === "undefined") return;
    try {
        window.localStorage.removeItem(key);
    } catch {
        /* storage is best effort */
    }
}

/** JWT of the current session, or an empty string when signed out. */
export function getToken(): string {
    if (typeof window === "undefined") return "";
    return window.localStorage.getItem(TOKEN_KEY) ?? "";
}

export function setToken(token: string): void {
    if (typeof window === "undefined") return;
    window.localStorage.setItem(TOKEN_KEY, token);
}

export function hasSessionToken(): boolean {
    return Boolean(getToken());
}

/** Persisted identity of the signed-in user. Null when signed out. */
export function getSessionUser(): SessionUser | null {
    const stored = readJson<SessionUser>(SESSION_KEY);
    if (!stored || typeof stored.username !== "string") return null;
    return { username: stored.username, role: stored.role ?? "" };
}

export function setSessionUser(user: SessionUser): void {
    writeJson(SESSION_KEY, { username: user.username, role: user.role ?? "" });
}

/**
 * Clears every piece of authentication state: the JWT, the persisted identity
 * and any in-memory session entry.
 *
 * Workspace preferences (`rmos.preferences.v1`) and application data are NOT
 * removed - signing out must not wipe the user's workspace configuration.
 */
export function clearSession(): void {
    remove(TOKEN_KEY);
    remove(SESSION_KEY);
}

/** Fired after a local sign-out so the router can leave protected routes. */
export const SIGNED_OUT_EVENT = "rmos:signed-out";

export function emitSignedOut(): void {
    if (typeof window === "undefined") return;
    window.dispatchEvent(new Event(SIGNED_OUT_EVENT));
}

/**
 * The one shared logout action. Both the top-right profile menu and the
 * bottom-left sidebar user section call this, so behaviour can never diverge.
 */
export function signOut(): void {
    clearSession();
    emitSignedOut();
}