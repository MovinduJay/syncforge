package com.syncforge.syncforge.integration.service;

import com.syncforge.syncforge.integration.dto.CreateIntegrationRequest;
import com.syncforge.syncforge.integration.dto.IntegrationResponse;
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
public class IntegrationService {

    private final IntegrationRepository integrationRepository;
    private final TenantRepository tenantRepository;

    public IntegrationService(
            IntegrationRepository integrationRepository,
            TenantRepository tenantRepository
    ) {
        this.integrationRepository = integrationRepository;
        this.tenantRepository = tenantRepository;
    }

    @Transactional
    public IntegrationResponse createIntegration(
            Long tenantId,
            CreateIntegrationRequest request
    ) {

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Tenant not found"
                ));

        boolean alreadyExists = integrationRepository.existsByTenantIdAndType(
                tenantId,
                request.type()
        );

        if (alreadyExists) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Integration type already exists for this tenant"
            );
        }

        Integration integration = new Integration(
                tenant,
                request.type(),
                request.displayName()
        );

        try {
            Integration savedIntegration = integrationRepository.save(integration);
            return toResponse(savedIntegration);
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Integration type already exists for this tenant"
            );
        }
    }

    @Transactional(readOnly = true)
    public List<IntegrationResponse> getIntegrationsByTenant(Long tenantId) {

        boolean tenantExists = tenantRepository.existsById(tenantId);

        if (!tenantExists) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Tenant not found"
            );
        }

        return integrationRepository.findByTenantId(tenantId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private IntegrationResponse toResponse(Integration integration) {

        return new IntegrationResponse(
                integration.getId(),
                integration.getTenant().getId(),
                integration.getType().name(),
                integration.getDisplayName(),
                integration.getStatus().name(),
                integration.getCreatedAt()
        );
    }
}
