export function matchesSearch(query: string, values: unknown[]) {
    const normalizedQuery = query.trim().toLowerCase();

    if (!normalizedQuery) {
        return true;
    }

    return values.some((value) =>
        String(value ?? "").toLowerCase().includes(normalizedQuery)
    );
}
