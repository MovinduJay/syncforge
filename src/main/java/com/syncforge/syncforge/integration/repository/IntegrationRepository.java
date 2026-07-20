package com.syncforge.syncforge.integration.repository;

import com.syncforge.syncforge.integration.model.Integration;
import com.syncforge.syncforge.integration.model.IntegrationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IntegrationRepository extends JpaRepository<Integration, Long> {

    List<Integration> findByTenantId(Long tenantId);

    boolean existsByTenantIdAndType(Long tenantId, IntegrationType type);
}