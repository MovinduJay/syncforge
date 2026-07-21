package com.syncforge.syncforge.webhook.repository;

import com.syncforge.syncforge.webhook.model.WebhookEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WebhookEventRepository extends JpaRepository<WebhookEvent, Long> {

    Optional<WebhookEvent> findByIntegrationIdAndExternalEventId(
            Long integrationId,
            String externalEventId
    );

    boolean existsByIntegrationIdAndExternalEventId(
            Long integrationId,
            String externalEventId
    );

    List<WebhookEvent> findByTenantId(Long tenantId);
}