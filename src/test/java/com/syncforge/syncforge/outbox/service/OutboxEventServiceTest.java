package com.syncforge.syncforge.outbox.service;

import com.syncforge.syncforge.outbox.model.OutboxEvent;
import com.syncforge.syncforge.outbox.model.OutboxEventStatus;
import com.syncforge.syncforge.outbox.repository.OutboxEventRepository;
import com.syncforge.syncforge.syncjob.model.SyncJob;
import com.syncforge.syncforge.tenant.model.Tenant;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OutboxEventServiceTest {

    @Test
    void shouldCreatePendingOutboxEventWhenSyncJobIsCreated() {
        OutboxEventRepository outboxEventRepository = mock(OutboxEventRepository.class);

        OutboxEventService outboxEventService = new OutboxEventService(
                outboxEventRepository
        );

        Tenant tenant = mock(Tenant.class);
        SyncJob syncJob = mock(SyncJob.class);

        when(tenant.getId()).thenReturn(1L);
        when(syncJob.getId()).thenReturn(99L);
        when(syncJob.getTenant()).thenReturn(tenant);

        outboxEventService.createSyncJobCreatedEvent(syncJob);

        ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);

        verify(outboxEventRepository).save(captor.capture());

        OutboxEvent savedOutboxEvent = captor.getValue();

        assertEquals(tenant, savedOutboxEvent.getTenant());
        assertEquals("SYNC_JOB", savedOutboxEvent.getAggregateType());
        assertEquals(99L, savedOutboxEvent.getAggregateId());
        assertEquals("SYNC_JOB_CREATED", savedOutboxEvent.getEventType());
        assertEquals(OutboxEventStatus.PENDING, savedOutboxEvent.getStatus());

        assertTrue(savedOutboxEvent.getPayloadJson().contains("\"tenantId\":1"));
        assertTrue(savedOutboxEvent.getPayloadJson().contains("\"syncJobId\":99"));
    }
}
