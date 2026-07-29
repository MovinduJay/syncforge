package com.syncforge.syncforge.integration.connector;

import com.syncforge.syncforge.integration.model.IntegrationType;
import com.syncforge.syncforge.syncjob.model.SyncJob;
import org.springframework.stereotype.Component;

@Component
public class SupportConnector implements ExternalSystemConnector {

    @Override
    public IntegrationType getSupportedType() {
        return IntegrationType.SUPPORT;
    }

    @Override
    public SyncOperationResult sync(SyncJob syncJob) {
        simulateNetworkDelay();

        if (shouldFail(syncJob)) {
            return new SyncOperationResult(
                    false,
                    "Simulated Support API failure"
            );
        }

        return new SyncOperationResult(
                true,
                "Support customer sync completed"
        );
    }

    private boolean shouldFail(SyncJob syncJob) {
        String payloadJson = syncJob.getWebhookEvent().getPayloadJson();

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
