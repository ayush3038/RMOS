import { cn } from '@/lib/utils';

export type StatusVariant = 'amber' | 'red' | 'green' | 'blue' | 'grey';

export type StatusLevel =
    | 'Critical' | 'High' | 'Medium' | 'Low'
    | 'Pending' | 'Scheduled' | 'In Progress' | 'Completed' | 'Postponed' | 'Cancelled'
    | 'On Time' | 'Delayed' | 'Minor Delay' | 'Diverted' | 'Terminated' | 'Running'
    | 'Proposed' | 'Approved' | 'Modified' | 'Rejected' | 'Utilized' | 'Released'
    | 'Open' | 'Acknowledged' | 'Resolved'
    | 'Available' | 'Maintenance' | 'Blocked' | 'Restricted'
    | 'Good' | 'Fair' | 'Poor'
    | 'Ready' | 'Generating' | 'Failed'
    | 'Simulation';

const statusToVariant: Record<StatusLevel, StatusVariant> = {
    Critical: 'red',
    Delayed: 'red',
    Rejected: 'red',
    Cancelled: 'red',
    Terminated: 'grey',
    Blocked: 'red',
    Open: 'red',
    Poor: 'red',
    Failed: 'red',
    High: 'amber',
    Pending: 'amber',
    'Minor Delay': 'amber',
    Modified: 'amber',
    Simulation: 'amber',
    Diverted: 'amber',
    Acknowledged: 'amber',
    Restricted: 'amber',
    Fair: 'amber',
    Generating: 'amber',
    Medium: 'blue',
    Scheduled: 'blue',
    'In Progress': 'blue',
    Maintenance: 'blue',
    Running: 'blue',
    Good: 'blue',
    Ready: 'blue',
    Completed: 'green',
    'On Time': 'green',
    Approved: 'green',
    Resolved: 'green',
    Available: 'green',
    Released: 'green',
    Utilized: 'green',
    Low: 'grey',
    Postponed: 'grey',
    Proposed: 'grey'
};

export function StatusChip({
    status,
    showDot = true,
    className
}: {
    status: StatusLevel;
    showDot?: boolean;
    className?: string;
}) {
    const variant = statusToVariant[status] || 'grey';

    const variantClasses = {
        green: 'bg-green-bg text-green border-green-line',
        amber: 'bg-amber-bg text-amber border-amber-line',
        red: 'bg-red-bg text-red border-red-line',
        grey: 'bg-paper text-muted border-border',
        blue: 'bg-blue-bg text-blue border-blue-line'
    }[variant];

    return (
        <span
            className={cn(
                'inline-flex items-center gap-[5px] text-[11px] font-semibold px-2 py-[2.5px] rounded-full border whitespace-nowrap',
                variantClasses,
                className
            )}
        >
            {showDot && <span className="w-[5px] h-[5px] rounded-full bg-current shrink-0" />}
            {status}
        </span>
    );
}
