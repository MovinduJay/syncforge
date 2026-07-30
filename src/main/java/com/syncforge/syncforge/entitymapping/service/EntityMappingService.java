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
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.syncforge.syncforge.syncjob.model.SyncJob;

import java.util.List;

@Service
public class EntityMappingService {

    private final EntityMappingRepository entityMappingRepository;
    private final TenantRepository tenantRepository;
    private final IntegrationRepository integrationRepository;
    private final ObjectMapper objectMapper;

    public EntityMappingService(
            EntityMappingRepository entityMappingRepository,
            TenantRepository tenantRepository,
            IntegrationRepository integrationRepository,
            ObjectMapper objectMapper
    ) {
        this.entityMappingRepository = entityMappingRepository;
        this.tenantRepository = tenantRepository;
        this.integrationRepository = integrationRepository;
        this.objectMapper = objectMapper;
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

    @Transactional(readOnly = true)
    public ResolvedEntityMapping resolveForSyncJob(SyncJob syncJob) {

        String sourceExternalEntityId = extractSourceExternalEntityId(
                syncJob.getWebhookEvent().getPayloadJson(),
                syncJob.getEntityType()
        );

        EntityMapping sourceMapping = entityMappingRepository
                .findByTenantIdAndIntegrationIdAndEntityTypeAndExternalEntityId(
                        syncJob.getTenant().getId(),
                        syncJob.getSourceIntegration().getId(),
                        syncJob.getEntityType(),
                        sourceExternalEntityId
                )
                .orElseThrow(() -> new IllegalStateException(
                        "Source entity mapping not found for external ID: " + sourceExternalEntityId
                ));

        EntityMapping targetMapping = entityMappingRepository
                .findByTenantIdAndIntegrationIdAndEntityTypeAndCanonicalEntityId(
                        syncJob.getTenant().getId(),
                        syncJob.getTargetIntegration().getId(),
                        syncJob.getEntityType(),
                        sourceMapping.getCanonicalEntityId()
                )
                .orElseThrow(() -> new IllegalStateException(
                        "Target entity mapping not found for canonical ID: "
                                + sourceMapping.getCanonicalEntityId()
                ));

        return new ResolvedEntityMapping(
                sourceExternalEntityId,
                targetMapping.getExternalEntityId(),
                sourceMapping.getCanonicalEntityId()
        );
    }
    private String extractSourceExternalEntityId(
            String payloadJson,
            String entityType
    ) {
        try {
            JsonNode rootNode = objectMapper.readTree(payloadJson);

            if ("CUSTOMER".equals(entityType)) {
                JsonNode customerIdNode = rootNode.get("customerId");

                if (customerIdNode == null || customerIdNode.asText().isBlank()) {
                    throw new IllegalStateException("customerId is missing from webhook payload");
                }

                return customerIdNode.asText();
            }

            throw new IllegalStateException("Unsupported entity type: " + entityType);

        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Failed to extract source entity ID from webhook payload",
                    exception
            );
        }
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
