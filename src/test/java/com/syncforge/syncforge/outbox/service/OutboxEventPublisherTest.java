package com.syncforge.syncforge.outbox.service;

import com.syncforge.syncforge.messaging.publisher.SyncJobPublisher;
import com.syncforge.syncforge.outbox.model.OutboxEvent;
import com.syncforge.syncforge.outbox.model.OutboxEventStatus;
import com.syncforge.syncforge.outbox.repository.OutboxEventRepository;
import com.syncforge.syncforge.tenant.model.Tenant;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OutboxEventPublisherTest {

    @Test
    void shouldPublishPendingOutboxEventAndMarkAsPublished() {
        OutboxEventRepository outboxEventRepository = mock(OutboxEventRepository.class);
        SyncJobPublisher syncJobPublisher = mock(SyncJobPublisher.class);

        OutboxEventPublisher outboxEventPublisher = new OutboxEventPublisher(
                outboxEventRepository,
                syncJobPublisher
        );

        Tenant tenant = mock(Tenant.class);
        when(tenant.getId()).thenReturn(1L);

        OutboxEvent outboxEvent = new OutboxEvent(
                tenant,
                "SYNC_JOB",
                99L,
                "SYNC_JOB_CREATED",
                "{\"tenantId\":1,\"syncJobId\":99}"
        );

        when(outboxEventRepository.findTop20ByStatusOrderByCreatedAtAsc(
                OutboxEventStatus.PENDING
        )).thenReturn(List.of(outboxEvent));

        when(outboxEventRepository.findTop20ByStatusAndNextAttemptAtBeforeOrderByCreatedAtAsc(
                eq(OutboxEventStatus.FAILED),
                any(LocalDateTime.class)
        )).thenReturn(List.of());

        outboxEventPublisher.publishPendingOutboxEvents();

        verify(syncJobPublisher, times(1)).publishSyncJob(1L, 99L);

        assertEquals(OutboxEventStatus.PUBLISHED, outboxEvent.getStatus());
        assertNotNull(outboxEvent.getPublishedAt());
    }

    @Test
    void shouldMarkOutboxEventAsFailedWhenRabbitMqPublishFails() {
        OutboxEventRepository outboxEventRepository = mock(OutboxEventRepository.class);
        SyncJobPublisher syncJobPublisher = mock(SyncJobPublisher.class);

        OutboxEventPublisher outboxEventPublisher = new OutboxEventPublisher(
                outboxEventRepository,
                syncJobPublisher
        );

        Tenant tenant = mock(Tenant.class);
        when(tenant.getId()).thenReturn(1L);

        OutboxEvent outboxEvent = new OutboxEvent(
                tenant,
                "SYNC_JOB",
                99L,
                "SYNC_JOB_CREATED",
                "{\"tenantId\":1,\"syncJobId\":99}"
        );

        when(outboxEventRepository.findTop20ByStatusOrderByCreatedAtAsc(
                OutboxEventStatus.PENDING
        )).thenReturn(List.of(outboxEvent));

        when(outboxEventRepository.findTop20ByStatusAndNextAttemptAtBeforeOrderByCreatedAtAsc(
                eq(OutboxEventStatus.FAILED),
                any(LocalDateTime.class)
        )).thenReturn(List.of());

        doThrow(new RuntimeException("RabbitMQ unavailable"))
                .when(syncJobPublisher)
                .publishSyncJob(1L, 99L);

        outboxEventPublisher.publishPendingOutboxEvents();

        assertEquals(OutboxEventStatus.FAILED, outboxEvent.getStatus());
        assertEquals(1, outboxEvent.getAttemptCount());
        assertEquals("RabbitMQ unavailable", outboxEvent.getLastError());
        assertNotNull(outboxEvent.getNextAttemptAt());
    }
}
