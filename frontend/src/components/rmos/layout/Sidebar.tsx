import {
    LayoutDashboard,
    CalendarDays,
    Wrench,
    Route,
    Users,
    Calendar,
    CalendarRange,
    Siren,
    LineChart,
    FileText,
    Settings,
    Train
} from "lucide-react";

import { OfficialLogo } from "@/components/rmos/OfficialLogo";

interface SidebarProps {
    isOpen: boolean;
    activePage: string;
    onNavigate: (page: string) => void;
    onClose: () => void;
}

const navItems = [
    { id: 'dashboard', label: 'Dashboard', icon: LayoutDashboard },
    { id: 'planner', label: 'Block Planner', icon: CalendarDays },
    { id: 'maintenance', label: 'Maintenance', icon: Wrench },
    { id: 'trains', label: 'Train Operations', icon: Train },
    { id: 'corridor', label: 'Corridor Availability', icon: Route },
    { id: 'departments', label: 'Departments', icon: Users },
    { id: 'weekly', label: 'Weekly Plan', icon: Calendar },
    { id: 'monthly', label: 'Monthly Plan', icon: CalendarRange },
    { id: 'conflicts', label: 'Conflicts', icon: Siren },
    { id: 'analytics', label: 'Analytics', icon: LineChart },
    { id: 'reports', label: 'Reports', icon: FileText },
];

export function Sidebar({ isOpen, activePage, onNavigate, onClose }: SidebarProps) {
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
                className={`
                    fixed top-0 bottom-0 left-0 z-50 w-[236px] flex flex-col bg-navy-900 text-[#C9D4DF] border-r border-[#08111C]
                    transition-transform duration-300 ease-in-out lg:static lg:translate-x-0
                    ${isOpen ? "translate-x-0" : "-translate-x-full"}
                `}
            >
                {/* Brand Header */}
                <div className="px-[18px] pt-5 pb-4 border-b border-white/5">
                    <div className="flex items-center gap-[9px]">
                        <OfficialLogo className="w-8 h-8 shrink-0" />
                        <div>
                            <div className="font-bold text-[15.5px] tracking-[0.2px] text-white leading-tight">RMOS</div>
                            <div className="text-[10.5px] text-[#8CA0B3] mt-[2px] tracking-[0.3px] uppercase leading-tight">Maintenance Planning & Optimization</div>
                        </div>
                    </div>
                </div>

                {/* Main Navigation */}
                <nav className="flex-1 overflow-y-auto px-[10px] py-[10px]">
                    <div className="text-[10.5px] uppercase tracking-[0.6px] text-[#5E7086] px-[10px] pt-[14px] pb-[6px] font-semibold">
                        Primary Nav
                    </div>
                    {navItems.map((item) => {
                        const Icon = item.icon;
                        const isActive = activePage === item.id;
                        return (
                            <button
                                key={item.id}
                                onClick={() => {
                                    onNavigate(item.id);
                                    onClose();
                                }}
                                className={`
                                    w-full flex items-center gap-[10px] px-[10px] py-2 rounded-[5px] text-[13.2px] font-medium mb-[1px]
                                    transition-colors
                                    ${isActive
                                        ? "bg-[rgba(42,92,138,0.28)] text-white border-l-2 border-l-[#4F8FC4]"
                                        : "text-[#AEBDCC] border-l-2 border-transparent hover:bg-white/5 hover:text-white"
                                    }
                                `}
                            >
                                <Icon className={`w-4 h-4 shrink-0 transition-opacity ${isActive ? "opacity-100" : "opacity-80"}`} />
                                <span>{item.label}</span>
                            </button>
                        );
                    })}
                </nav>

                {/* Bottom Area Component */}
                <div className="border-t border-white/5 p-[10px]">
                    <button className="w-full flex items-center gap-[10px] px-[10px] py-2 rounded-[5px] text-[13.2px] font-medium text-[#AEBDCC] border-l-2 border-transparent hover:bg-white/5 hover:text-white mb-2">
                        <Settings className="w-4 h-4 shrink-0 opacity-80" />
                        <span>Settings</span>
                    </button>
                    <div className="flex items-center gap-[9px] px-[10px] py-2 rounded-[5px] hover:bg-white/5 cursor-pointer">
                        <div className="w-[26px] h-[26px] rounded-full bg-blue text-white text-[11px] font-bold flex items-center justify-center shrink-0">
                            RD
                        </div>
                        <div className="text-left">
                            <div className="text-[12.5px] text-[#D5DEE7] font-semibold leading-tight">RMOS Demo</div>
                            <div className="text-[11px] text-[#7A8B9C] leading-tight mt-0.5">Simulation User</div>
                        </div>
                    </div>
                </div>
            </aside>
        </>
    );
}
