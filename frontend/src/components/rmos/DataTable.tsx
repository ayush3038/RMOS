import { useMemo, useState, type ReactNode } from "react";
import {
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableHeader,
    TableRow
} from "@/components/ui/table";
import { Button } from "@/components/ui/button";
import { ArrowDown, ArrowUp, ChevronLeft, ChevronRight, ChevronsUpDown } from "lucide-react";
import { cn } from "@/lib/utils";
import { usePreferences } from "@/store/preferences";

export interface DataTableColumn<T> {
    key: string;
    header: string;
    cell: (row: T) => ReactNode;
    sortValue?: (row: T) => string | number;
    headerClassName?: string;
    cellClassName?: string;
}

type SortDirection = "asc" | "desc";

interface DataTableProps<T> {
    columns: DataTableColumn<T>[];
    rows: T[];
    rowId: (row: T) => string;
    onRowClick?: (row: T) => void;
    selectedRowId?: string | null;
    pageSize?: number;
    isLoading?: boolean;
    errorMessage?: string | null;
    emptyMessage?: string;
    initialSort?: { key: string; direction: SortDirection };
}

/**
 * Sortable, paginated table used across the RMOS module pages.
 * Respects the density preference from the settings panel.
 */
export function DataTable<T>({
    columns,
    rows,
    rowId,
    onRowClick,
    selectedRowId,
    pageSize,
    isLoading,
    errorMessage,
    emptyMessage = "No records match the current filters.",
    initialSort
}: DataTableProps<T>) {
    const { density, rowsPerPage } = usePreferences();
    const size = pageSize ?? rowsPerPage;
    const [sort, setSort] = useState<{ key: string; direction: SortDirection } | null>(
        initialSort ?? null
    );
    const [page, setPage] = useState(1);

    const sorted = useMemo(() => {
        if (!sort) return rows;
        const column = columns.find((entry) => entry.key === sort.key);
        if (!column?.sortValue) return rows;
        const factor = sort.direction === "asc" ? 1 : -1;
        return [...rows].sort((first, second) => {
            const a = column.sortValue!(first);
            const b = column.sortValue!(second);
            if (typeof a === "number" && typeof b === "number") return (a - b) * factor;
            return String(a).localeCompare(String(b)) * factor;
        });
    }, [rows, sort, columns]);

    const pageCount = Math.max(1, Math.ceil(sorted.length / size));
    const currentPage = Math.min(page, pageCount);
    const startIndex = (currentPage - 1) * size;
    const visibleRows = sorted.slice(startIndex, startIndex + size);

    // Reset to the first page whenever the result set changes.
    const [resultLength, setResultLength] = useState(rows.length);
    if (resultLength !== rows.length) {
        setResultLength(rows.length);
        if (page !== 1) setPage(1);
    }

    const toggleSort = (key: string) => {
        setSort((previous) => {
            if (previous?.key !== key) return { key, direction: "asc" };
            if (previous.direction === "asc") return { key, direction: "desc" };
            return null;
        });
        setPage(1);
    };

    const rowPadding = density === "compact" ? "py-[5px]" : "py-2.5";

    if (isLoading) {
        return (
            <div className="rounded-md border">
                <div className="space-y-2 p-4">
                    {Array.from({ length: 5 }).map((_, index) => (
                        <div key={index} className="h-8 animate-pulse rounded bg-slate-100" />
                    ))}
                </div>
            </div>
        );
    }

    if (errorMessage) {
        return (
            <div className="rounded-md border border-red-line bg-red-bg px-4 py-6 text-center text-sm text-red">
                {errorMessage}
            </div>
        );
    }

    if (rows.length === 0) {
        return (
            <div className="rounded-md border border-dashed px-4 py-10 text-center text-sm text-muted">
                {emptyMessage}
            </div>
        );
    }

    return (
        <div className="rounded-md border">
            <div className="overflow-x-auto">
                <Table>
                    <TableHeader>
                        <TableRow className="bg-slate-50 hover:bg-slate-50">
                            {columns.map((column) => {
                                const active = sort?.key === column.key;
                                return (
                                    <TableHead
                                        key={column.key}
                                        className={cn(
                                            "h-9 text-[11.5px] uppercase tracking-[0.4px] text-muted",
                                            column.headerClassName
                                        )}
                                    >
                                        {column.sortValue ? (
                                            <button
                                                type="button"
                                                onClick={() => toggleSort(column.key)}
                                                className={cn(
                                                    "inline-flex items-center gap-1 hover:text-ink transition-colors",
                                                    active && "text-ink font-semibold"
                                                )}
                                            >
                                                {column.header}
                                                {active ? (
                                                    sort!.direction === "asc" ? (
                                                        <ArrowUp className="w-3 h-3" />
                                                    ) : (
                                                        <ArrowDown className="w-3 h-3" />
                                                    )
                                                ) : (
                                                    <ChevronsUpDown className="w-3 h-3 opacity-40" />
                                                )}
                                            </button>
                                        ) : (
                                            column.header
                                        )}
                                    </TableHead>
                                );
                            })}
                        </TableRow>
                    </TableHeader>
                    <TableBody>
                        {visibleRows.map((row) => {
                            const id = rowId(row);
                            return (
                                <TableRow
                                    key={id}
                                    onClick={onRowClick ? () => onRowClick(row) : undefined}
                                    className={cn(
                                        onRowClick && "cursor-pointer",
                                        selectedRowId === id && "bg-blue-bg/60 hover:bg-blue-bg"
                                    )}
                                >
                                    {columns.map((column) => (
                                        <TableCell
                                            key={column.key}
                                            className={cn(rowPadding, column.cellClassName)}
                                        >
                                            {column.cell(row)}
                                        </TableCell>
                                    ))}
                                </TableRow>
                            );
                        })}
                    </TableBody>
                </Table>
            </div>

            <div className="flex items-center justify-between gap-3 border-t px-3 py-2 text-xs text-muted">
                <span>
                    Showing {startIndex + 1}–{Math.min(startIndex + size, sorted.length)} of{" "}
                    {sorted.length}
                </span>
                <div className="flex items-center gap-1.5">
                    <Button
                        variant="outline"
                        size="icon-xs"
                        onClick={() => setPage((value) => Math.max(1, value - 1))}
                        disabled={currentPage <= 1}
                        aria-label="Previous page"
                    >
                        <ChevronLeft className="w-3.5 h-3.5" />
                    </Button>
                    <span className="font-mono tabular-nums">
                        {currentPage} / {pageCount}
                    </span>
                    <Button
                        variant="outline"
                        size="icon-xs"
                        onClick={() => setPage((value) => Math.min(pageCount, value + 1))}
                        disabled={currentPage >= pageCount}
                        aria-label="Next page"
                    >
                        <ChevronRight className="w-3.5 h-3.5" />
                    </Button>
                </div>
            </div>
        </div>
    );
}
