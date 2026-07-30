package com.syncforge.syncforge.syncjob.service;

import com.syncforge.syncforge.audit.service.AuditLogService;
import com.syncforge.syncforge.conflict.service.ConflictEvaluationResult;
import com.syncforge.syncforge.conflict.service.ConflictRuleEvaluator;
import com.syncforge.syncforge.entitymapping.service.EntityMappingService;
import com.syncforge.syncforge.integration.connector.IntegrationConnectorRegistry;
import com.syncforge.syncforge.integration.model.Integration;
import com.syncforge.syncforge.integration.repository.IntegrationRepository;
import com.syncforge.syncforge.outbox.service.OutboxEventService;
import com.syncforge.syncforge.syncjob.dto.ProcessSyncJobRequest;
import com.syncforge.syncforge.syncjob.model.SyncJob;
import com.syncforge.syncforge.syncjob.model.SyncJobStatus;
import com.syncforge.syncforge.syncjob.repository.SyncJobRepository;
import com.syncforge.syncforge.tenant.model.Tenant;
import com.syncforge.syncforge.webhook.model.WebhookEvent;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SyncJobServiceTest {

    @Test
    void shouldMoveJobToDeadLetterAfterThreeFailures() {
        SyncJobRepository syncJobRepository = mock(SyncJobRepository.class);
        IntegrationRepository integrationRepository = mock(IntegrationRepository.class);
        OutboxEventService outboxEventService = mock(OutboxEventService.class);
        AuditLogService auditLogService = mock(AuditLogService.class);
        IntegrationConnectorRegistry connectorRegistry = mock(IntegrationConnectorRegistry.class);
        EntityMappingService entityMappingService = mock(EntityMappingService.class);
        ConflictRuleEvaluator conflictRuleEvaluator = mock(ConflictRuleEvaluator.class);

        SyncJobService syncJobService = new SyncJobService(
                syncJobRepository,
                integrationRepository,
                outboxEventService,
                auditLogService,
                connectorRegistry,
                entityMappingService,
                conflictRuleEvaluator
        );

        Tenant tenant = mock(Tenant.class);
        WebhookEvent webhookEvent = mock(WebhookEvent.class);
        Integration sourceIntegration = mock(Integration.class);
        Integration targetIntegration = mock(Integration.class);

        when(tenant.getId()).thenReturn(1L);
        when(webhookEvent.getId()).thenReturn(10L);
        when(sourceIntegration.getId()).thenReturn(1L);
        when(targetIntegration.getId()).thenReturn(2L);

        SyncJob syncJob = new SyncJob(
                tenant,
                webhookEvent,
                sourceIntegration,
                targetIntegration,
                "CUSTOMER",
                "UPDATE"
        );

        when(syncJobRepository.findById(99L)).thenReturn(Optional.of(syncJob));
        when(conflictRuleEvaluator.evaluate(syncJob))
                .thenReturn(new ConflictEvaluationResult(true, "Allowed"));

        ProcessSyncJobRequest failureRequest = new ProcessSyncJobRequest(
                true,
                "Billing API timeout"
        );

        syncJobService.processSyncJob(1L, 99L, failureRequest);

        assertEquals(SyncJobStatus.FAILED, syncJob.getStatus());
        assertEquals(1, syncJob.getAttemptCount());
        assertEquals("Billing API timeout", syncJob.getLastError());
        assertNotNull(syncJob.getNextRetryAt());

        syncJobService.processSyncJob(1L, 99L, failureRequest);

        assertEquals(SyncJobStatus.FAILED, syncJob.getStatus());
        assertEquals(2, syncJob.getAttemptCount());

        syncJobService.processSyncJob(1L, 99L, failureRequest);

        assertEquals(SyncJobStatus.DEAD_LETTER, syncJob.getStatus());
        assertEquals(3, syncJob.getAttemptCount());
        assertEquals("Billing API timeout", syncJob.getLastError());
        assertNull(syncJob.getNextRetryAt());
        assertNotNull(syncJob.getProcessedAt());

        verify(auditLogService, times(2)).logSyncJobFailed(syncJob);
        verify(auditLogService, times(1)).logSyncJobDeadLetter(syncJob);
    }
}
