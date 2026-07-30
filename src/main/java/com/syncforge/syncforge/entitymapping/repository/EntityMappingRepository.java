package com.syncforge.syncforge.entitymapping.repository;

import com.syncforge.syncforge.entitymapping.model.EntityMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EntityMappingRepository extends JpaRepository<EntityMapping, Long> {

    Optional<EntityMapping> findByTenantIdAndIntegrationIdAndEntityTypeAndExternalEntityId(
            Long tenantId,
            Long integrationId,
            String entityType,
            String externalEntityId
    );

    Optional<EntityMapping> findByTenantIdAndIntegrationIdAndEntityTypeAndCanonicalEntityId(
            Long tenantId,
            Long integrationId,
            String entityType,
            String canonicalEntityId
    );

    List<EntityMapping> findByTenantId(Long tenantId);

    List<EntityMapping> findByTenantIdAndCanonicalEntityId(
            Long tenantId,
            String canonicalEntityId
    );
}
