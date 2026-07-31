package com.syncforge.syncforge.entitymapping.service;

import com.syncforge.syncforge.entitymapping.model.EntityMapping;
import com.syncforge.syncforge.entitymapping.repository.EntityMappingRepository;
import com.syncforge.syncforge.integration.model.Integration;
import com.syncforge.syncforge.integration.repository.IntegrationRepository;
import com.syncforge.syncforge.syncjob.model.SyncJob;
import com.syncforge.syncforge.tenant.model.Tenant;
import com.syncforge.syncforge.tenant.repository.TenantRepository;
import com.syncforge.syncforge.webhook.model.WebhookEvent;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class EntityMappingServiceTest {

    @Test
    void shouldResolveSourceCanonicalAndTargetEntityIdsForSyncJob() {
        EntityMappingRepository entityMappingRepository = mock(EntityMappingRepository.class);
        TenantRepository tenantRepository = mock(TenantRepository.class);
        IntegrationRepository integrationRepository = mock(IntegrationRepository.class);

        EntityMappingService entityMappingService = new EntityMappingService(
                entityMappingRepository,
                tenantRepository,
                integrationRepository,
                new ObjectMapper()
        );

        Tenant tenant = mock(Tenant.class);
        Integration sourceIntegration = mock(Integration.class);
        Integration targetIntegration = mock(Integration.class);
        WebhookEvent webhookEvent = mock(WebhookEvent.class);
        SyncJob syncJob = mock(SyncJob.class);

        when(tenant.getId()).thenReturn(1L);

        when(sourceIntegration.getId()).thenReturn(1L);
        when(targetIntegration.getId()).thenReturn(2L);

        when(webhookEvent.getPayloadJson())
                .thenReturn("{\"customerId\":\"CRM-101\",\"email\":\"new@email.com\"}");

        when(syncJob.getTenant()).thenReturn(tenant);
        when(syncJob.getSourceIntegration()).thenReturn(sourceIntegration);
        when(syncJob.getTargetIntegration()).thenReturn(targetIntegration);
        when(syncJob.getEntityType()).thenReturn("CUSTOMER");
        when(syncJob.getWebhookEvent()).thenReturn(webhookEvent);

        EntityMapping sourceMapping = new EntityMapping(
                tenant,
                sourceIntegration,
                "CUSTOMER",
                "CRM-101",
                "CUST-0001"
        );

        EntityMapping targetMapping = new EntityMapping(
                tenant,
                targetIntegration,
                "CUSTOMER",
                "BILL-882",
                "CUST-0001"
        );

        when(entityMappingRepository.findByTenantIdAndIntegrationIdAndEntityTypeAndExternalEntityId(
                1L,
                1L,
                "CUSTOMER",
                "CRM-101"
        )).thenReturn(Optional.of(sourceMapping));

        when(entityMappingRepository.findByTenantIdAndIntegrationIdAndEntityTypeAndCanonicalEntityId(
                1L,
                2L,
                "CUSTOMER",
                "CUST-0001"
        )).thenReturn(Optional.of(targetMapping));

        ResolvedEntityMapping result = entityMappingService.resolveForSyncJob(syncJob);

        assertEquals("CRM-101", result.sourceExternalEntityId());
        assertEquals("CUST-0001", result.canonicalEntityId());
        assertEquals("BILL-882", result.targetExternalEntityId());
    }
}
