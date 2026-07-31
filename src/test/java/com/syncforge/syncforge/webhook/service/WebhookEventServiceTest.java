package com.syncforge.syncforge.webhook.service;

import com.syncforge.syncforge.audit.service.AuditLogService;
import com.syncforge.syncforge.integration.model.Integration;
import com.syncforge.syncforge.integration.model.IntegrationType;
import com.syncforge.syncforge.integration.repository.IntegrationRepository;
import com.syncforge.syncforge.syncjob.service.SyncJobService;
import com.syncforge.syncforge.tenant.model.Tenant;
import com.syncforge.syncforge.tenant.repository.TenantRepository;
import com.syncforge.syncforge.webhook.dto.ReceiveWebhookRequest;
import com.syncforge.syncforge.webhook.dto.WebhookEventResponse;
import com.syncforge.syncforge.webhook.model.WebhookEvent;
import com.syncforge.syncforge.webhook.repository.WebhookEventRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class WebhookEventServiceTest {

    @Test
    void shouldReturnDuplicateWithoutCreatingSyncJobsWhenWebhookAlreadyExists() {
        WebhookEventRepository webhookEventRepository = mock(WebhookEventRepository.class);
        IntegrationRepository integrationRepository = mock(IntegrationRepository.class);
        TenantRepository tenantRepository = mock(TenantRepository.class);
        SyncJobService syncJobService = mock(SyncJobService.class);
        AuditLogService auditLogService = mock(AuditLogService.class);
        WebhookEventCreationService webhookEventCreationService = mock(WebhookEventCreationService.class);

        WebhookEventService webhookEventService = new WebhookEventService(
                webhookEventRepository,
                integrationRepository,
                tenantRepository,
                syncJobService,
                auditLogService,
                webhookEventCreationService
        );

        Tenant tenant = mock(Tenant.class);
        Integration integration = mock(Integration.class);
        WebhookEvent existingWebhookEvent = mock(WebhookEvent.class);

        when(tenant.getId()).thenReturn(1L);

        when(integration.getId()).thenReturn(1L);
        when(integration.getTenant()).thenReturn(tenant);
        when(integration.getType()).thenReturn(IntegrationType.CRM);

        when(existingWebhookEvent.getId()).thenReturn(10L);
        when(existingWebhookEvent.getTenant()).thenReturn(tenant);
        when(existingWebhookEvent.getIntegration()).thenReturn(integration);
        when(existingWebhookEvent.getExternalEventId()).thenReturn("evt-duplicate-1");
        when(existingWebhookEvent.getEventType()).thenReturn("CUSTOMER_UPDATED");
        when(existingWebhookEvent.getReceivedAt()).thenReturn(null);
        when(existingWebhookEvent.getProcessedAt()).thenReturn(null);

        ReceiveWebhookRequest request = new ReceiveWebhookRequest(
                "evt-duplicate-1",
                "CUSTOMER_UPDATED",
                "{\"customerId\":\"CRM-101\",\"email\":\"test@email.com\"}"
        );

        when(integrationRepository.findById(1L))
                .thenReturn(Optional.of(integration));

        when(webhookEventRepository.findByIntegrationIdAndExternalEventId(
                1L,
                "evt-duplicate-1"
        )).thenReturn(Optional.of(existingWebhookEvent));

        WebhookEventResponse response = webhookEventService.receiveWebhook(
                1L,
                request
        );

        assertEquals("DUPLICATE", response.status());
        assertEquals("evt-duplicate-1", response.externalEventId());

        verify(webhookEventRepository, never()).saveAndFlush(any(WebhookEvent.class));
        verify(syncJobService, never()).createJobsForWebhookEvent(any(WebhookEvent.class));
        verify(auditLogService, times(1)).logWebhookDuplicate(existingWebhookEvent);
    }
}
