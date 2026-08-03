package com.syncforge.syncforge.integration.controller;

import com.syncforge.syncforge.auth.service.TenantAccessService;
import com.syncforge.syncforge.integration.dto.CreateIntegrationRequest;
import com.syncforge.syncforge.integration.dto.IntegrationResponse;
import com.syncforge.syncforge.integration.service.IntegrationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tenants/{tenantId}/integrations")
public class IntegrationController {

    private final IntegrationService integrationService;
    private final TenantAccessService tenantAccessService;

    public IntegrationController(
            IntegrationService integrationService,
            TenantAccessService tenantAccessService
    ) {
        this.integrationService = integrationService;
        this.tenantAccessService = tenantAccessService;
    }

    @PostMapping
    public IntegrationResponse createIntegration(
            @PathVariable Long tenantId,
            @Valid @RequestBody CreateIntegrationRequest request
    ) {
        tenantAccessService.requireCurrentTenant(tenantId);
        return integrationService.createIntegration(tenantId, request);
    }

    @GetMapping
    public List<IntegrationResponse> getIntegrationsByTenant(
            @PathVariable Long tenantId
    ) {
        tenantAccessService.requireCurrentTenant(tenantId);
        return integrationService.getIntegrationsByTenant(tenantId);
    }
}
