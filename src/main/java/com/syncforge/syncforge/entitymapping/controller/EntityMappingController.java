package com.syncforge.syncforge.entitymapping.controller;

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

    public EntityMappingController(EntityMappingService entityMappingService) {
        this.entityMappingService = entityMappingService;
    }

    @PostMapping
    public EntityMappingResponse createEntityMapping(
            @PathVariable Long tenantId,
            @Valid @RequestBody CreateEntityMappingRequest request
    ) {
        return entityMappingService.createEntityMapping(tenantId, request);
    }

    @GetMapping
    public List<EntityMappingResponse> getEntityMappingsByTenant(
            @PathVariable Long tenantId
    ) {
        return entityMappingService.getEntityMappingsByTenant(tenantId);
    }
}
