import type { ReactNode } from "react";

type TableToolbarProps = {
    label: string;
    children: ReactNode;
};

export function TableToolbar({ label, children }: TableToolbarProps) {
    return (
        <section className="table-toolbar" aria-label={label}>
            {children}
        </section>
    );
}
