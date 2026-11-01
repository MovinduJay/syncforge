type TableEmptyStateProps = {
    columnCount: number;
    message: string;
};

export function TableEmptyState({ columnCount, message }: TableEmptyStateProps) {
    return (
        <tr>
            <td colSpan={columnCount} className="empty-state">{message}</td>
        </tr>
    );
}
