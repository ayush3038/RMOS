import { useState, useEffect } from "react";
import { Sidebar } from "./Sidebar";
import { Topbar } from "./Topbar";

interface AppShellProps {
    children: React.ReactNode;
    activePage: string;
    onNavigate: (page: string) => void;
}

export function AppShell({ children, activePage, onNavigate }: AppShellProps) {
    const [sidebarOpen, setSidebarOpen] = useState(false);

    useEffect(() => {
        const handleKeyDown = (e: KeyboardEvent) => {
            if (e.key === "Escape" && sidebarOpen) {
                setSidebarOpen(false);
            }
        };
        window.addEventListener("keydown", handleKeyDown);
        return () => window.removeEventListener("keydown", handleKeyDown);
    }, [sidebarOpen]);

    return (
        <div className="flex h-screen overflow-hidden bg-paper text-ink font-sans text-[14px] leading-relaxed antialiased">
            <Sidebar
                isOpen={sidebarOpen}
                activePage={activePage}
                onNavigate={onNavigate}
                onClose={() => setSidebarOpen(false)}
            />

            <div className="flex-1 flex flex-col min-w-0 h-screen">
                <Topbar onOpenSidebar={() => setSidebarOpen(true)} />
                <main className="flex-1 overflow-y-auto px-3 py-3.5 sm:px-4 sm:py-4 md:px-6 md:py-5">
                    <div className="max-w-[1480px] mx-auto h-full">
                        {children}
                    </div>
                </main>
            </div>
        </div>
    );
}
