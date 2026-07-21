package com.syncforge.syncforge.webhook.model;

import com.syncforge.syncforge.integration.model.Integration;
import com.syncforge.syncforge.tenant.model.Tenant;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "webhook_events",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_webhook_events_integration_external_event",
                        columnNames = {"integration_id", "external_event_id"}
                )
        }
)
public class WebhookEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "integration_id", nullable = false)
    private Integration integration;

    @Column(name = "external_event_id", nullable = false, length = 150)
    private String externalEventId;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Lob
    @Column(name = "payload_json", nullable = false, columnDefinition = "TEXT")
    private String payloadJson;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private WebhookEventStatus status = WebhookEventStatus.RECEIVED;

    @Column(name = "received_at", nullable = false)
    private LocalDateTime receivedAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    public WebhookEvent() {
    }

    public WebhookEvent(
            Tenant tenant,
            Integration integration,
            String externalEventId,
            String eventType,
            String payloadJson
    ) {
        this.tenant = tenant;
        this.integration = integration;
        this.externalEventId = externalEventId;
        this.eventType = eventType;
        this.payloadJson = payloadJson;
        this.status = WebhookEventStatus.RECEIVED;
    }

    @PrePersist
    public void onCreate() {
        this.receivedAt = LocalDateTime.now();
    }

    public void markProcessed() {
        this.status = WebhookEventStatus.PROCESSED;
        this.processedAt = LocalDateTime.now();
    }

    public void markFailed() {
        this.status = WebhookEventStatus.FAILED;
    }

    public Long getId() {
        return id;
    }

    public Tenant getTenant() {
        return tenant;
    }

    public Integration getIntegration() {
        return integration;
    }

    public String getExternalEventId() {
        return externalEventId;
    }

    public String getEventType() {
        return eventType;
    }

    public String getPayloadJson() {
        return payloadJson;
    }

    public WebhookEventStatus getStatus() {
        return status;
    }

    public LocalDateTime getReceivedAt() {
        return receivedAt;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }
}