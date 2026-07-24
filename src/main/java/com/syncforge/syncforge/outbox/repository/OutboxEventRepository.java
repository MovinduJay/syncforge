package com.syncforge.syncforge.outbox.repository;

import com.syncforge.syncforge.outbox.model.OutboxEvent;
import com.syncforge.syncforge.outbox.model.OutboxEventStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    List<OutboxEvent> findTop20ByStatusOrderByCreatedAtAsc(
            OutboxEventStatus status
    );

    List<OutboxEvent> findTop20ByStatusAndNextAttemptAtBeforeOrderByCreatedAtAsc(
            OutboxEventStatus status,
            LocalDateTime now
    );

    List<OutboxEvent> findByTenantIdOrderByCreatedAtDesc(Long tenantId);
}