package com.syncforge.syncforge.syncjob.repository;

import com.syncforge.syncforge.syncjob.model.SyncJob;
import com.syncforge.syncforge.syncjob.model.SyncJobStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SyncJobRepository extends JpaRepository<SyncJob, Long> {

    List<SyncJob> findByTenantId(Long tenantId);

    List<SyncJob> findByWebhookEventId(Long webhookEventId);

    List<SyncJob> findByStatus(SyncJobStatus status);
}