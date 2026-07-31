package com.syncforge.syncforge.webhook.service;

import com.syncforge.syncforge.webhook.model.WebhookEvent;
import com.syncforge.syncforge.webhook.repository.WebhookEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WebhookEventCreationService {

    private final WebhookEventRepository webhookEventRepository;

    public WebhookEventCreationService(WebhookEventRepository webhookEventRepository) {
        this.webhookEventRepository = webhookEventRepository;
    }

    @Transactional
    public WebhookEvent saveNewWebhookEvent(WebhookEvent webhookEvent) {
        return webhookEventRepository.saveAndFlush(webhookEvent);
    }
}
