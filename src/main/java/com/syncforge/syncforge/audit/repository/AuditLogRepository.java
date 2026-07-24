package com.syncforge.syncforge.audit.repository;

import com.syncforge.syncforge.audit.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByTenantIdOrderByCreatedAtDesc(Long tenantId);

    List<AuditLog> findByWebhookEventIdOrderByCreatedAtDesc(Long webhookEventId);

    List<AuditLog> findBySyncJobIdOrderByCreatedAtDesc(Long syncJobId);
}