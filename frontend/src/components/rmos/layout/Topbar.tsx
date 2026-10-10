import { useEffect, useMemo, useRef, useState } from "react";
import {
    Bell,
    CircleHelp,
    FileText,
    LayoutDashboard,
    LogOut,
    Menu,
    Search,
    Settings,
    Siren,
    Check
} from "lucide-react";
import { SimulatorControls } from "./SimulatorControls";
import { NAV_ITEMS } from "@/config/navigation";
import { useData } from "@/hooks/useData";
import { useLogout } from "@/hooks/useLogout";
import { getRepository } from "@/services/api";
import { getSessionUser } from "@/services/api/session";
import { relativeTime } from "@/lib/format";
import { cn } from "@/lib/utils";
import type { OperationalEvent } from "@/types/models";

interface TopbarProps {
    onOpenSidebar: () => void;
    activePage: string;
    onNavigate: (page: string) => void;
    workspaceName?: string;
}

const EVENT_TARGET: Record<OperationalEvent["type"], string> = {
    conflict_detected: "conflicts",
    conflict_resolved: "conflicts",
    request_added: "departments",
    request_reviewed: "departments",
    maintenance_updated: "maintenance",
    block_changed: "planner",
    block_approved: "planner",
    train_delayed: "trains",
    train_scheduled: "trains",
    asset_flagged: "corridor",
    report_generated: "reports"
};

const SEVERITY_DOT: Record<OperationalEvent["severity"], string> = {
    critical: "bg-red",
    warning: "bg-amber",
    info: "bg-blue"
};

export function Topbar({
    onOpenSidebar,
    activePage,
    onNavigate,
    workspaceName = "Demo Railway Division"
}: TopbarProps) {
    const repository = getRepository();
    const { data: events } = useData(() => repository.getRecentEvents());
    const { data: kpis } = useData(() => repository.getDashboardKpis());
    const logout = useLogout();

    const [query, setQuery] = useState("");
    const [searchOpen, setSearchOpen] = useState(false);
    const [notificationsOpen, setNotificationsOpen] = useState(false);
    const [helpOpen, setHelpOpen] = useState(false);
    const [profileOpen, setProfileOpen] = useState(false);
    const [readIds, setReadIds] = useState<string[]>([]);
    const searchRef = useRef<HTMLDivElement>(null);
    const profileRef = useRef<HTMLDivElement>(null);

    const user = getSessionUser();

    const results = useMemo(() => {
        const term = query.trim().toLowerCase();
        if (!term) return NAV_ITEMS.slice(0, 6);
        return NAV_ITEMS.filter(
            (item) =>
                item.label.toLowerCase().includes(term) ||
                item.hint.toLowerCase().includes(term)
        );
    }, [query]);

    const unread = (events ?? []).filter((event) => !readIds.includes(event.id));
    const alertCount = unread.filter((event) => event.severity !== "info").length;
    const latest = events?.[0];

    useEffect(() => {
        const onClickOutside = (event: MouseEvent) => {
            if (searchRef.current && !searchRef.current.contains(event.target as Node)) {
                setSearchOpen(false);
            }
            if (profileRef.current && !profileRef.current.contains(event.target as Node)) {
                setProfileOpen(false);
            }
        };
        const onKeyDown = (event: KeyboardEvent) => {
            if (event.key === "Escape") {
                setSearchOpen(false);
                setProfileOpen(false);
                setNotificationsOpen(false);
                setHelpOpen(false);
            }
        };
        document.addEventListener("mousedown", onClickOutside);
        document.addEventListener("keydown", onKeyDown);
        return () => {
            document.removeEventListener("mousedown", onClickOutside);
            document.removeEventListener("keydown", onKeyDown);
        };
    }, []);

    const closeAll = () => {
        setNotificationsOpen(false);
        setHelpOpen(false);
        setProfileOpen(false);
        setSearchOpen(false);
    };

    const goTo = (page: string) => {
        onNavigate(page);
        setQuery("");
        closeAll();
    };

    const openEvent = (event: OperationalEvent) => {
        setReadIds((previous) =>
            previous.includes(event.id) ? previous : [...previous, event.id]
        );
        goTo(EVENT_TARGET[event.type] ?? "dashboard");
    };

    return (
        <header className="h-[56px] shrink-0 bg-surface border-b border-border flex items-center justify-between px-2.5 lg:px-[18px] gap-3.5">
            <div className="flex items-center gap-3.5 min-w-0">
                <button
                    onClick={onOpenSidebar}
                    className="p-1 text-ink-soft bg-transparent border-none rounded hover:bg-paper lg:hidden"
                    aria-label="Open menu"
                >
                    <Menu className="w-5 h-5" />
                </button>

                <div className="flex flex-col min-w-0">
                    <div className="text-[12.5px] font-semibold text-ink truncate leading-tight">
                        {workspaceName}
                    </div>
                    <div className="text-[11px] text-muted truncate hidden md:block leading-tight mt-0.5">
                        Week {weekNumber()} &middot; 2026 &middot; Data: Simulation &middot;{" "}
                        {latest ? `latest event ${relativeTime(latest.timestamp)}` : "loading events"}
                        {kpis ? ` · ${kpis.trainsRunning} trains running` : ""}
                    </div>
                </div>

                <div className="hidden sm:inline-flex items-center gap-1.5 bg-amber-bg text-amber border border-amber-line text-[10.5px] font-bold tracking-[0.4px] px-2 py-[3px] rounded-full uppercase shrink-0">
                    <span className="w-1.5 h-1.5 rounded-full bg-amber animate-pulse" />
                    SIMULATION MODE
                </div>

                <div className="hidden xl:block ml-4">
                    <SimulatorControls />
                </div>
            </div>

            {/* Global module search */}
            <div className="relative hidden md:block w-[260px] lg:w-[300px]" ref={searchRef}>
                <Search className="pointer-events-none absolute left-2.5 top-1/2 -translate-y-1/2 w-3.5 h-3.5 text-muted" />
                <input
                    value={query}
                    onChange={(event) => {
                        setQuery(event.target.value);
                        setSearchOpen(true);
                    }}
                    onFocus={() => setSearchOpen(true)}
                    onKeyDown={(event) => {
                        if (event.key === "Enter" && results[0]) goTo(results[0].id);
                        if (event.key === "Escape") setSearchOpen(false);
                    }}
                    placeholder="Jump to a module…"
                    aria-label="Search modules"
                    className="w-full h-8 rounded-md border border-input bg-transparent pl-8 pr-3 text-[13px] text-ink placeholder:text-muted outline-none focus:border-ring focus:ring-2 focus:ring-ring/25"
                />

                {searchOpen && (
                    <div className="absolute left-0 right-0 top-[38px] z-50 rounded-md border bg-surface shadow-md overflow-hidden">
                        {results.length === 0 && (
                            <p className="px-3 py-3 text-[12.5px] text-muted">No module matches “{query}”.</p>
                        )}
                        {results.map((item) => {
                            const Icon = item.icon;
                            return (
                                <button
                                    key={item.id}
                                    type="button"
                                    onClick={() => goTo(item.id)}
                                    className={cn(
                                        "w-full flex items-center gap-2.5 px-3 py-2 text-left transition-colors hover:bg-accent",
                                        activePage === item.id && "bg-blue-bg/60"
                                    )}
                                >
                                    <Icon className="w-3.5 h-3.5 text-muted shrink-0" />
                                    <span className="min-w-0">
                                        <span className="block text-[12.5px] font-medium text-ink truncate">
                                            {item.label}
                                        </span>
                                        <span className="block text-[11px] text-muted truncate">{item.hint}</span>
                                    </span>
                                </button>
                            );
                        })}
                    </div>
                )}
            </div>

            <div className="flex items-center gap-1.5 shrink-0">
                {/* Notifications */}
                <div className="relative">
                    <button
                        onClick={() => {
                            setNotificationsOpen((value) => !value);
                            setHelpOpen(false);
                            setProfileOpen(false);
                        }}
                        className="w-8 h-8 rounded-[6px] border border-transparent bg-transparent flex items-center justify-center text-ink-soft relative hover:bg-paper hover:border-border transition-colors"
                        title="Notifications"
                        aria-label="Notifications"
                        aria-expanded={notificationsOpen}
                    >
                        <Bell className="w-[17px] h-[17px]" />
                        {alertCount > 0 && (
                            <span className="absolute -top-1 -right-1 min-w-[16px] h-4 px-1 rounded-full bg-red text-white text-[10px] font-bold flex items-center justify-center border border-white">
                                {alertCount}
                            </span>
                        )}
                    </button>

                    {notificationsOpen && (
                        <>
                            <div className="fixed inset-0 z-40" onClick={() => setNotificationsOpen(false)} />
                            <div className="absolute right-0 top-[40px] z-50 w-[330px] rounded-md border bg-surface shadow-md overflow-hidden">
                                <div className="flex items-center justify-between px-3 py-2 border-b bg-slate-50">
                                    <span className="text-[12.5px] font-semibold">
                                        Operations feed
                                    </span>
                                    <button
                                        type="button"
                                        onClick={() => setReadIds((events ?? []).map((event) => event.id))}
                                        className="inline-flex items-center gap-1 text-[11.5px] text-blue hover:underline"
                                    >
                                        <Check className="w-3 h-3" />
                                        Mark all read
                                    </button>
                                </div>

                                <div className="max-h-[320px] overflow-y-auto">
                                    {(events ?? []).slice(0, 10).map((event) => {
                                        const isRead = readIds.includes(event.id);
                                        return (
                                            <button
                                                key={event.id}
                                                type="button"
                                                onClick={() => openEvent(event)}
                                                className={cn(
                                                    "w-full text-left px-3 py-2.5 border-b border-border-soft last:border-0 hover:bg-accent transition-colors",
                                                    isRead && "opacity-60"
                                                )}
                                            >
                                                <div className="flex items-start gap-2">
                                                    <span
                                                        className={cn(
                                                            "w-2 h-2 rounded-full mt-1.5 shrink-0",
                                                            SEVERITY_DOT[event.severity]
                                                        )}
                                                    />
                                                    <span className="min-w-0">
                                                        <span className="block text-[12.5px] text-ink leading-snug">
                                                            {event.message}
                                                        </span>
                                                        <span className="block text-[11px] text-muted mt-0.5">
                                                            {event.actor} · {relativeTime(event.timestamp)}
                                                        </span>
                                                    </span>
                                                </div>
                                            </button>
                                        );
                                    })}
                                    {(events ?? []).length === 0 && (
                                        <p className="px-3 py-4 text-[12.5px] text-muted text-center">
                                            No operational events yet.
                                        </p>
                                    )}
                                </div>
                            </div>
                        </>
                    )}
                </div>

                {/* Help */}
                <div className="relative">
                    <button
                        onClick={() => {
                            setHelpOpen((value) => !value);
                            setNotificationsOpen(false);
                            setProfileOpen(false);
                        }}
                        className="w-8 h-8 rounded-[6px] border border-transparent bg-transparent flex items-center justify-center text-ink-soft hover:bg-paper hover:border-border transition-colors"
                        title="Help"
                        aria-label="Help"
                        aria-expanded={helpOpen}
                    >
                        <CircleHelp className="w-[17px] h-[17px]" />
                    </button>

                    {helpOpen && (
                        <>
                            <div className="fixed inset-0 z-40" onClick={() => setHelpOpen(false)} />
                            <div className="absolute right-0 top-[40px] z-50 w-[290px] rounded-md border bg-surface shadow-md p-3.5">
                                <div className="flex items-center gap-2 mb-2">
                                    <LayoutDashboard className="w-4 h-4 text-primary" />
                                    <span className="text-[13px] font-semibold">RMOS demo workspace</span>
                                </div>
                                <ul className="space-y-1.5 text-[12.5px] text-muted list-disc pl-4">
                                    <li>Use the sidebar filter or the top search to jump between modules.</li>
                                    <li>Every table supports sorting, paging and CSV export.</li>
                                    <li>Row actions, approvals and conflict resolutions are simulated locally.</li>
                                    <li>
                                        <span className="inline-flex items-center gap-1 align-middle">
                                            <Siren className="w-3 h-3" /> Recommendations require human approval.
                                        </span>
                                    </li>
                                </ul>
                                <button
                                    type="button"
                                    onClick={() => goTo("reports")}
                                    className="mt-3 inline-flex items-center gap-1.5 text-[12.5px] text-blue hover:underline"
                                >
                                    <FileText className="w-3.5 h-3.5" />
                                    Open the report library
                                </button>
                            </div>
                        </>
                    )}
                </div>

                {/* Profile trigger - a real button, keyboard reachable, opens the menu below. */}
                <div className="relative pl-2 border-l border-border ml-1" ref={profileRef}>
                    <button
                        type="button"
                        onClick={() => {
                            setProfileOpen((value) => !value);
                            setNotificationsOpen(false);
                            setHelpOpen(false);
                        }}
                        aria-haspopup="menu"
                        aria-expanded={profileOpen}
                        aria-controls="rmos-profile-menu"
                        aria-label={`Account menu for ${user?.username ?? "demo user"}`}
                        title={user?.username ? `Signed in as ${user.username}` : "Account menu"}
                        className={cn(
                            "group flex items-center gap-2 rounded-md border border-transparent bg-transparent py-1 pl-1 pr-2",
                            "cursor-pointer select-none",
                            "hover:bg-paper hover:border-border focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring/60",
                            profileOpen && "bg-paper border-border"
                        )}
                    >
                        <span
                            aria-hidden="true"
                            className="w-[26px] h-[26px] rounded-full bg-blue text-white text-[11px] font-bold flex items-center justify-center shrink-0 pointer-events-none"
                        >
                            {initials(user?.username)}
                        </span>
                        <span className="hidden md:flex flex-col items-start leading-tight min-w-0 pointer-events-none">
                            <span className="text-[12.5px] font-semibold text-ink truncate max-w-[130px]">
                                {displayName(user?.username)}
                            </span>
                            <span className="text-[11px] text-muted truncate max-w-[130px]">
                                {roleLabel(user?.role)}
                            </span>
                        </span>
                    </button>

                    {profileOpen && (
                        <>
                            <div className="fixed inset-0 z-40" onClick={() => setProfileOpen(false)} />
                            <div
                                id="rmos-profile-menu"
                                role="menu"
                                aria-label="Account"
                                className="absolute right-0 top-[46px] z-50 w-[236px] rounded-md border bg-surface shadow-md overflow-hidden"
                            >
                                <div className="flex items-center gap-2.5 px-3 py-2.5 border-b bg-slate-50">
                                    <span
                                        aria-hidden="true"
                                        className="w-8 h-8 rounded-full bg-blue text-white text-[12px] font-bold flex items-center justify-center shrink-0"
                                    >
                                        {initials(user?.username)}
                                    </span>
                                    <span className="min-w-0">
                                        <span className="block text-[12.5px] font-semibold text-ink truncate">
                                            {displayName(user?.username)}
                                        </span>
                                        <span className="block text-[11px] text-muted truncate">
                                            {roleLabel(user?.role)}
                                        </span>
                                    </span>
                                </div>

                                <div className="p-1.5">
                                    <div className="px-2 py-1.5 text-[11.5px] text-muted">
                                        Signed in as <span className="font-mono text-ink">{user?.username ?? "admin"}</span>
                                    </div>

                                    <button
                                        type="button"
                                        role="menuitem"
                                        onClick={() => {
                                            setProfileOpen(false);
                                            onNavigate("dashboard");
                                        }}
                                        className="w-full flex items-center gap-2 rounded-[5px] px-2 py-2 text-left text-[12.5px] font-medium text-ink hover:bg-accent focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring/60 cursor-pointer"
                                    >
                                        <LayoutDashboard className="w-3.5 h-3.5 text-muted" aria-hidden="true" />
                                        Workspace
                                    </button>

                                    <button
                                        type="button"
                                        role="menuitem"
                                        onClick={() => {
                                            setProfileOpen(false);
                                            goTo("assistant");
                                        }}
                                        className="w-full flex items-center gap-2 rounded-[5px] px-2 py-2 text-left text-[12.5px] font-medium text-ink hover:bg-accent focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring/60 cursor-pointer"
                                    >
                                        <Settings className="w-3.5 h-3.5 text-muted" aria-hidden="true" />
                                        AI Assistant
                                    </button>

                                    <div className="my-1 border-t" />

                                    <button
                                        type="button"
                                        role="menuitem"
                                        onClick={logout}
                                        className="w-full flex items-center gap-2 rounded-[5px] px-2 py-2 text-left text-[12.5px] font-semibold text-red hover:bg-red-bg focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-red-line cursor-pointer"
                                    >
                                        <LogOut className="w-3.5 h-3.5" aria-hidden="true" />
                                        Log out
                                    </button>
                                </div>
                            </div>
                        </>
                    )}
                </div>
            </div>
        </header>
    );
}

/** Initials for the avatar chip; falls back to the demo initials. */
function initials(username?: string): string {
    if (!username) return "RD";
    const parts = username.trim().split(/[\s._-]+/).filter(Boolean);
    if (parts.length === 0) return "RD";
    if (parts.length === 1) return parts[0].slice(0, 2).toUpperCase();
    return (parts[0][0] + parts[1][0]).toUpperCase();
}

/** Human label for the signed-in account. */
function displayName(username?: string): string {
    if (!username) return "RMOS Demo";
    const cleaned = username.trim();
    if (!cleaned) return "RMOS Demo";
    return cleaned
        .split(/[\s._-]+/)
        .filter(Boolean)
        .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
        .join(" ");
}

/** Role is only shown when the backend actually returned one. */
function roleLabel(role?: string): string {
    if (!role) return "Signed in";
    return role
        .replace(/^ROLE_/i, "")
        .toLowerCase()
        .replace(/(^|[_\s])([a-z])/g, (_match, prefix: string, letter: string) => prefix + letter.toUpperCase());
}

function weekNumber(): number {
    const date = new Date();
    const start = new Date(date.getFullYear(), 0, 1);
    const days = Math.floor((date.getTime() - start.getTime()) / 86_400_000);
    return Math.ceil((days + start.getDay() + 1) / 7);
}
