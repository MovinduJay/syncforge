package com.syncforge.syncforge.syncjob.model;

import com.syncforge.syncforge.integration.model.Integration;
import com.syncforge.syncforge.tenant.model.Tenant;
import com.syncforge.syncforge.webhook.model.WebhookEvent;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "sync_jobs",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_sync_jobs_webhook_target",
                        columnNames = {"webhook_event_id", "target_integration_id"}
                )
        }
)
public class SyncJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "webhook_event_id", nullable = false)
    private WebhookEvent webhookEvent;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "source_integration_id", nullable = false)
    private Integration sourceIntegration;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "target_integration_id", nullable = false)
    private Integration targetIntegration;

    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType;

    @Column(name = "operation_type", nullable = false, length = 50)
    private String operationType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SyncJobStatus status = SyncJobStatus.PENDING;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount = 0;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    @Column(name = "next_retry_at")
    private LocalDateTime nextRetryAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    public SyncJob() {
    }

    public SyncJob(
            Tenant tenant,
            WebhookEvent webhookEvent,
            Integration sourceIntegration,
            Integration targetIntegration,
            String entityType,
            String operationType
    ) {
        this.tenant = tenant;
        this.webhookEvent = webhookEvent;
        this.sourceIntegration = sourceIntegration;
        this.targetIntegration = targetIntegration;
        this.entityType = entityType;
        this.operationType = operationType;
        this.status = SyncJobStatus.PENDING;
    }

    @PrePersist
    public void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void markProcessing() {
        this.status = SyncJobStatus.PROCESSING;
    }

    public void markSucceeded() {
        this.status = SyncJobStatus.SUCCEEDED;
        this.processedAt = LocalDateTime.now();
        this.lastError = null;
        this.nextRetryAt = null;
    }

    public void markFailed(String errorMessage, LocalDateTime nextRetryAt) {
        this.status = SyncJobStatus.FAILED;
        this.attemptCount++;
        this.lastError = errorMessage;
        this.nextRetryAt = nextRetryAt;
    }

    public void markDeadLetter(String errorMessage) {
        this.status = SyncJobStatus.DEAD_LETTER;
        this.attemptCount++;
        this.lastError = errorMessage;
        this.nextRetryAt = null;
        this.processedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Tenant getTenant() {
        return tenant;
    }

    public WebhookEvent getWebhookEvent() {
        return webhookEvent;
    }

    public Integration getSourceIntegration() {
        return sourceIntegration;
    }

    public Integration getTargetIntegration() {
        return targetIntegration;
    }

    public String getEntityType() {
        return entityType;
    }

    public String getOperationType() {
        return operationType;
    }

    public SyncJobStatus getStatus() {
        return status;
    }

    public int getAttemptCount() {
        return attemptCount;
    }

    public String getLastError() {
        return lastError;
    }

    public LocalDateTime getNextRetryAt() {
        return nextRetryAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }
}