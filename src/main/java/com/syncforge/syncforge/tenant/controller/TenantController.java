package com.syncforge.syncforge.tenant.controller;

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

    public TenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @PostMapping
    public TenantResponse createTenant(@Valid @RequestBody CreateTenantRequest request) {
        return tenantService.createTenant(request);
    }

    @GetMapping
    public List<TenantResponse> getAllTenants() {
        return tenantService.getAllTenants();
    }
}