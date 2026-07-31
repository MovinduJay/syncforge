package com.syncforge.syncforge.entitymapping.model;

import com.syncforge.syncforge.integration.model.Integration;
import com.syncforge.syncforge.tenant.model.Tenant;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "entity_mappings",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_entity_mapping_external_id",
                        columnNames = {
                                "tenant_id",
                                "integration_id",
                                "entity_type",
                                "external_entity_id"
                        }
                ),
                @UniqueConstraint(
                        name = "uk_entity_mapping_canonical_id",
                        columnNames = {
                                "tenant_id",
                                "integration_id",
                                "entity_type",
                                "canonical_entity_id"
                        }
                )
        }
)
public class EntityMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "integration_id", nullable = false)
    private Integration integration;

    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType;

    @Column(name = "external_entity_id", nullable = false, length = 150)
    private String externalEntityId;

    @Column(name = "canonical_entity_id", nullable = false, length = 150)
    private String canonicalEntityId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public EntityMapping() {
    }

    public EntityMapping(
            Tenant tenant,
            Integration integration,
            String entityType,
            String externalEntityId,
            String canonicalEntityId
    ) {
        this.tenant = tenant;
        this.integration = integration;
        this.entityType = entityType;
        this.externalEntityId = externalEntityId;
        this.canonicalEntityId = canonicalEntityId;
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

    public Integration getIntegration() {
        return integration;
    }

    public String getEntityType() {
        return entityType;
    }

    public String getExternalEntityId() {
        return externalEntityId;
    }

    public String getCanonicalEntityId() {
        return canonicalEntityId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
