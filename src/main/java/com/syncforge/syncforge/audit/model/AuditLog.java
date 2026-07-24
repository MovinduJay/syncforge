package com.syncforge.syncforge.audit.model;

import com.syncforge.syncforge.syncjob.model.SyncJob;
import com.syncforge.syncforge.tenant.model.Tenant;
import com.syncforge.syncforge.webhook.model.WebhookEvent;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "webhook_event_id")
    private WebhookEvent webhookEvent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sync_job_id")
    private SyncJob syncJob;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AuditAction action;

    @Column(nullable = false, length = 255)
    private String message;

    @Column(name = "details_json", columnDefinition = "TEXT")
    private String detailsJson;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public AuditLog() {
    }

    public AuditLog(
            Tenant tenant,
            WebhookEvent webhookEvent,
            SyncJob syncJob,
            AuditAction action,
            String message,
            String detailsJson
    ) {
        this.tenant = tenant;
        this.webhookEvent = webhookEvent;
        this.syncJob = syncJob;
        this.action = action;
        this.message = message;
        this.detailsJson = detailsJson;
    }

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
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

    public SyncJob getSyncJob() {
        return syncJob;
    }

    public AuditAction getAction() {
        return action;
    }

    public String getMessage() {
        return message;
    }

    public String getDetailsJson() {
        return detailsJson;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}