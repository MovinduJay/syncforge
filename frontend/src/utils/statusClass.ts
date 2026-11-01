export function getStatusClass(value: string) {
    if (value.endsWith("SUCCEEDED") || value === "PROCESSED") {
        return "status status-success";
    }

    if (value.includes("DEAD_LETTER")) {
        return "status status-dead";
    }

    if (value.endsWith("FAILED")) {
        return "status status-failed";
    }

    if (value.endsWith("PROCESSING") || value === "DUPLICATE") {
        return "status status-processing";
    }

    return "status";
}
