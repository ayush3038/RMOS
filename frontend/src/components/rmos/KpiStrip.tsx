import type { ComponentType, ReactNode } from "react";
import { cn } from "@/lib/utils";

export type KpiTone = "default" | "red" | "amber" | "green" | "blue";

export interface KpiItem {
    label: string;
    value: ReactNode;
    hint?: string;
    tone?: KpiTone;
    icon?: ComponentType<{ className?: string }>;
    /** Module id this metric summarises. When set (and the strip receives an
     * `onNavigate`), the card becomes a clickable button that opens that module. */
    page?: string;
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
    /** When provided, items carrying a `page` render as clickable cards that
     * navigate to that module. Items without a `page` stay plain cards. */
    onNavigate?: (page: string) => void;
}

const CARD_BASE_CLASS =
    "border rounded-md bg-surface px-3.5 py-3 flex flex-col justify-between min-h-[86px]";

/** Extra affordances for clickable cards (pointer, hover, visible focus ring). */
const CARD_INTERACTIVE_CLASS =
    "cursor-pointer text-left transition-colors hover:bg-slate-50/70 hover:border-ring/50 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring/60";

/** Dense metric strip used at the top of every module page. */
export function KpiStrip({ items, className, onNavigate }: KpiStripProps) {
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
                const content = (
                    <>
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
                    </>
                );

                // Only items with a meaningful destination become buttons;
                // everything else keeps the original non-interactive card.
                if (item.page && onNavigate) {
                    return (
                        <button
                            key={`${item.label}-${index}`}
                            type="button"
                            onClick={() => onNavigate(item.page as string)}
                            className={cn(CARD_BASE_CLASS, CARD_INTERACTIVE_CLASS)}
                        >
                            {content}
                        </button>
                    );
                }

                return (
                    <div
                        key={`${item.label}-${index}`}
                        className={CARD_BASE_CLASS}
                    >
                        {content}
                    </div>
                );
            })}
        </div>
    );
}
