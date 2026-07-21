package com.syncforge.syncforge.webhook.controller;

import com.syncforge.syncforge.webhook.dto.ReceiveWebhookRequest;
import com.syncforge.syncforge.webhook.dto.WebhookEventResponse;
import com.syncforge.syncforge.webhook.service.WebhookEventService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class WebhookEventController {

    private final WebhookEventService webhookEventService;

    public WebhookEventController(WebhookEventService webhookEventService) {
        this.webhookEventService = webhookEventService;
    }

    @PostMapping("/api/integrations/{integrationId}/webhooks")
    public WebhookEventResponse receiveWebhook(
            @PathVariable Long integrationId,
            @Valid @RequestBody ReceiveWebhookRequest request
    ) {
        return webhookEventService.receiveWebhook(integrationId, request);
    }

    @GetMapping("/api/tenants/{tenantId}/webhook-events")
    public List<WebhookEventResponse> getWebhookEventsByTenant(
            @PathVariable Long tenantId
    ) {
        return webhookEventService.getWebhookEventsByTenant(tenantId);
    }
}