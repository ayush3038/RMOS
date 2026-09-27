import { useState } from "react";
import { AppShell } from "@/components/rmos/layout/AppShell";
import { Dashboard } from "@/components/rmos/dashboard/Dashboard";
import { ModulePage } from "@/components/rmos/ModulePage";

const PAGE_TITLES: Record<string, { title: string; description: string }> = {
    planner: {
        title: "Block Planner",
        description: "Maintenance block planning and candidate scheduling.",
    },
    maintenance: {
        title: "Maintenance",
        description: "Maintenance task visibility and prioritization.",
    },
    trains: {
        title: "Train Operations",
        description: "Current synthetic train and corridor operating state.",
    },
    corridor: {
        title: "Corridor Availability",
        description: "Availability and maintenance state across corridors.",
    },
    departments: {
        title: "Departments",
        description: "Cross-department maintenance and block requests.",
    },
    weekly: {
        title: "Weekly Plan",
        description: "Weekly maintenance planning overview.",
    },
    monthly: {
        title: "Monthly Plan",
        description: "Monthly maintenance planning overview.",
    },
    conflicts: {
        title: "Conflicts",
        description: "Detected maintenance, train, and resource conflicts.",
    },
    analytics: {
        title: "Analytics",
        description: "Operational KPIs and RMOS planning indicators.",
    },
    reports: {
        title: "Reports",
        description: "Operational summary and planning information.",
    },
};

function App() {
    const [activePage, setActivePage] = useState("dashboard");

    return (
        <AppShell activePage={activePage} onNavigate={setActivePage}>
            {activePage === "dashboard" ? (
                <Dashboard />
            ) : (
                <ModulePage
                    page={activePage}
                    title={
                        PAGE_TITLES[activePage]?.title ??
                        "RMOS Module"
                    }
                    description={
                        PAGE_TITLES[activePage]?.description ??
                        "RMOS operational module."
                    }
                />
            )}
        </AppShell>
    );
}

export default App;