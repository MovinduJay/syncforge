package com.syncforge.syncforge.integration.model;

import com.syncforge.syncforge.tenant.model.Tenant;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "integrations",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_integrations_tenant_type",
                        columnNames = {"tenant_id", "type"}
                )
        }
)
public class Integration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private IntegrationType type;

    @Column(name = "display_name", nullable = false, length = 150)
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private IntegrationStatus status = IntegrationStatus.ACTIVE;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Integration() {
    }

    public Integration(Tenant tenant, IntegrationType type, String displayName) {
        this.tenant = tenant;
        this.type = type;
        this.displayName = displayName;
        this.status = IntegrationStatus.ACTIVE;
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

    public Long getId() {
        return id;
    }

    public Tenant getTenant() {
        return tenant;
    }

    public IntegrationType getType() {
        return type;
    }

    public String getDisplayName() {
        return displayName;
    }

    public IntegrationStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}