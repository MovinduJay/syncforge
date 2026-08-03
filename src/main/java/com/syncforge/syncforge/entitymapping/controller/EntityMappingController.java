package com.syncforge.syncforge.entitymapping.controller;

import com.syncforge.syncforge.auth.service.TenantAccessService;
import com.syncforge.syncforge.entitymapping.dto.CreateEntityMappingRequest;
import com.syncforge.syncforge.entitymapping.dto.EntityMappingResponse;
import com.syncforge.syncforge.entitymapping.service.EntityMappingService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tenants/{tenantId}/entity-mappings")
public class EntityMappingController {

    private final EntityMappingService entityMappingService;
    private final TenantAccessService tenantAccessService;

    public EntityMappingController(
            EntityMappingService entityMappingService,
            TenantAccessService tenantAccessService
    ) {
        this.entityMappingService = entityMappingService;
        this.tenantAccessService = tenantAccessService;
    }

    @PostMapping
    public EntityMappingResponse createEntityMapping(
            @PathVariable Long tenantId,
            @Valid @RequestBody CreateEntityMappingRequest request
    ) {
        tenantAccessService.requireCurrentTenant(tenantId);
        return entityMappingService.createEntityMapping(tenantId, request);
    }

    @GetMapping
    public List<EntityMappingResponse> getEntityMappingsByTenant(
            @PathVariable Long tenantId
    ) {
        tenantAccessService.requireCurrentTenant(tenantId);
        return entityMappingService.getEntityMappingsByTenant(tenantId);
    }
}
