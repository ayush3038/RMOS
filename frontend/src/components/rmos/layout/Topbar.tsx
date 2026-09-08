import { Menu, Bell, CircleHelp } from "lucide-react";

interface TopbarProps {
    onOpenSidebar: () => void;
    workspaceName?: string;
    metadata?: string;
}

export function Topbar({
    onOpenSidebar,
    workspaceName = "Demo Railway Division"
}: TopbarProps) {
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
                    {/* Hide metadata on very small screens, show on md+ */}
                    <div className="text-[11px] text-muted truncate hidden md:block leading-tight mt-0.5">
                        Week 35 &middot; 2026 &middot; Data: Simulation &middot; Updated 2 min ago
                    </div>
                </div>

                <div className="hidden sm:inline-flex items-center gap-1.5 bg-amber-bg text-amber border border-amber-line text-[10.5px] font-bold tracking-[0.4px] px-2 py-[3px] rounded-full uppercase shrink-0">
                    <span className="w-1.5 h-1.5 rounded-full bg-amber" />
                    SIMULATION MODE
                </div>
            </div>

            <div className="flex items-center gap-1.5 shrink-0">
                <button
                    className="w-8 h-8 rounded-[6px] border border-transparent bg-transparent flex items-center justify-center text-ink-soft relative hover:bg-paper hover:border-border transition-colors"
                    title="Notifications"
                    aria-label="Notifications"
                >
                    <Bell className="w-[17px] h-[17px]" />
                    <span className="absolute top-[5px] right-[5px] w-1.5 h-1.5 rounded-full bg-red border-[1.5px] border-white box-content" />
                </button>

                <button
                    className="w-8 h-8 rounded-[6px] border border-transparent bg-transparent flex items-center justify-center text-ink-soft hover:bg-paper hover:border-border transition-colors"
                    title="Help"
                    aria-label="Help"
                >
                    <CircleHelp className="w-[17px] h-[17px]" />
                </button>

                <div className="flex items-center gap-2 pl-2 border-l border-border ml-1">
                    <div className="w-[26px] h-[26px] rounded-full bg-blue text-white text-[11px] font-bold flex items-center justify-center shrink-0">
                        RD
                    </div>
                </div>
            </div>
        </header>
    );
}
