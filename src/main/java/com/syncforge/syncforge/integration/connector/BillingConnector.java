package com.syncforge.syncforge.integration.connector;

import com.syncforge.syncforge.integration.model.IntegrationType;
import org.springframework.stereotype.Component;

@Component
public class BillingConnector implements ExternalSystemConnector {

    @Override
    public IntegrationType getSupportedType() {
        return IntegrationType.BILLING;
    }

    @Override
    public SyncOperationResult sync(SyncOperationContext context) {
        simulateNetworkDelay();

        if (shouldFail(context)) {
            return new SyncOperationResult(
                    false,
                    "Simulated Billing API failure"
            );
        }

        return new SyncOperationResult(
                true,
                "Billing customer sync completed for target ID: "
                        + context.targetExternalEntityId()
        );
    }

    private boolean shouldFail(SyncOperationContext context) {
        String payloadJson = context.payloadJson();

        return payloadJson != null
                && payloadJson.contains("\"failTarget\":\"BILLING\"");
    }

    private void simulateNetworkDelay() {
        try {
            Thread.sleep(300);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Billing sync interrupted", exception);
        }
    }
}
