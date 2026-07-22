package com.syncforge.syncforge.messaging.consumer;

import com.syncforge.syncforge.messaging.config.RabbitMQConfig;
import com.syncforge.syncforge.messaging.dto.SyncJobMessage;
import com.syncforge.syncforge.syncjob.service.SyncJobService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class SyncJobConsumer {

    private final SyncJobService syncJobService;

    public SyncJobConsumer(SyncJobService syncJobService) {
        this.syncJobService = syncJobService;
    }

    @RabbitListener(queues = RabbitMQConfig.SYNC_JOB_QUEUE)
    public void consumeSyncJob(SyncJobMessage message) {

        syncJobService.processSyncJobFromQueue(
                message.tenantId(),
                message.syncJobId()
        );
    }
}