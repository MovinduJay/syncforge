package com.syncforge.syncforge.outbox.service;

import com.syncforge.syncforge.outbox.model.OutboxEvent;
import com.syncforge.syncforge.outbox.repository.OutboxEventRepository;
import com.syncforge.syncforge.syncjob.model.SyncJob;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OutboxEventService {

    private final OutboxEventRepository outboxEventRepository;

    public OutboxEventService(OutboxEventRepository outboxEventRepository) {
        this.outboxEventRepository = outboxEventRepository;
    }

    @Transactional
    public void createSyncJobCreatedEvent(SyncJob syncJob) {

        String payloadJson = """
                {"tenantId":%d,"syncJobId":%d}
                """.formatted(
                syncJob.getTenant().getId(),
                syncJob.getId()
        );

        OutboxEvent outboxEvent = new OutboxEvent(
                syncJob.getTenant(),
                "SYNC_JOB",
                syncJob.getId(),
                "SYNC_JOB_CREATED",
                payloadJson
        );

        outboxEventRepository.save(outboxEvent);
    }
}
