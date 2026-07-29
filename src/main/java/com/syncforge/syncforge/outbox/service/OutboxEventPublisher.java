package com.syncforge.syncforge.outbox.service;

import com.syncforge.syncforge.messaging.publisher.SyncJobPublisher;
import com.syncforge.syncforge.outbox.model.OutboxEvent;
import com.syncforge.syncforge.outbox.model.OutboxEventStatus;
import com.syncforge.syncforge.outbox.repository.OutboxEventRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OutboxEventPublisher {

    private final OutboxEventRepository outboxEventRepository;
    private final SyncJobPublisher syncJobPublisher;

    public OutboxEventPublisher(
            OutboxEventRepository outboxEventRepository,
            SyncJobPublisher syncJobPublisher
    ) {
        this.outboxEventRepository = outboxEventRepository;
        this.syncJobPublisher = syncJobPublisher;
    }

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void publishPendingOutboxEvents() {

        List<OutboxEvent> eventsToPublish = new ArrayList<>();

        eventsToPublish.addAll(
                outboxEventRepository.findTop20ByStatusOrderByCreatedAtAsc(
                        OutboxEventStatus.PENDING
                )
        );

        eventsToPublish.addAll(
                outboxEventRepository.findTop20ByStatusAndNextAttemptAtBeforeOrderByCreatedAtAsc(
                        OutboxEventStatus.FAILED,
                        LocalDateTime.now()
                )
        );

        eventsToPublish.forEach(this::publishOutboxEvent);
    }

    private void publishOutboxEvent(OutboxEvent outboxEvent) {

        try {
            if ("SYNC_JOB_CREATED".equals(outboxEvent.getEventType())) {
                syncJobPublisher.publishSyncJob(
                        outboxEvent.getTenant().getId(),
                        outboxEvent.getAggregateId()
                );

                outboxEvent.markPublished();
                return;
            }

            outboxEvent.markFailed("Unsupported outbox event type: " + outboxEvent.getEventType());

        } catch (Exception exception) {
            outboxEvent.markFailed(resolveErrorMessage(exception));
        }
    }

    private String resolveErrorMessage(Exception exception) {
        String message = exception.getMessage();

        if (message == null || message.isBlank()) {
            return exception.getClass().getSimpleName();
        }

        return message;
    }
}
