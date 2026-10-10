export interface CsvColumn<T> {
    key: string;
    header: string;
    value: (row: T) => string | number;
}

const escapeCell = (value: string | number): string => {
    const raw = String(value ?? "");
    return /[",\n]/.test(raw) ? `"${raw.replace(/"/g, '""')}"` : raw;
};

/** Browser-side CSV download helper used by the module toolbars. */
export function downloadCsv<T>(filename: string, columns: CsvColumn<T>[], rows: T[]): void {
    const header = columns.map((column) => escapeCell(column.header)).join(",");
    const body = rows.map((row) => columns.map((column) => escapeCell(column.value(row))).join(","));
    const csv = [header, ...body].join("\n");
    const blob = new Blob([csv], { type: "text/csv;charset=utf-8;" });
    const url = URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = url;
    link.download = filename.endsWith(".csv") ? filename : `${filename}.csv`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
}
