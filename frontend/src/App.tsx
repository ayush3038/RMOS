import { useState } from "react";
import { AppShell } from "@/components/rmos/layout/AppShell";
import { PageHeader } from "@/components/rmos/PageHeader";
import { EmptyState } from "@/components/rmos/EmptyState";

// Placeholder components mappings
const PAGE_TITLES: Record<string, { title: string, description: string }> = {
    dashboard: { title: "RMOS Dashboard", description: "Dashboard implementation pending." },
    planner: { title: "Block Planner", description: "Block Planner implementation pending." },
    maintenance: { title: "Maintenance", description: "Maintenance implementation pending." },
    trains: { title: "Train Operations", description: "Train Operations implementation pending." },
    corridor: { title: "Corridor Availability", description: "Corridor Availability implementation pending." },
    departments: { title: "Departments", description: "Departments implementation pending." },
    weekly: { title: "Weekly Plan", description: "Weekly Plan implementation pending." },
    monthly: { title: "Monthly Plan", description: "Monthly Plan implementation pending." },
    conflicts: { title: "Conflicts", description: "Conflicts implementation pending." },
    analytics: { title: "Analytics", description: "Analytics implementation pending." },
    reports: { title: "Reports", description: "Reports implementation pending." },
};

import { Dashboard } from "@/components/rmos/dashboard/Dashboard";

function App() {
    const [activePage, setActivePage] = useState("dashboard");

    const pageData = PAGE_TITLES[activePage] || {
        title: "Unknown Page",
        description: "This page does not exist."
    };

    return (
        <AppShell activePage={activePage} onNavigate={setActivePage}>
            {activePage === "dashboard" ? (
                <Dashboard />
            ) : (
                <div className="flex flex-col gap-6">
                    <PageHeader
                        title={pageData.title}
                        description={pageData.description}
                    />

                    <div className="border border-border-soft rounded-[6px] bg-white p-6">
                        <EmptyState
                            message={`Module scaffold \u2014 implementation pending for ${activePage}.`}
                        />
                    </div>
                </div>
            )}
        </AppShell>
    );
}

export default App;
