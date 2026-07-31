package com.syncforge.syncforge.integration.connector;

import com.syncforge.syncforge.integration.model.IntegrationType;
import org.springframework.stereotype.Component;

@Component
public class SupportConnector implements ExternalSystemConnector {

    @Override
    public IntegrationType getSupportedType() {
        return IntegrationType.SUPPORT;
    }

    @Override
    public SyncOperationResult sync(SyncOperationContext context) {
        simulateNetworkDelay();

        if (shouldFail(context)) {
            return new SyncOperationResult(
                    false,
                    "Simulated Support API failure"
            );
        }

        return new SyncOperationResult(
                true,
                "Support customer sync completed for target ID: "
                        + context.targetExternalEntityId()
        );
    }

    private boolean shouldFail(SyncOperationContext context) {
        String payloadJson = context.payloadJson();

        return payloadJson != null
                && payloadJson.contains("\"failTarget\":\"SUPPORT\"");
    }

    private void simulateNetworkDelay() {
        try {
            Thread.sleep(300);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Support sync interrupted", exception);
        }
    }
}
