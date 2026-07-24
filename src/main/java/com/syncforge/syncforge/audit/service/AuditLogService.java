package com.syncforge.syncforge.audit.service;

import com.syncforge.syncforge.audit.dto.AuditLogResponse;
import com.syncforge.syncforge.audit.model.AuditAction;
import com.syncforge.syncforge.audit.model.AuditLog;
import com.syncforge.syncforge.audit.repository.AuditLogRepository;
import com.syncforge.syncforge.syncjob.model.SyncJob;
import com.syncforge.syncforge.tenant.model.Tenant;
import com.syncforge.syncforge.webhook.model.WebhookEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void logWebhookReceived(WebhookEvent webhookEvent) {
        save(
                webhookEvent.getTenant(),
                webhookEvent,
                null,
                AuditAction.WEBHOOK_RECEIVED,
                "Webhook event received",
                "{\"externalEventId\":\"" + webhookEvent.getExternalEventId() + "\"}"
        );
    }

    @Transactional
    public void logWebhookDuplicate(WebhookEvent webhookEvent) {
        save(
                webhookEvent.getTenant(),
                webhookEvent,
                null,
                AuditAction.WEBHOOK_DUPLICATE,
                "Duplicate webhook event received",
                "{\"externalEventId\":\"" + webhookEvent.getExternalEventId() + "\"}"
        );
    }

    @Transactional
    public void logSyncJobCreated(SyncJob syncJob) {
        save(
                syncJob.getTenant(),
                syncJob.getWebhookEvent(),
                syncJob,
                AuditAction.SYNC_JOB_CREATED,
                "Sync job created",
                "{\"targetIntegrationId\":" + syncJob.getTargetIntegration().getId() + "}"
        );
    }

    @Transactional
    public void logSyncJobProcessing(SyncJob syncJob) {
        save(
                syncJob.getTenant(),
                syncJob.getWebhookEvent(),
                syncJob,
                AuditAction.SYNC_JOB_PROCESSING,
                "Sync job processing started",
                null
        );
    }

    @Transactional
    public void logSyncJobSucceeded(SyncJob syncJob) {
        save(
                syncJob.getTenant(),
                syncJob.getWebhookEvent(),
                syncJob,
                AuditAction.SYNC_JOB_SUCCEEDED,
                "Sync job completed successfully",
                null
        );
    }

    @Transactional
    public void logSyncJobFailed(SyncJob syncJob) {
        save(
                syncJob.getTenant(),
                syncJob.getWebhookEvent(),
                syncJob,
                AuditAction.SYNC_JOB_FAILED,
                "Sync job failed",
                "{\"attemptCount\":" + syncJob.getAttemptCount() + "}"
        );
    }

    @Transactional
    public void logSyncJobDeadLetter(SyncJob syncJob) {
        save(
                syncJob.getTenant(),
                syncJob.getWebhookEvent(),
                syncJob,
                AuditAction.SYNC_JOB_DEAD_LETTER,
                "Sync job moved to dead letter",
                "{\"attemptCount\":" + syncJob.getAttemptCount() + "}"
        );
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> getAuditLogsByTenant(Long tenantId) {
        return auditLogRepository.findByTenantIdOrderByCreatedAtDesc(tenantId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private void save(
            Tenant tenant,
            WebhookEvent webhookEvent,
            SyncJob syncJob,
            AuditAction action,
            String message,
            String detailsJson
    ) {
        AuditLog auditLog = new AuditLog(
                tenant,
                webhookEvent,
                syncJob,
                action,
                message,
                detailsJson
        );

        auditLogRepository.save(auditLog);
    }

    private AuditLogResponse toResponse(AuditLog auditLog) {
        Long webhookEventId = auditLog.getWebhookEvent() == null
                ? null
                : auditLog.getWebhookEvent().getId();

        Long syncJobId = auditLog.getSyncJob() == null
                ? null
                : auditLog.getSyncJob().getId();

        return new AuditLogResponse(
                auditLog.getId(),
                auditLog.getTenant().getId(),
                webhookEventId,
                syncJobId,
                auditLog.getAction().name(),
                auditLog.getMessage(),
                auditLog.getDetailsJson(),
                auditLog.getCreatedAt()
        );
    }
}