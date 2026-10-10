import { useState, useEffect, useRef } from "react";
import { Sidebar } from "./Sidebar";
import { Topbar } from "./Topbar";
import { usePreferences } from "@/store/preferences";
import { AUTO_REFRESH_EVENT } from "@/hooks/useData";

interface AppShellProps {
    children: React.ReactNode;
    activePage: string;
    onNavigate: (page: string) => void;
}

export function AppShell({ children, activePage, onNavigate }: AppShellProps) {
    const [sidebarOpen, setSidebarOpen] = useState(false);
    const { autoRefreshSeconds } = usePreferences();
    const mainRef = useRef<HTMLElement>(null);

    // Start each module at the top of the scroll area.
    useEffect(() => {
        mainRef.current?.scrollTo({ top: 0 });
    }, [activePage]);

    useEffect(() => {
        const handleKeyDown = (e: KeyboardEvent) => {
            if (e.key === "Escape" && sidebarOpen) {
                setSidebarOpen(false);
            }
        };
        window.addEventListener("keydown", handleKeyDown);
        return () => window.removeEventListener("keydown", handleKeyDown);
    }, [sidebarOpen]);

    // Workspace level auto refresh driven by the settings panel.
    useEffect(() => {
        if (!autoRefreshSeconds) return;
        const interval = window.setInterval(() => {
            window.dispatchEvent(new Event(AUTO_REFRESH_EVENT));
        }, autoRefreshSeconds * 1000);
        return () => window.clearInterval(interval);
    }, [autoRefreshSeconds]);

    return (
        <div className="flex h-screen overflow-hidden bg-paper text-ink font-sans text-[14px] leading-relaxed antialiased">
            <Sidebar
                isOpen={sidebarOpen}
                activePage={activePage}
                onNavigate={onNavigate}
                onClose={() => setSidebarOpen(false)}
            />

            <div className="flex-1 flex flex-col min-w-0 h-screen">
                <Topbar onOpenSidebar={() => setSidebarOpen(true)} activePage={activePage} onNavigate={onNavigate} />
                <main ref={mainRef} className="flex-1 overflow-y-auto px-3 py-3.5 sm:px-4 sm:py-4 md:px-6 md:py-5">
                    <div className="max-w-[1480px] mx-auto min-h-full">
                        {children}
                    </div>
                </main>
            </div>
        </div>
    );
}
