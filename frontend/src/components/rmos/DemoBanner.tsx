import { FlaskConical, X } from "lucide-react";
import { usePreferences } from "@/store/preferences";

/** Simulation notice shown above module content; can be dismissed in settings. */
export function DemoBanner() {
    const { showDemoBanner, toggleDemoBanner } = usePreferences();

    if (!showDemoBanner) return null;

    return (
        <div className="flex items-center justify-between gap-4 border border-amber-line bg-amber-bg rounded-md px-3.5 py-2.5">
            <div className="flex items-start gap-2.5 min-w-0">
                <FlaskConical className="w-4 h-4 text-amber shrink-0 mt-0.5" />
                <div className="min-w-0">
                    <p className="font-semibold text-amber text-[13px] leading-tight">
                        Simulation mode · synthetic demonstration data
                    </p>
                    <p className="text-[12px] text-amber/80 leading-snug mt-0.5">
                        Every record below is generated for demonstration. Nothing here is a field
                        result and no recommendation is applied without human approval.
                    </p>
                </div>
            </div>
            <div className="flex items-center gap-2 shrink-0">
                <span className="text-[10.5px] font-bold tracking-[0.5px] px-2 py-1 rounded-full bg-amber text-white uppercase">
                    DEMO
                </span>
                <button
                    type="button"
                    onClick={toggleDemoBanner}
                    className="text-amber/70 hover:text-amber transition-colors"
                    aria-label="Hide simulation notice"
                >
                    <X className="w-4 h-4" />
                </button>
            </div>
        </div>
    );
}
