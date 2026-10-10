import type { ReactNode } from "react";
import { Download, RotateCw, Search, X } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";

interface ModuleToolbarProps {
    search: string;
    onSearchChange: (value: string) => void;
    placeholder?: string;
    filters?: ReactNode;
    actions?: ReactNode;
    resultLabel?: string;
    onRefresh?: () => void;
    onExport?: () => void;
    exportDisabled?: boolean;
}

/**
 * Shared filter bar for module pages: free-text search, slot for select
 * filters, record counter and refresh/export actions.
 */
export function ModuleToolbar({
    search,
    onSearchChange,
    placeholder = "Search records…",
    filters,
    actions,
    resultLabel,
    onRefresh,
    onExport,
    exportDisabled
}: ModuleToolbarProps) {
    return (
        <div className="flex flex-wrap items-center gap-2 border rounded-md bg-surface px-3 py-2.5">
            <div className="relative w-full sm:w-[240px]">
                <Search className="pointer-events-none absolute left-2.5 top-1/2 -translate-y-1/2 w-3.5 h-3.5 text-muted" />
                <Input
                    value={search}
                    onChange={(event) => onSearchChange(event.target.value)}
                    placeholder={placeholder}
                    className="h-8 pl-8 pr-7 text-[13px]"
                    aria-label={placeholder}
                />
                {search && (
                    <button
                        type="button"
                        onClick={() => onSearchChange("")}
                        className="absolute right-2 top-1/2 -translate-y-1/2 text-muted hover:text-ink"
                        aria-label="Clear search"
                    >
                        <X className="w-3.5 h-3.5" />
                    </button>
                )}
            </div>

            <div className="flex flex-wrap items-center gap-2">{filters}</div>

            <div className="ml-auto flex items-center gap-2">
                {resultLabel && (
                    <span className="text-[12px] text-muted tabular-nums hidden sm:inline">
                        {resultLabel}
                    </span>
                )}
                {actions}
                {onRefresh && (
                    <Button
                        variant="outline"
                        size="sm"
                        onClick={onRefresh}
                        className="h-8"
                        title="Reload module data"
                    >
                        <RotateCw className="w-3.5 h-3.5" />
                        Refresh
                    </Button>
                )}
                {onExport && (
                    <Button
                        variant="outline"
                        size="sm"
                        onClick={onExport}
                        disabled={exportDisabled}
                        className="h-8"
                    >
                        <Download className="w-3.5 h-3.5" />
                        Export CSV
                    </Button>
                )}
            </div>
        </div>
    );
}

interface FilterSelectProps {
    label: string;
    value: string;
    onChange: (value: string) => void;
    options: Array<{ value: string; label: string }>;
}

/** Compact native select used for module filters (keeps the toolbar light). */
export function FilterSelect({ label, value, onChange, options }: FilterSelectProps) {
    return (
        <label className="inline-flex items-center gap-1.5">
            <span className="text-[11.5px] font-semibold uppercase tracking-[0.4px] text-muted">
                {label}
            </span>
            <select
                value={value}
                onChange={(event) => onChange(event.target.value)}
                className="h-8 rounded-md border border-input bg-surface px-2 text-[13px] text-ink outline-none focus:border-ring focus:ring-2 focus:ring-ring/30"
            >
                {options.map((option) => (
                    <option key={option.value} value={option.value}>
                        {option.label}
                    </option>
                ))}
            </select>
        </label>
    );
}
