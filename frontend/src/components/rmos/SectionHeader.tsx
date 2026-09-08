import React from 'react';

export function SectionHeader({ title, description, badge }: { title: string, description?: string, badge?: React.ReactNode }) {
    return (
        <div className="flex justify-between items-center px-4 py-[13px] border-b border-border-soft">
            <div>
                <h3 className="text-[13.5px] font-bold m-0 text-ink leading-tight">{title}</h3>
                {description && <div className="text-[11.5px] text-muted mt-0.5">{description}</div>}
            </div>
            {badge && <div>{badge}</div>}
        </div>
    );
}
