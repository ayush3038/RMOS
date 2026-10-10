const MS_DAY = 86_400_000;

export const startOfDay = (date: Date): Date => {
    const copy = new Date(date);
    copy.setHours(0, 0, 0, 0);
    return copy;
};

export const addDays = (date: Date, days: number): Date =>
    new Date(date.getTime() + days * MS_DAY);

/** Week starts on Monday for planning views. */
export const startOfWeek = (date: Date): Date => {
    const day = startOfDay(date);
    const weekday = (day.getDay() + 6) % 7;
    return addDays(day, -weekday);
};

export const startOfMonth = (date: Date): Date =>
    new Date(date.getFullYear(), date.getMonth(), 1);

export const monthLabel = (date: Date): string =>
    date.toLocaleDateString(undefined, { month: "long", year: "numeric" });

export const weekLabel = (weekStart: Date): string => {
    const weekEnd = addDays(weekStart, 6);
    return `${weekStart.toLocaleDateString(undefined, { day: "2-digit", month: "short" })} – ${weekEnd.toLocaleDateString(undefined, { day: "2-digit", month: "short", year: "numeric" })}`;
};

export const toDayKey = (date: Date): string =>
    `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}-${String(date.getDate()).padStart(2, "0")}`;

/** 42 cells (6 weeks) covering the month, including leading/trailing days. */
export const monthGrid = (monthDate: Date): Date[] => {
    const first = startOfMonth(monthDate);
    const gridStart = startOfWeek(first);
    return Array.from({ length: 42 }, (_, index) => addDays(gridStart, index));
};

export const WEEKDAY_LABELS = ["Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"];

export const isSameDay = (a: Date, b: Date): boolean => toDayKey(a) === toDayKey(b);
