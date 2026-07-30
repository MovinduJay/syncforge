package com.syncforge.syncforge.conflict.service;

import com.syncforge.syncforge.conflict.dto.ConflictRuleResponse;
import com.syncforge.syncforge.conflict.dto.CreateConflictRuleRequest;
import com.syncforge.syncforge.conflict.model.ConflictRule;
import com.syncforge.syncforge.conflict.repository.ConflictRuleRepository;
import com.syncforge.syncforge.tenant.model.Tenant;
import com.syncforge.syncforge.tenant.repository.TenantRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ConflictRuleService {

    private final ConflictRuleRepository conflictRuleRepository;
    private final TenantRepository tenantRepository;

    public ConflictRuleService(
            ConflictRuleRepository conflictRuleRepository,
            TenantRepository tenantRepository
    ) {
        this.conflictRuleRepository = conflictRuleRepository;
        this.tenantRepository = tenantRepository;
    }

    @Transactional
    public ConflictRuleResponse createConflictRule(
            Long tenantId,
            CreateConflictRuleRequest request
    ) {

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Tenant not found"
                ));

        String normalizedEntityType = request.entityType().trim().toUpperCase();
        String normalizedFieldName = request.fieldName().trim();

        boolean ruleExists = conflictRuleRepository
                .findByTenantIdAndEntityTypeAndFieldNameAndActiveTrue(
                        tenantId,
                        normalizedEntityType,
                        normalizedFieldName
                )
                .isPresent();

        if (ruleExists) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Active conflict rule already exists for this entity field"
            );
        }

        ConflictRule conflictRule = new ConflictRule(
                tenant,
                normalizedEntityType,
                normalizedFieldName,
                request.owningIntegrationType(),
                request.strategy()
        );

        try {
            ConflictRule savedConflictRule = conflictRuleRepository.saveAndFlush(conflictRule);
            return toResponse(savedConflictRule);
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Conflict rule already exists"
            );
        }
    }

    @Transactional(readOnly = true)
    public List<ConflictRuleResponse> getConflictRulesByTenant(Long tenantId) {

        boolean tenantExists = tenantRepository.existsById(tenantId);

        if (!tenantExists) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Tenant not found"
            );
        }

        return conflictRuleRepository.findByTenantIdOrderByCreatedAtDesc(tenantId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private ConflictRuleResponse toResponse(ConflictRule conflictRule) {
        return new ConflictRuleResponse(
                conflictRule.getId(),
                conflictRule.getTenant().getId(),
                conflictRule.getEntityType(),
                conflictRule.getFieldName(),
                conflictRule.getOwningIntegrationType().name(),
                conflictRule.getStrategy().name(),
                conflictRule.isActive(),
                conflictRule.getCreatedAt()
        );
    }
}
