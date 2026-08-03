package com.syncforge.syncforge.auth.service;

import com.syncforge.syncforge.integration.model.Integration;
import com.syncforge.syncforge.integration.repository.IntegrationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TenantAccessService {

    private final CurrentUserService currentUserService;
    private final IntegrationRepository integrationRepository;

    public TenantAccessService(
            CurrentUserService currentUserService,
            IntegrationRepository integrationRepository
    ) {
        this.currentUserService = currentUserService;
        this.integrationRepository = integrationRepository;
    }

    public void requireCurrentTenant(Long tenantId) {
        Long currentTenantId = currentUserService.getCurrentTenantId();

        if (!currentTenantId.equals(tenantId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot access data from another tenant"
            );
        }
    }

    public Integration requireIntegrationBelongsToCurrentTenant(Long integrationId) {
        Integration integration = integrationRepository.findById(integrationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Integration not found"));

        requireCurrentTenant(integration.getTenant().getId());

        return integration;
    }
}
