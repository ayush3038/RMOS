// @vitest-environment jsdom
import { afterEach, beforeAll, beforeEach, describe, expect, it, vi } from "vitest";
import { cleanup, render, screen, waitFor } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import type { ReactElement } from "react";

import { AppRoutes } from "./App";
import { PreferencesProvider } from "@/store/preferences";
import { ToastProvider } from "@/store/toast";
import { TOKEN_KEY, SESSION_KEY } from "@/services/api/session";

/**
 * Routing behaviour tests.
 *
 * These cover the entry-point rule (the landing page is the default and is never
 * auto-redirected to the dashboard), clean URLs, protected routes and the fact
 * that every route survives a direct visit or refresh.
 */

vi.mock("@/services/api", () => ({
    getRepository: () => ({
        getRecentEvents: async () => [],
        getDashboardKpis: async () => ({
            trainsRunning: 0,
            trainsDelayed: 0,
            openConflicts: 0,
            pendingTasks: 0,
            assetsOnline: 0,
            assetsTotal: 0
        }),
        getMaintenanceTasks: async () => [],
        getConflicts: async () => [],
        getTrains: async () => [],
        getDepartmentRequests: async () => [],
        getCorridorSections: async () => [],
        getBlockWindows: async () => [],
        getAssets: async () => [],
        getReports: async () => []
    })
}));

function signIn() {
    window.localStorage.setItem(TOKEN_KEY, "test.jwt.token");
    window.localStorage.setItem(SESSION_KEY, JSON.stringify({ username: "admin", role: "ADMIN" }));
}

function signOut() {
    window.localStorage.removeItem(TOKEN_KEY);
    window.localStorage.removeItem(SESSION_KEY);
}

function renderAt(path: string): void {
    const tree: ReactElement = (
        <PreferencesProvider>
            <ToastProvider>
                <MemoryRouter initialEntries={[path]}>
                    <AppRoutes />
                </MemoryRouter>
            </ToastProvider>
        </PreferencesProvider>
    );
    render(tree);
}

const LANDING_MARKER = "Railway maintenance planning that engineers can actually defend";

// jsdom does not implement the scrolling APIs the dashboard shell calls.
beforeAll(() => {
    Element.prototype.scrollTo = function scrollTo() {
        /* no-op for tests */
    };
    Element.prototype.scrollIntoView = function scrollIntoView() {
        /* no-op for tests */
    };
});

beforeEach(() => {
    window.localStorage.clear();
});

afterEach(() => {
    cleanup();
    vi.restoreAllMocks();
});

describe("1. Landing page is the default entry point", () => {
    it("shows the public landing page at / for a logged-out visitor", () => {
        renderAt("/");
        expect(screen.getByText(LANDING_MARKER)).toBeDefined();
    });

    it("still shows the landing page at / for an authenticated visitor", () => {
        // The root must never bounce straight into the dashboard.
        signIn();
        renderAt("/");
        expect(screen.getByText(LANDING_MARKER)).toBeDefined();
    });

    it("keeps the landing page reachable without authentication", () => {
        renderAt("/");
        expect(document.querySelector("form")).toBeNull();
    });
});

describe("2. Clean URLs", () => {
    it("normalises a trailing slash on /login/ to /login", async () => {
        renderAt("/login/");
        await waitFor(() => {
            expect(screen.getByRole("button", { name: /sign in/i })).toBeDefined();
        });
    });

    it("normalises a trailing slash on /dashboard/ to the dashboard", async () => {
        signIn();
        renderAt("/dashboard/");
        await waitFor(() => {
            expect(screen.queryByText(LANDING_MARKER)).toBeNull();
        });
    });

    it("leaves the root path untouched", () => {
        renderAt("/");
        expect(screen.getByText(LANDING_MARKER)).toBeDefined();
    });
});

describe("3. Login route", () => {
    it("shows the login page at /login for a logged-out visitor", () => {
        renderAt("/login");
        expect(screen.getByLabelText(/^username$/i)).toBeDefined();
        expect(screen.getByLabelText(/^password$/i)).toBeDefined();
    });

    it("pre-fills the documented demo credentials", () => {
        renderAt("/login");
        expect((screen.getByLabelText(/^username$/i) as HTMLInputElement).value).toBe("admin");
        expect((screen.getByLabelText(/^password$/i) as HTMLInputElement).value).toBe("demo123");
    });

    it("sends an already-authenticated visitor to the dashboard", async () => {
        signIn();
        renderAt("/login");
        await waitFor(() => {
            expect(screen.queryByLabelText(/^username$/i)).toBeNull();
        });
    });
});

describe("4. Protected routes", () => {
    it("redirects /dashboard to /login when logged out", async () => {
        renderAt("/dashboard");
        await waitFor(() => {
            expect(screen.getByLabelText(/^username$/i)).toBeDefined();
        });
    });

    it("redirects a module route to /login when logged out", async () => {
        renderAt("/trains");
        await waitFor(() => {
            expect(screen.getByLabelText(/^username$/i)).toBeDefined();
        });
    });

    it("allows an authenticated visitor to open /dashboard", () => {
        signIn();
        renderAt("/dashboard");
        expect(screen.queryByLabelText(/^username$/i)).toBeNull();
        expect(screen.queryByText(LANDING_MARKER)).toBeNull();
    });
});

describe("5. Direct navigation and refresh on module routes", () => {
    it("renders the requested module, not the dashboard, when opened directly", async () => {
        signIn();
        renderAt("/trains");
        await waitFor(() => {
            expect(screen.getAllByText("Train Operations").length).toBeGreaterThan(0);
        });
    });

    it("renders a different module for a different deep link", async () => {
        signIn();
        renderAt("/analytics");
        await waitFor(() => {
            expect(screen.getAllByText("Analytics").length).toBeGreaterThan(0);
        });
    });

    it("sends an unknown module path back to the dashboard instead of an empty page", async () => {
        signIn();
        renderAt("/not-a-real-module");
        await waitFor(() => {
            expect(screen.queryByText("RMOS Module")).toBeNull();
        });
    });
});

describe("6. Unknown URLs", () => {
    it("returns an unknown deep path to the landing page", async () => {
        renderAt("/this/does/not/exist");
        await waitFor(() => {
            expect(screen.getByText(LANDING_MARKER)).toBeDefined();
        });
    });
});

describe("7. Logout behaviour", () => {
    it("clearing the session makes /dashboard redirect back to login", async () => {
        signIn();
        renderAt("/dashboard");
        expect(screen.queryByLabelText(/^username$/i)).toBeNull();

        // Simulate the shared logout handler clearing session state.
        signOut();

        const { unmount } = render(
            <PreferencesProvider>
                <ToastProvider>
                    <MemoryRouter initialEntries={["/dashboard"]}>
                        <AppRoutes />
                    </MemoryRouter>
                </ToastProvider>
            </PreferencesProvider>
        );
        await waitFor(() => {
            expect(screen.getByLabelText(/^username$/i)).toBeDefined();
        });
        unmount();
    });
});
