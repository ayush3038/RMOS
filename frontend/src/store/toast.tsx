import {
    createContext,
    useCallback,
    useContext,
    useMemo,
    useState,
    type ReactNode
} from "react";
import { AlertTriangle, CheckCircle2, Info, X, XCircle } from "lucide-react";

export type ToastTone = "info" | "success" | "warning" | "danger";

interface ToastItem {
    id: number;
    message: string;
    tone: ToastTone;
}

interface ToastValue {
    toast: (message: string, tone?: ToastTone) => void;
}

const ToastContext = createContext<ToastValue | null>(null);

const TONE_STYLES: Record<ToastTone, { icon: typeof Info; className: string }> = {
    info: { icon: Info, className: "text-blue" },
    success: { icon: CheckCircle2, className: "text-green" },
    warning: { icon: AlertTriangle, className: "text-amber" },
    danger: { icon: XCircle, className: "text-red" }
};

let toastSequence = 0;

export function ToastProvider({ children }: { children: ReactNode }) {
    const [toasts, setToasts] = useState<ToastItem[]>([]);

    const dismiss = useCallback((id: number) => {
        setToasts((prev) => prev.filter((item) => item.id !== id));
    }, []);

    const toast = useCallback(
        (message: string, tone: ToastTone = "success") => {
            const id = ++toastSequence;
            setToasts((prev) => [...prev.slice(-3), { id, message, tone }]);
            window.setTimeout(() => dismiss(id), 3800);
        },
        [dismiss]
    );

    const value = useMemo<ToastValue>(() => ({ toast }), [toast]);

    return (
        <ToastContext.Provider value={value}>
            {children}
            <div className="fixed bottom-5 right-5 z-[60] flex flex-col gap-2 items-end pointer-events-none">
                {toasts.map((item) => {
                    const tone = TONE_STYLES[item.tone];
                    const Icon = tone.icon;
                    return (
                        <div
                            key={item.id}
                            className="pointer-events-auto flex items-start gap-2.5 bg-navy-900 text-white border border-white/10 shadow-md rounded-md px-3.5 py-2.5 max-w-[360px] animate-in slide-in-from-bottom-4 fade-in"
                            role="status"
                        >
                            <Icon className={`w-4 h-4 mt-0.5 shrink-0 ${tone.className}`} />
                            <span className="text-[12.5px] leading-snug">{item.message}</span>
                            <button
                                onClick={() => dismiss(item.id)}
                                className="text-white/50 hover:text-white transition-colors"
                                aria-label="Dismiss notification"
                            >
                                <X className="w-3.5 h-3.5" />
                            </button>
                        </div>
                    );
                })}
            </div>
        </ToastContext.Provider>
    );
}

export function useToast(): ToastValue {
    const context = useContext(ToastContext);
    if (!context) {
        throw new Error("useToast must be used within ToastProvider");
    }
    return context;
}
