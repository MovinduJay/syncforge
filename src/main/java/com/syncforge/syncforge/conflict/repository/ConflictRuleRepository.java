package com.syncforge.syncforge.conflict.repository;

import com.syncforge.syncforge.conflict.model.ConflictRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConflictRuleRepository extends JpaRepository<ConflictRule, Long> {

    Optional<ConflictRule> findByTenantIdAndEntityTypeAndFieldNameAndActiveTrue(
            Long tenantId,
            String entityType,
            String fieldName
    );

    List<ConflictRule> findByTenantIdOrderByCreatedAtDesc(Long tenantId);
}
