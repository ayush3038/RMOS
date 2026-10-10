import type { ComponentType, ReactNode } from "react";
import { cn } from "@/lib/utils";

export type KpiTone = "default" | "red" | "amber" | "green" | "blue";

export interface KpiItem {
    label: string;
    value: ReactNode;
    hint?: string;
    tone?: KpiTone;
    icon?: ComponentType<{ className?: string }>;
}

const TONE_CLASSES: Record<KpiTone, string> = {
    default: "text-ink",
    red: "text-red",
    amber: "text-amber",
    green: "text-green",
    blue: "text-blue"
};

const ICON_TONES: Record<KpiTone, string> = {
    default: "text-muted",
    red: "text-red",
    amber: "text-amber",
    green: "text-green",
    blue: "text-blue"
};

interface KpiStripProps {
    items: KpiItem[];
    className?: string;
}

/** Dense metric strip used at the top of every module page. */
export function KpiStrip({ items, className }: KpiStripProps) {
    return (
        <div
            className={cn(
                "grid gap-3 grid-cols-2 md:grid-cols-3 xl:grid-cols-6",
                className
            )}
        >
            {items.map((item, index) => {
                const Icon = item.icon;
                const tone = item.tone ?? "default";
                return (
                    <div
                        key={`${item.label}-${index}`}
                        className="border rounded-md bg-surface px-3.5 py-3 flex flex-col justify-between min-h-[86px]"
                    >
                        <div className="flex items-start justify-between gap-2">
                            <span className="text-[11.5px] font-semibold uppercase tracking-[0.4px] text-muted leading-tight">
                                {item.label}
                            </span>
                            {Icon && <Icon className={cn("w-4 h-4 shrink-0", ICON_TONES[tone])} />}
                        </div>
                        <div>
                            <div
                                className={cn(
                                    "text-[22px] leading-none font-bold font-mono tabular-nums",
                                    TONE_CLASSES[tone]
                                )}
                            >
                                {item.value}
                            </div>
                            {item.hint && (
                                <div className="text-[11.5px] text-muted mt-1.5 leading-tight">
                                    {item.hint}
                                </div>
                            )}
                        </div>
                    </div>
                );
            })}
        </div>
    );
}
