package com.syncforge.syncforge.webhook.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReceiveWebhookRequest(

        @NotBlank(message = "External event ID is required")
        @Size(
                max = 150,
                message = "External event ID cannot exceed 150 characters"
        )
        String externalEventId,

        @NotBlank(message = "Event type is required")
        @Size(
                max = 100,
                message = "Event type cannot exceed 100 characters"
        )
        String eventType,

        @NotBlank(message = "Payload JSON is required")
        String payloadJson

) {
}