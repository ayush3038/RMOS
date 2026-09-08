import { cn } from '@/lib/utils';

export type StatusVariant = 'amber' | 'red' | 'green' | 'blue' | 'grey';
export type StatusLevel = 'Critical' | 'High' | 'Medium' | 'Low' | 'Pending' | 'Scheduled' | 'Completed' | 'Postponed' | 'On Time' | 'Delayed' | 'Minor Delay' | 'Proposed' | 'Approved' | 'Modified' | 'Rejected' | 'Simulation';

const statusToVariant: Record<StatusLevel, StatusVariant> = {
    Critical: 'red',
    Delayed: 'red',
    Rejected: 'red',
    High: 'amber',
    Pending: 'amber',
    'Minor Delay': 'amber',
    Modified: 'amber',
    Simulation: 'amber',
    Medium: 'blue',
    Scheduled: 'blue',
    Completed: 'green',
    'On Time': 'green',
    Approved: 'green',
    Low: 'grey',
    Postponed: 'grey',
    Proposed: 'grey',
};

export function StatusChip({ status, showDot = true, className }: { status: StatusLevel, showDot?: boolean, className?: string }) {
    const variant = statusToVariant[status] || 'grey';

    const variantClasses = {
        green: 'bg-green-bg text-green border-green-line',
        amber: 'bg-amber-bg text-amber border-amber-line',
        red: 'bg-red-bg text-red border-red-line',
        grey: 'bg-paper text-muted border-border',
        blue: 'bg-blue-bg text-blue border-blue-line',
    }[variant];

    return (
        <span className={cn("inline-flex items-center gap-[5px] text-[11px] font-semibold px-2 py-[2.5px] rounded-full border whitespace-nowrap", variantClasses, className)}>
            {showDot && <span className="w-[5px] h-[5px] rounded-full bg-current shrink-0" />}
            {status}
        </span>
    );
}
