package com.syncforge.syncforge.entitymapping.service;

import com.syncforge.syncforge.entitymapping.dto.CreateEntityMappingRequest;
import com.syncforge.syncforge.entitymapping.dto.EntityMappingResponse;
import com.syncforge.syncforge.entitymapping.model.EntityMapping;
import com.syncforge.syncforge.entitymapping.repository.EntityMappingRepository;
import com.syncforge.syncforge.integration.model.Integration;
import com.syncforge.syncforge.integration.repository.IntegrationRepository;
import com.syncforge.syncforge.tenant.model.Tenant;
import com.syncforge.syncforge.tenant.repository.TenantRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class EntityMappingService {

    private final EntityMappingRepository entityMappingRepository;
    private final TenantRepository tenantRepository;
    private final IntegrationRepository integrationRepository;

    public EntityMappingService(
            EntityMappingRepository entityMappingRepository,
            TenantRepository tenantRepository,
            IntegrationRepository integrationRepository
    ) {
        this.entityMappingRepository = entityMappingRepository;
        this.tenantRepository = tenantRepository;
        this.integrationRepository = integrationRepository;
    }

    @Transactional
    public EntityMappingResponse createEntityMapping(
            Long tenantId,
            CreateEntityMappingRequest request
    ) {

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Tenant not found"
                ));

        Integration integration = integrationRepository.findById(request.integrationId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Integration not found"
                ));

        if (!integration.getTenant().getId().equals(tenantId)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Integration does not belong to this tenant"
            );
        }

        String normalizedEntityType = request.entityType().trim().toUpperCase();

        boolean externalMappingExists = entityMappingRepository
                .findByTenantIdAndIntegrationIdAndEntityTypeAndExternalEntityId(
                        tenantId,
                        request.integrationId(),
                        normalizedEntityType,
                        request.externalEntityId().trim()
                )
                .isPresent();

        if (externalMappingExists) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "External entity ID is already mapped"
            );
        }

        boolean canonicalMappingExists = entityMappingRepository
                .findByTenantIdAndIntegrationIdAndEntityTypeAndCanonicalEntityId(
                        tenantId,
                        request.integrationId(),
                        normalizedEntityType,
                        request.canonicalEntityId().trim()
                )
                .isPresent();

        if (canonicalMappingExists) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Canonical entity ID is already mapped for this integration"
            );
        }

        EntityMapping entityMapping = new EntityMapping(
                tenant,
                integration,
                normalizedEntityType,
                request.externalEntityId().trim(),
                request.canonicalEntityId().trim()
        );

        try {
            EntityMapping savedEntityMapping = entityMappingRepository.saveAndFlush(entityMapping);
            return toResponse(savedEntityMapping);
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Entity mapping already exists"
            );
        }
    }

    @Transactional(readOnly = true)
    public List<EntityMappingResponse> getEntityMappingsByTenant(Long tenantId) {

        boolean tenantExists = tenantRepository.existsById(tenantId);

        if (!tenantExists) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Tenant not found"
            );
        }

        return entityMappingRepository.findByTenantId(tenantId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private EntityMappingResponse toResponse(EntityMapping entityMapping) {
        return new EntityMappingResponse(
                entityMapping.getId(),
                entityMapping.getTenant().getId(),
                entityMapping.getIntegration().getId(),
                entityMapping.getIntegration().getType().name(),
                entityMapping.getEntityType(),
                entityMapping.getExternalEntityId(),
                entityMapping.getCanonicalEntityId(),
                entityMapping.getCreatedAt()
        );
    }
}
