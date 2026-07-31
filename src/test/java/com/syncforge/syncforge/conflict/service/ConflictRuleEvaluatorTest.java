package com.syncforge.syncforge.conflict.service;

import com.syncforge.syncforge.conflict.model.ConflictRule;
import com.syncforge.syncforge.conflict.model.ConflictStrategy;
import com.syncforge.syncforge.conflict.repository.ConflictRuleRepository;
import com.syncforge.syncforge.integration.model.Integration;
import com.syncforge.syncforge.integration.model.IntegrationType;
import com.syncforge.syncforge.syncjob.model.SyncJob;
import com.syncforge.syncforge.tenant.model.Tenant;
import com.syncforge.syncforge.webhook.model.WebhookEvent;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConflictRuleEvaluatorTest {

    @Test
    void shouldAllowUpdateWhenSourceIntegrationOwnsField() {
        ConflictRuleRepository conflictRuleRepository = mock(ConflictRuleRepository.class);

        ConflictRuleEvaluator evaluator = new ConflictRuleEvaluator(
                conflictRuleRepository,
                new ObjectMapper()
        );

        Tenant tenant = mock(Tenant.class);
        Integration sourceIntegration = mock(Integration.class);
        WebhookEvent webhookEvent = mock(WebhookEvent.class);
        SyncJob syncJob = mock(SyncJob.class);

        when(tenant.getId()).thenReturn(1L);
        when(sourceIntegration.getType()).thenReturn(IntegrationType.CRM);

        when(webhookEvent.getPayloadJson())
                .thenReturn("{\"customerId\":\"CRM-101\",\"email\":\"new@email.com\"}");

        when(syncJob.getTenant()).thenReturn(tenant);
        when(syncJob.getEntityType()).thenReturn("CUSTOMER");
        when(syncJob.getSourceIntegration()).thenReturn(sourceIntegration);
        when(syncJob.getWebhookEvent()).thenReturn(webhookEvent);

        ConflictRule emailRule = new ConflictRule(
                tenant,
                "CUSTOMER",
                "email",
                IntegrationType.CRM,
                ConflictStrategy.OWNER_WINS
        );

        when(conflictRuleRepository.findByTenantIdAndEntityTypeAndFieldNameAndActiveTrue(
                1L,
                "CUSTOMER",
                "email"
        )).thenReturn(Optional.of(emailRule));

        ConflictEvaluationResult result = evaluator.evaluate(syncJob);

        assertTrue(result.allowed());
        assertEquals("Conflict rules passed", result.message());
    }

    @Test
    void shouldBlockUpdateWhenSourceIntegrationDoesNotOwnField() {
        ConflictRuleRepository conflictRuleRepository = mock(ConflictRuleRepository.class);

        ConflictRuleEvaluator evaluator = new ConflictRuleEvaluator(
                conflictRuleRepository,
                new ObjectMapper()
        );

        Tenant tenant = mock(Tenant.class);
        Integration sourceIntegration = mock(Integration.class);
        WebhookEvent webhookEvent = mock(WebhookEvent.class);
        SyncJob syncJob = mock(SyncJob.class);

        when(tenant.getId()).thenReturn(1L);
        when(sourceIntegration.getType()).thenReturn(IntegrationType.SUPPORT);

        when(webhookEvent.getPayloadJson())
                .thenReturn("{\"customerId\":\"SUP-554\",\"email\":\"support@email.com\"}");

        when(syncJob.getTenant()).thenReturn(tenant);
        when(syncJob.getEntityType()).thenReturn("CUSTOMER");
        when(syncJob.getSourceIntegration()).thenReturn(sourceIntegration);
        when(syncJob.getWebhookEvent()).thenReturn(webhookEvent);

        ConflictRule emailRule = new ConflictRule(
                tenant,
                "CUSTOMER",
                "email",
                IntegrationType.CRM,
                ConflictStrategy.OWNER_WINS
        );

        when(conflictRuleRepository.findByTenantIdAndEntityTypeAndFieldNameAndActiveTrue(
                1L,
                "CUSTOMER",
                "email"
        )).thenReturn(Optional.of(emailRule));

        ConflictEvaluationResult result = evaluator.evaluate(syncJob);

        assertFalse(result.allowed());
        assertEquals(
                "Field email is owned by CRM, but source was SUPPORT",
                result.message()
        );
    }
}
