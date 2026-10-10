import type { ReactNode } from "react";
import { cn } from "@/lib/utils";

export function DetailItem({ label, value }: { label: string; value: ReactNode }) {
    return (
        <div className="border rounded-md px-2.5 py-2">
            <div className="text-[10.5px] uppercase tracking-[0.4px] font-semibold text-muted">
                {label}
            </div>
            <div className="text-[13px] mt-0.5">{value}</div>
        </div>
    );
}

export function DetailGrid({
    items,
    columns = 2,
    className
}: {
    items: Array<{ label: string; value: ReactNode }>;
    columns?: 2 | 3;
    className?: string;
}) {
    return (
        <div
            className={cn(
                `grid gap-3 ${columns === 3 ? "grid-cols-2 sm:grid-cols-3" : "grid-cols-2"}`,
                className
            )}
        >
            {items.map((item) => (
                <DetailItem key={item.label} label={item.label} value={item.value} />
            ))}
        </div>
    );
}
