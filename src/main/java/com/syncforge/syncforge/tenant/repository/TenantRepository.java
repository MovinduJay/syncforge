package com.syncforge.syncforge.tenant.repository;

import com.syncforge.syncforge.tenant.model.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantRepository extends JpaRepository<Tenant, Long> {
}