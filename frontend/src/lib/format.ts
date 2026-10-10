export const formatDate = (value: string): string =>
    new Date(value).toLocaleDateString(undefined, {
        day: "2-digit",
        month: "short",
        year: "numeric"
    });

export const formatShortDate = (value: string | Date): string => {
    const date = typeof value === "string" ? new Date(value) : value;
    return date.toLocaleDateString(undefined, { day: "2-digit", month: "short" });
};

export const formatTime = (value: string): string =>
    new Date(value).toLocaleTimeString(undefined, { hour: "2-digit", minute: "2-digit" });

export const formatDateTime = (value: string): string =>
    `${formatShortDate(value)} · ${formatTime(value)}`;

export const relativeTime = (value: string): string => {
    const diffMs = Date.now() - new Date(value).getTime();
    const diffMin = Math.round(diffMs / 60000);
    if (Math.abs(diffMin) < 1) return "just now";
    if (Math.abs(diffMin) < 60) return `${diffMin > 0 ? "" : "in "}${Math.abs(diffMin)}m${diffMin > 0 ? " ago" : ""}`;
    const diffHours = Math.round(diffMin / 60);
    if (Math.abs(diffHours) < 24) return `${diffHours > 0 ? "" : "in "}${Math.abs(diffHours)}h${diffHours > 0 ? " ago" : ""}`;
    const diffDays = Math.round(diffHours / 24);
    return `${diffDays > 0 ? "" : "in "}${Math.abs(diffDays)}d${diffDays > 0 ? " ago" : ""}`;
};

export const formatDuration = (minutes: number): string => {
    if (minutes < 60) return `${minutes}m`;
    const hours = Math.floor(minutes / 60);
    const rest = minutes % 60;
    return rest ? `${hours}h ${rest}m` : `${hours}h`;
};

export const formatCurrency = (value: number): string =>
    new Intl.NumberFormat("en-IN", {
        style: "currency",
        currency: "INR",
        maximumFractionDigits: 0
    }).format(value);

export const isOverdue = (value: string): boolean => new Date(value).getTime() < Date.now();
