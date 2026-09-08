import { cn } from '@/lib/utils';
export function EmptyState({ message, className }: { message: string, className?: string }) {
    return (
        <div className={cn("py-10 px-5 text-center text-muted text-[12.5px]", className)}>
            {message}
        </div>
    );
}
