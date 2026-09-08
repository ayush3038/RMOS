import { cn } from '@/lib/utils';
export function LoadingState({ message = "Loading...", className }: { message?: string, className?: string }) {
    return (
        <div className={cn("py-10 px-5 text-center text-faint flex flex-col items-center gap-3 text-[12.5px]", className)}>
            <div className="w-5 h-5 rounded-full border-[1.6px] border-navy-600 border-t-transparent animate-spin" />
            {message}
        </div>
    );
}
