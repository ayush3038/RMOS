import { useEffect, useMemo, useRef, useState } from "react";
import {
    CalendarDays,
    CalendarRange,
    Calendar,
    ChevronRight,
    FileText,
    LayoutDashboard,
    LineChart,
    LogOut,
    Route,
    Search,
    Settings,
    Siren,
    Train,
    Users,
    Wrench,
    X
} from "lucide-react";

import { OfficialLogo } from "@/components/rmos/OfficialLogo";
import { NAV_GROUPS, type BadgeKey } from "@/config/navigation";
import { useData } from "@/hooks/useData";
import { useLogout } from "@/hooks/useLogout";
import { getRepository } from "@/services/api";
import { getSessionUser } from "@/services/api/session";
import { usePreferences } from "@/store/preferences";
import { cn } from "@/lib/utils";
import {
    Sheet,
    SheetContent,
    SheetDescription,
    SheetHeader,
    SheetTitle
} from "@/components/ui/sheet";
import { Button } from "@/components/ui/button";

interface SidebarProps {
    isOpen: boolean;
    activePage: string;
    onNavigate: (page: string) => void;
    onClose: () => void;
}

/** Icon map kept local so navigation config stays data-only. */
const ICONS: Record<string, typeof LayoutDashboard> = {
    dashboard: LayoutDashboard,
    trains: Train,
    corridor: Route,
    planner: CalendarDays,
    maintenance: Wrench,
    departments: Users,
    weekly: Calendar,
    monthly: CalendarRange,
    conflicts: Siren,
    analytics: LineChart,
    reports: FileText
};

export function Sidebar({ isOpen, activePage, onNavigate, onClose }: SidebarProps) {
    const repository = getRepository();
    const tasks = useData(() => repository.getMaintenanceTasks());
    const conflicts = useData(() => repository.getConflicts());
    const trains = useData(() => repository.getTrains());
    const requests = useData(() => repository.getDepartmentRequests());
    const logout = useLogout();

    const [filter, setFilter] = useState("");
    const [collapsed, setCollapsed] = useState<Record<string, boolean>>({});
    const [settingsOpen, setSettingsOpen] = useState(false);
    const [userMenuOpen, setUserMenuOpen] = useState(false);
    const userMenuRef = useRef<HTMLDivElement>(null);

    const user = getSessionUser();

    useEffect(() => {
        if (!userMenuOpen) return;
        const onClickOutside = (event: MouseEvent) => {
            if (userMenuRef.current && !userMenuRef.current.contains(event.target as Node)) {
                setUserMenuOpen(false);
            }
        };
        const onKeyDown = (event: KeyboardEvent) => {
            if (event.key === "Escape") setUserMenuOpen(false);
        };
        document.addEventListener("mousedown", onClickOutside);
        document.addEventListener("keydown", onKeyDown);
        return () => {
            document.removeEventListener("mousedown", onClickOutside);
            document.removeEventListener("keydown", onKeyDown);
        };
    }, [userMenuOpen]);

    const counts: Record<BadgeKey, number> = {
        pendingTasks: (tasks.data ?? []).filter(
            (task) => task.status === "pending" || task.status === "in-progress"
        ).length,
        openConflicts: (conflicts.data ?? []).filter((conflict) => conflict.status !== "resolved").length,
        delayedTrains: (trains.data ?? []).filter((train) => train.status === "delayed").length,
        pendingApprovals: (requests.data ?? []).filter((request) => request.status === "proposed").length
    };

    const groups = useMemo(() => {
        const query = filter.trim().toLowerCase();
        if (!query) return NAV_GROUPS;
        return NAV_GROUPS.map((group) => ({
            ...group,
            items: group.items.filter(
                (item) =>
                    item.label.toLowerCase().includes(query) ||
                    item.hint.toLowerCase().includes(query)
            )
        })).filter((group) => group.items.length > 0);
    }, [filter]);

    const totalMatches = groups.reduce((total, group) => total + group.items.length, 0);

    return (
        <>
            {/* Mobile backdrop */}
            {isOpen && (
                <div
                    className="fixed inset-0 z-40 bg-[rgba(6,12,20,0.45)] lg:hidden"
                    onClick={onClose}
                    aria-hidden="true"
                />
            )}

            {/* Sidebar Container */}
            <aside
                className={cn(
                    "fixed top-0 bottom-0 left-0 z-50 w-[248px] flex flex-col bg-navy-900 text-[#C9D4DF] border-r border-[#08111C]",
                    "transition-transform duration-300 ease-in-out lg:static lg:translate-x-0",
                    isOpen ? "translate-x-0" : "-translate-x-full"
                )}
            >
                {/* Brand Header */}
                <div className="px-[18px] pt-5 pb-4 border-b border-white/5">
                    <div className="flex items-center gap-[9px]">
                        <OfficialLogo className="w-8 h-8 shrink-0" />
                        <div className="min-w-0">
                            <div className="font-bold text-[15.5px] tracking-[0.2px] text-white leading-tight">
                                RMOS
                            </div>
                            <div className="text-[10.5px] text-[#8CA0B3] mt-[2px] tracking-[0.3px] uppercase leading-tight">
                                Maintenance Planning & Optimization
                            </div>
                        </div>
                    </div>
                </div>

                {/* Nav filter */}
                <div className="px-[14px] pt-3.5 pb-1.5 relative">
                    <Search className="w-3.5 h-3.5 absolute left-[22px] top-1/2 -translate-y-1/2 text-[#5E7086]" />
                    <input
                        value={filter}
                        onChange={(event) => setFilter(event.target.value)}
                        placeholder="Filter modules…"
                        aria-label="Filter navigation"
                        className="w-full h-8 rounded-[5px] bg-white/5 border border-white/10 pl-8 pr-7 text-[12.5px] text-white placeholder:text-[#5E7086] outline-none focus:border-[#4F8FC4] focus:bg-white/[0.08]"
                    />
                    {filter && (
                        <button
                            type="button"
                            onClick={() => setFilter("")}
                            className="absolute right-[22px] top-1/2 -translate-y-1/2 text-[#5E7086] hover:text-white"
                            aria-label="Clear filter"
                        >
                            <X className="w-3.5 h-3.5" />
                        </button>
                    )}
                </div>

                {/* Grouped Navigation - the only scroll container in the sidebar. */}
                <nav
                    className="rmos-sidebar-scroll flex-1 min-h-0 overflow-y-auto overflow-x-hidden px-[10px] py-[6px]"
                    tabIndex={0}
                    aria-label="Module navigation"
                >
                    {totalMatches === 0 && (
                        <p className="text-[12px] text-[#5E7086] px-2 py-4 text-center">
                            No module matches “{filter}”.
                        </p>
                    )}

                    {groups.map((group) => {
                        const isCollapsed = Boolean(collapsed[group.id]);
                        const activeInGroup = group.items.some((item) => item.id === activePage);

                        return (
                            <div key={group.id} className="pt-3">
                                <button
                                    type="button"
                                    onClick={() =>
                                        setCollapsed((previous) => ({
                                            ...previous,
                                            [group.id]: !previous[group.id]
                                        }))
                                    }
                                    className="w-full flex items-center gap-1.5 px-[10px] pb-[6px] group"
                                    aria-expanded={!isCollapsed}
                                >
                                    <ChevronRight
                                        className={cn(
                                            "w-3 h-3 text-[#5E7086] transition-transform",
                                            !isCollapsed && "rotate-90"
                                        )}
                                    />
                                    <span
                                        className={cn(
                                            "text-[10.5px] uppercase tracking-[0.6px] font-semibold flex-1 text-left",
                                            activeInGroup ? "text-[#8CA0B3]" : "text-[#5E7086]"
                                        )}
                                    >
                                        {group.label}
                                    </span>
                                    {activeInGroup && (
                                        <span className="w-1.5 h-1.5 rounded-full bg-[#4F8FC4]" />
                                    )}
                                </button>

                                {!isCollapsed &&
                                    group.items.map((item) => {
                                        const Icon = ICONS[item.id] ?? item.icon;
                                        const isActive = activePage === item.id;
                                        const badgeValue = item.badge ? counts[item.badge] : 0;

                                        return (
                                            <button
                                                key={item.id}
                                                onClick={() => {
                                                    onNavigate(item.id);
                                                    onClose();
                                                }}
                                                title={item.hint}
                                                className={cn(
                                                    "w-full flex items-center gap-[10px] px-[10px] py-[7px] rounded-[5px] text-[13.2px] font-medium mb-[1px] transition-colors",
                                                    "focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#4F8FC4]",
                                                    isActive
                                                        ? "bg-[rgba(42,92,138,0.28)] text-white border-l-2 border-l-[#4F8FC4]"
                                                        : "text-[#AEBDCC] border-l-2 border-transparent hover:bg-white/5 hover:text-white"
                                                )}
                                                aria-current={isActive ? "page" : undefined}
                                            >
                                                <Icon
                                                    className={cn(
                                                        "w-4 h-4 shrink-0 transition-opacity",
                                                        isActive ? "opacity-100" : "opacity-80"
                                                    )}
                                                />
                                                <span className="flex-1 text-left truncate">{item.label}</span>
                                                {badgeValue > 0 && (
                                                    <span
                                                        className={cn(
                                                            "min-w-[20px] px-1.5 h-[18px] rounded-full text-[10.5px] font-bold flex items-center justify-center",
                                                            item.badge === "openConflicts" ||
                                                                item.badge === "delayedTrains"
                                                                ? "bg-red text-white"
                                                                : "bg-[rgba(79,143,196,0.25)] text-[#9EC7E6]"
                                                        )}
                                                    >
                                                        {badgeValue}
                                                    </span>
                                                )}
                                            </button>
                                        );
                                    })}
                            </div>
                        );
                    })}
                </nav>

                {/* Bottom Area */}
                <div className="relative shrink-0 border-t border-white/5 p-[10px]" ref={userMenuRef}>
                    <div className="flex items-center justify-between gap-2 px-[10px] py-1.5 mb-1.5">
                        <span className="text-[10.5px] uppercase tracking-[0.6px] text-[#5E7086]">
                            Data source
                        </span>
                        <span className="text-[10.5px] font-bold text-amber tracking-[0.4px]">
                            SIMULATION
                        </span>
                    </div>

                    {/*
                    Bottom-left user section. The whole row is a real button:
                    click / Enter / Space opens the menu, which carries the
                    settings action and the shared logout.
                */}
                <button
                    type="button"
                    onClick={() => setUserMenuOpen((open) => !open)}
                    aria-haspopup="menu"
                    aria-expanded={userMenuOpen}
                    aria-controls="rmos-sidebar-user-menu"
                    aria-label={`User menu for ${user?.username ?? "RMOS Demo"}`}
                    className={cn(
                        "w-full flex items-center gap-[9px] rounded-[5px] px-[10px] py-2 text-left",
                        "border-l-2 border-transparent cursor-pointer select-none",
                        "hover:bg-white/5 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#4F8FC4]",
                        userMenuOpen && "bg-white/5 border-l-[#4F8FC4]"
                    )}
                >
                    <span
                        aria-hidden="true"
                        className="w-[26px] h-[26px] rounded-full bg-blue text-white text-[11px] font-bold flex items-center justify-center shrink-0 pointer-events-none"
                    >
                        {initials(user?.username)}
                    </span>
                    <span className="text-left min-w-0 flex-1 pointer-events-none">
                        <span className="block text-[12.5px] text-[#D5DEE7] font-semibold leading-tight truncate">
                            {displayName(user?.username)}
                        </span>
                        <span className="block text-[11px] text-[#7A8B9C] leading-tight mt-0.5 truncate">
                            {roleLabel(user?.role)}
                        </span>
                    </span>
                </button>

                {userMenuOpen && (
                    <div
                        id="rmos-sidebar-user-menu"
                        role="menu"
                        aria-label="Account"
                        className="absolute left-[10px] right-[10px] bottom-[calc(100%-4px)] z-50 rounded-md border border-[#1E3448] bg-navy-800 shadow-lg overflow-hidden"
                    >
                        <div className="px-3 py-2 border-b border-white/5">
                            <p className="text-[11.5px] text-[#AEBDCC] truncate">
                                Signed in as{" "}
                                <span className="font-mono text-white">{user?.username ?? "admin"}</span>
                            </p>
                        </div>
                        <div className="p-1">
                            <button
                                type="button"
                                role="menuitem"
                                onClick={() => {
                                    setUserMenuOpen(false);
                                    setSettingsOpen(true);
                                }}
                                className="w-full flex items-center gap-2 rounded-[5px] px-2 py-2 text-left text-[12.5px] font-medium text-[#D5DEE7] hover:bg-white/5 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#4F8FC4] cursor-pointer"
                            >
                                <Settings className="w-3.5 h-3.5 shrink-0 opacity-80" aria-hidden="true" />
                                Settings
                            </button>
                            <button
                                type="button"
                                role="menuitem"
                                onClick={() => {
                                    setUserMenuOpen(false);
                                    onClose();
                                    logout();
                                }}
                                className="w-full flex items-center gap-2 rounded-[5px] px-2 py-2 text-left text-[12.5px] font-semibold text-[#F0A9A3] hover:bg-[rgba(166,51,44,0.18)] focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#A6332C] cursor-pointer"
                            >
                                <LogOut className="w-3.5 h-3.5 shrink-0" aria-hidden="true" />
                                Log out
                            </button>
                        </div>
                    </div>
                )}
                </div>
            </aside>

            <SettingsSheet open={settingsOpen} onOpenChange={setSettingsOpen} />
        </>
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
    if (!role) return "RMOS Demo Workspace";
    return role
        .replace(/^ROLE_/i, "")
        .toLowerCase()
        .replace(/(^|[_\s])([a-z])/g, (_match, prefix: string, letter: string) => prefix + letter.toUpperCase());
}

function SettingsSheet({
    open,
    onOpenChange
}: {
    open: boolean;
    onOpenChange: (open: boolean) => void;
}) {
    const {
        density,
        setDensity,
        showDemoBanner,
        toggleDemoBanner,
        rowsPerPage,
        setRowsPerPage,
        autoRefreshSeconds,
        setAutoRefreshSeconds
    } = usePreferences();

    return (
        <Sheet open={open} onOpenChange={onOpenChange}>
            <SheetContent className="w-full sm:max-w-sm overflow-y-auto">
                <SheetHeader>
                    <SheetTitle>Workspace settings</SheetTitle>
                    <SheetDescription>
                        Preferences are stored locally in this browser for the demo workspace.
                    </SheetDescription>
                </SheetHeader>

                <div className="space-y-6 px-4 pb-4">
                    <section className="space-y-2">
                        <h4 className="text-[11.5px] uppercase tracking-[0.5px] font-semibold text-muted">
                            Table density
                        </h4>
                        <div className="flex gap-2">
                            {(["compact", "comfortable"] as const).map((option) => (
                                <button
                                    key={option}
                                    type="button"
                                    onClick={() => setDensity(option)}
                                    className={cn(
                                        "flex-1 h-9 rounded-md border text-[13px] font-medium capitalize transition-colors",
                                        density === option
                                            ? "border-blue bg-blue-bg text-blue"
                                            : "border-border hover:bg-accent"
                                    )}
                                >
                                    {option}
                                </button>
                            ))}
                        </div>
                        <p className="text-[12px] text-muted">Row height used by every module table.</p>
                    </section>

                    <section className="space-y-2">
                        <h4 className="text-[11.5px] uppercase tracking-[0.5px] font-semibold text-muted">
                            Rows per page
                        </h4>
                        <div className="flex gap-2">
                            {[5, 10, 20, 50].map((option) => (
                                <button
                                    key={option}
                                    type="button"
                                    onClick={() => setRowsPerPage(option)}
                                    className={cn(
                                        "flex-1 h-9 rounded-md border text-[13px] font-mono transition-colors",
                                        rowsPerPage === option
                                            ? "border-blue bg-blue-bg text-blue"
                                            : "border-border hover:bg-accent"
                                    )}
                                >
                                    {option}
                                </button>
                            ))}
                        </div>
                    </section>

                    <section className="space-y-2">
                        <h4 className="text-[11.5px] uppercase tracking-[0.5px] font-semibold text-muted">
                            Auto refresh
                        </h4>
                        <div className="flex gap-2">
                            {[0, 30, 60, 300].map((option) => (
                                <button
                                    key={option}
                                    type="button"
                                    onClick={() => setAutoRefreshSeconds(option)}
                                    className={cn(
                                        "flex-1 h-9 rounded-md border text-[13px] transition-colors",
                                        autoRefreshSeconds === option
                                            ? "border-blue bg-blue-bg text-blue"
                                            : "border-border hover:bg-accent"
                                    )}
                                >
                                    {option === 0 ? "Off" : `${option}s`}
                                </button>
                            ))}
                        </div>
                    </section>

                    <section className="space-y-2">
                        <h4 className="text-[11.5px] uppercase tracking-[0.5px] font-semibold text-muted">
                            Demonstration banner
                        </h4>
                        <div className="flex items-center justify-between border rounded-md px-3 py-2.5">
                            <span className="text-[13px]">Show simulation notice</span>
                            <Button
                                size="sm"
                                variant={showDemoBanner ? "default" : "outline"}
                                onClick={toggleDemoBanner}
                            >
                                {showDemoBanner ? "Visible" : "Hidden"}
                            </Button>
                        </div>
                    </section>

                    <p className="text-[12px] text-muted border-t pt-4">
                        RMOS provides planning recommendations only. Every action still requires human review before
                        execution.
                    </p>
                </div>
            </SheetContent>
        </Sheet>
    );
}
