package com.syncforge.syncforge.webhook.service;

import com.syncforge.syncforge.integration.model.Integration;
import com.syncforge.syncforge.integration.repository.IntegrationRepository;
import com.syncforge.syncforge.tenant.repository.TenantRepository;
import com.syncforge.syncforge.webhook.dto.ReceiveWebhookRequest;
import com.syncforge.syncforge.webhook.dto.WebhookEventResponse;
import com.syncforge.syncforge.webhook.model.WebhookEvent;
import com.syncforge.syncforge.webhook.model.WebhookEventStatus;
import com.syncforge.syncforge.webhook.repository.WebhookEventRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.syncforge.syncforge.syncjob.service.SyncJobService;

import java.util.List;

@Service
public class WebhookEventService {

    private final WebhookEventRepository webhookEventRepository;
    private final IntegrationRepository integrationRepository;
    private final TenantRepository tenantRepository;
    private final SyncJobService syncJobService;

    public WebhookEventService(
            WebhookEventRepository webhookEventRepository,
            IntegrationRepository integrationRepository,
            TenantRepository tenantRepository,
            SyncJobService syncJobService
    ) {
        this.webhookEventRepository = webhookEventRepository;
        this.integrationRepository = integrationRepository;
        this.tenantRepository = tenantRepository;
        this.syncJobService=syncJobService;
    }

    @Transactional
    public WebhookEventResponse receiveWebhook(
            Long integrationId,
            ReceiveWebhookRequest request
    ) {

        Integration integration = integrationRepository.findById(integrationId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Integration not found"
                ));

        return webhookEventRepository
                .findByIntegrationIdAndExternalEventId(
                        integrationId,
                        request.externalEventId()
                )
                .map(this::toDuplicateResponse)
                .orElseGet(() -> createNewWebhookEvent(integration, request));
    }

    @Transactional(readOnly = true)
    public List<WebhookEventResponse> getWebhookEventsByTenant(Long tenantId) {

        boolean tenantExists = tenantRepository.existsById(tenantId);

        if (!tenantExists) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Tenant not found"
            );
        }

        return webhookEventRepository.findByTenantId(tenantId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private WebhookEventResponse createNewWebhookEvent(
            Integration integration,
            ReceiveWebhookRequest request
    ) {

        WebhookEvent webhookEvent = new WebhookEvent(
                integration.getTenant(),
                integration,
                request.externalEventId(),
                request.eventType(),
                request.payloadJson()
        );

        try {
            WebhookEvent savedWebhookEvent = webhookEventRepository.saveAndFlush(webhookEvent);

            syncJobService.createJobsForWebhookEvent(savedWebhookEvent);

            return toResponse(savedWebhookEvent);
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Webhook event already received"
            );
        }
    }

    private WebhookEventResponse toDuplicateResponse(WebhookEvent webhookEvent) {
        return toResponse(webhookEvent, WebhookEventStatus.DUPLICATE.name());
    }

    private WebhookEventResponse toResponse(WebhookEvent webhookEvent) {
        return toResponse(webhookEvent, webhookEvent.getStatus().name());
    }

    private WebhookEventResponse toResponse(
            WebhookEvent webhookEvent,
            String status
    ) {
        return new WebhookEventResponse(
                webhookEvent.getId(),
                webhookEvent.getTenant().getId(),
                webhookEvent.getIntegration().getId(),
                webhookEvent.getExternalEventId(),
                webhookEvent.getEventType(),
                status,
                webhookEvent.getReceivedAt(),
                webhookEvent.getProcessedAt()
        );
    }
}