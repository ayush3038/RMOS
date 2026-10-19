import { useEffect, useState } from "react";
import {
    BrowserRouter,
    Routes,
    Route,
    Navigate,
    Outlet,
    useOutletContext,
    useNavigate,
    useLocation,
    useParams
} from "react-router-dom";
import { AppShell } from "@/components/rmos/layout/AppShell";
import { Dashboard } from "@/components/rmos/dashboard/Dashboard";
import { ModulePage } from "@/components/rmos/ModulePage";
import { AssistantPage } from "@/components/rmos/assistant/AssistantPage";
import { LandingPage } from "@/components/rmos/auth/LandingPage";
import { LoginPage } from "@/components/rmos/auth/LoginPage";
import { PreferencesProvider } from "@/store/preferences";
import { ToastProvider } from "@/store/toast";
import { NAV_ITEMS } from "@/config/navigation";
import { hasSessionToken } from "@/services/api/chat";
import { SIGNED_OUT_EVENT } from "@/services/api/session";

const PAGE_TITLES: Record<string, { title: string; description: string }> = {
    planner: {
        title: "Block Planner",
        description: "Candidate maintenance tasks, proposed possession windows and block approvals.",
    },
    maintenance: {
        title: "Maintenance",
        description: "Work queue, work orders and execution state across departments.",
    },
    trains: {
        title: "Train Operations",
        description: "Live synthetic service board, delays and path conflicts.",
    },
    corridor: {
        title: "Corridor Availability",
        description: "Availability, capacity and inspection state across corridors.",
    },
    departments: {
        title: "Departments",
        description: "Cross-department block requests with approval and modification actions.",
    },
    weekly: {
        title: "Weekly Plan",
        description: "Seven-day planning board with tasks, blocks and daily load.",
    },
    monthly: {
        title: "Monthly Plan",
        description: "Month heatmap of maintenance load, criticality and block windows.",
    },
    conflicts: {
        title: "Conflicts",
        description: "Detected block, train, resource and safety clashes with resolution actions.",
    },
    analytics: {
        title: "Analytics",
        description: "Operational KPIs, trends and distribution of the planning run.",
    },
    reports: {
        title: "Reports",
        description: "Generated operational reports with run and download actions.",
    },
    assistant: {
        title: "AI Assistant",
        description: "RMOS AI assistant for maintenance plans, block planning and priority questions.",
    },
};

const VALID_PAGES = new Set(["dashboard", ...NAV_ITEMS.map((item) => item.id)]);

/**
 * Auth state, read once per route mount.
 *
 * RMOS is a client-only SPA, so the stored JWT is available on the very first
 * render. Reading it eagerly (instead of after an effect) means the first paint
 * is already the correct route - no splash screen, and no chance of briefly
 * showing the login form to an authenticated user or vice versa.
 */
function useIsAuthenticated(): boolean {
    return useState(() => hasSessionToken())[0];
}

/** Protected route wrapper - redirects to /login if no valid session. */
function ProtectedRoute({ children }: { children: React.ReactNode }) {
    const isAuthenticated = useIsAuthenticated();
    return isAuthenticated ? <>{children}</> : <Navigate to="/login" replace />;
}

/**
 * Public route wrapper for /login only - sends an already-authenticated visitor
 * straight to the workspace instead of showing a redundant sign-in form.
 *
 * It is deliberately NOT used for the landing page: `/` is the default entry
 * point and must always render the public landing page, never bounce straight
 * into the dashboard.
 */
function PublicRoute({ children }: { children: React.ReactNode }) {
    const isAuthenticated = useIsAuthenticated();
    return isAuthenticated ? <Navigate to="/dashboard" replace /> : <>{children}</>;
}

/**
 * Normalises trailing slashes so `/login/` and `/login` are not duplicate
 * variants of the same route. The root path "/" is left untouched.
 */
function TrailingSlashRedirect() {
    const location = useLocation();
    const { pathname } = location;

    if (pathname !== "/" && pathname.length > 1 && pathname.endsWith("/")) {
        const normalized = pathname.replace(/\/+$/, "");
        return <Navigate to={`${normalized}${location.search}${location.hash}`} replace />;
    }
    return null;
}

/** Main authenticated layout with sidebar and topbar */
function AuthenticatedLayout() {
    const navigate = useNavigate();
    // The rendered module is derived from the URL, so a direct visit or a
    // browser refresh on any module route shows that module - not the dashboard.
    const { page } = useParams<{ page?: string }>();
    const activePage = page ?? "dashboard";

    // Any explicit logout - from the profile menu, the sidebar user menu or a
    // 401/403 raised by the API client - leaves the protected area immediately.
    useEffect(() => {
        const leave = () => navigate("/login", { replace: true });
        window.addEventListener(SIGNED_OUT_EVENT, leave);
        window.addEventListener("auth:unauthorized", leave);
        return () => {
            window.removeEventListener(SIGNED_OUT_EVENT, leave);
            window.removeEventListener("auth:unauthorized", leave);
        };
    }, [navigate]);

    // Navigation goes through the URL so every module is linkable and
    // refreshable, instead of living only in component state. The optional
    // `record` deep-links a specific record inside the module (e.g. a
    // notification about work order TRC-103 opens /maintenance?record=TRC-103).
    const navigatePage = (page: string, record?: string) => {
        const target = VALID_PAGES.has(page) ? page : "dashboard";
        const base = target === "dashboard" ? "/dashboard" : `/${target}`;
        navigate(record && target !== "dashboard" ? `${base}?record=${encodeURIComponent(record)}` : base);
    };

    return (
        <AppShell activePage={activePage} onNavigate={navigatePage}>
            <Outlet context={{ activePage, onNavigate: navigatePage }} />
        </AppShell>
    );
}

/** Page renderer for authenticated routes */
function PageRenderer() {
    const { activePage, onNavigate } = useOutletContext<{
        activePage: string;
        onNavigate: (page: string, record?: string) => void;
    }>();

    // An unknown module id (e.g. a stale bookmark) goes back to the dashboard
    // rather than rendering an empty module page.
    if (!VALID_PAGES.has(activePage)) {
        return <Navigate to="/dashboard" replace />;
    }

    if (activePage === "dashboard") {
        return <Dashboard onNavigate={onNavigate} />;
    }
    if (activePage === "assistant") {
        return <AssistantPage />;
    }
    return (
        <ModulePage
            page={activePage}
            title={PAGE_TITLES[activePage]?.title ?? "RMOS Module"}
            description={PAGE_TITLES[activePage]?.description ?? "RMOS operational module."}
        />
    );
}

/** Every route table, exported so the routing behaviour can be tested directly. */
export function AppRoutes() {
    return (
        <>
            <TrailingSlashRedirect />
            <Routes>
                {/*
                    Public routes, declared BEFORE the authenticated catch-all so
                    they can never be captured by it.

                    `/` is the default entry point and always renders the public
                    landing page, logged in or not.
                */}
                <Route path="/login" element={<PublicRoute><LoginPage /></PublicRoute>} />
                <Route path="/" element={<LandingPage />} />

                {/* Protected routes */}
                <Route element={<ProtectedRoute><AuthenticatedLayout /></ProtectedRoute>}>
                    <Route path="/dashboard" element={<PageRenderer />} />
                    <Route path="/:page" element={<PageRenderer />} />
                </Route>

                {/* Anything else returns to the landing page rather than 404ing. */}
                <Route path="*" element={<Navigate to="/" replace />} />
            </Routes>
        </>
    );
}

function App() {
    return (
        <PreferencesProvider>
            <ToastProvider>
                <BrowserRouter>
                    <AppRoutes />
                </BrowserRouter>
            </ToastProvider>
        </PreferencesProvider>
    );
}

export default App;