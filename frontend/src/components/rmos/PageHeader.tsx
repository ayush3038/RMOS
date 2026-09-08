import React from 'react';

export function PageHeader({ title, description, actions }: { title: string, description?: React.ReactNode, actions?: React.ReactNode }) {
    return (
        <div className="flex justify-between items-start gap-4 mb-[18px] flex-wrap">
            <div>
                <h1 className="text-[20px] font-bold m-0 mb-[3px] text-ink tracking-tight">{title}</h1>
                {description && <p className="m-0 text-muted text-[13px]">{description}</p>}
            </div>
            {actions && <div className="flex gap-2 shrink-0">{actions}</div>}
        </div>
    );
}
