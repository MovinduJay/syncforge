package com.syncforge.syncforge.integration.controller;

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

    public IntegrationController(IntegrationService integrationService) {
        this.integrationService = integrationService;
    }

    @PostMapping
    public IntegrationResponse createIntegration(
            @PathVariable Long tenantId,
            @Valid @RequestBody CreateIntegrationRequest request
    ) {
        return integrationService.createIntegration(tenantId, request);
    }

    @GetMapping
    public List<IntegrationResponse> getIntegrationsByTenant(
            @PathVariable Long tenantId
    ) {
        return integrationService.getIntegrationsByTenant(tenantId);
    }
}