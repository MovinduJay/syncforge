package com.syncforge.syncforge.tenant.controller;

import com.syncforge.syncforge.auth.service.CurrentUserService;
import com.syncforge.syncforge.tenant.dto.CreateTenantRequest;
import com.syncforge.syncforge.tenant.dto.TenantResponse;
import com.syncforge.syncforge.tenant.service.TenantService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tenants")
public class TenantController {

    private final TenantService tenantService;
    private final CurrentUserService currentUserService;

    public TenantController(
            TenantService tenantService,
            CurrentUserService currentUserService
    ) {
        this.tenantService = tenantService;
        this.currentUserService = currentUserService;
    }

    @PostMapping
    public TenantResponse createTenant(@Valid @RequestBody CreateTenantRequest request) {
        return tenantService.createTenant(request);
    }

    @GetMapping
    public List<TenantResponse> getCurrentTenant() {
        Long currentTenantId = currentUserService.getCurrentTenantId();
        return List.of(tenantService.getTenantById(currentTenantId));
    }
}
