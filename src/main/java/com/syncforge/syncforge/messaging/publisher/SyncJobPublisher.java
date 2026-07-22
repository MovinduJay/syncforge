package com.syncforge.syncforge.messaging.publisher;

import com.syncforge.syncforge.messaging.config.RabbitMQConfig;
import com.syncforge.syncforge.messaging.dto.SyncJobMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class SyncJobPublisher {

    private final RabbitTemplate rabbitTemplate;

    public SyncJobPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishSyncJob(Long tenantId, Long syncJobId) {
        SyncJobMessage message = new SyncJobMessage(
                tenantId,
                syncJobId
        );

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.SYNC_JOB_EXCHANGE,
                RabbitMQConfig.SYNC_JOB_ROUTING_KEY,
                message
        );
    }
}