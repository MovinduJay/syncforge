package com.syncforge.syncforge.conflict.model;

import com.syncforge.syncforge.integration.model.IntegrationType;
import com.syncforge.syncforge.tenant.model.Tenant;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "conflict_rules",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_conflict_rule_tenant_entity_field",
                        columnNames = {"tenant_id", "entity_type", "field_name"}
                )
        }
)
public class ConflictRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType;

    @Column(name = "field_name", nullable = false, length = 100)
    private String fieldName;

    @Enumerated(EnumType.STRING)
    @Column(name = "owning_integration_type", nullable = false, length = 30)
    private IntegrationType owningIntegrationType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ConflictStrategy strategy;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public ConflictRule() {
    }

    public ConflictRule(
            Tenant tenant,
            String entityType,
            String fieldName,
            IntegrationType owningIntegrationType,
            ConflictStrategy strategy
    ) {
        this.tenant = tenant;
        this.entityType = entityType;
        this.fieldName = fieldName;
        this.owningIntegrationType = owningIntegrationType;
        this.strategy = strategy;
        this.active = true;
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

    public String getEntityType() {
        return entityType;
    }

    public String getFieldName() {
        return fieldName;
    }

    public IntegrationType getOwningIntegrationType() {
        return owningIntegrationType;
    }

    public ConflictStrategy getStrategy() {
        return strategy;
    }

    public boolean isActive() {
        return active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
