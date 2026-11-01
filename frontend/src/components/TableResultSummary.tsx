type TableResultSummaryProps = {
    visibleCount: number;
    totalCount: number;
    itemLabel: string;
};

export function TableResultSummary({
    visibleCount,
    totalCount,
    itemLabel,
}: TableResultSummaryProps) {
    return (
        <p className="toolbar-summary" aria-live="polite">
            Showing {visibleCount} of {totalCount} {itemLabel}
        </p>
    );
}
