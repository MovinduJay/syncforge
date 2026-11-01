type QueryFeedbackProps = {
    isLoading: boolean;
    isError: boolean;
    loadingMessage: string;
    errorMessage: string;
};

export function QueryFeedback({
    isLoading,
    isError,
    loadingMessage,
    errorMessage,
}: QueryFeedbackProps) {
    return (
        <>
            {isLoading && <p className="muted" role="status">{loadingMessage}</p>}
            {isError && <p className="error" role="alert">{errorMessage}</p>}
        </>
    );
}
