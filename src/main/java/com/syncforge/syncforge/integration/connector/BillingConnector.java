package com.syncforge.syncforge.integration.connector;

import com.syncforge.syncforge.integration.model.IntegrationType;
import com.syncforge.syncforge.syncjob.model.SyncJob;
import org.springframework.stereotype.Component;

@Component
public class BillingConnector implements ExternalSystemConnector {

    @Override
    public IntegrationType getSupportedType() {
        return IntegrationType.BILLING;
    }

    @Override
    public SyncOperationResult sync(SyncJob syncJob) {
        simulateNetworkDelay();

        if (shouldFail(syncJob)) {
            return new SyncOperationResult(
                    false,
                    "Simulated Billing API failure"
            );
        }

        return new SyncOperationResult(
                true,
                "Billing customer sync completed"
        );
    }

    private boolean shouldFail(SyncJob syncJob) {
        String payloadJson = syncJob.getWebhookEvent().getPayloadJson();

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
