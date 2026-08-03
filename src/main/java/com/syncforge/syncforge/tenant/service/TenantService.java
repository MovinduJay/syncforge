package com.syncforge.syncforge.tenant.service;

import com.syncforge.syncforge.tenant.dto.CreateTenantRequest;
import com.syncforge.syncforge.tenant.dto.TenantResponse;
import com.syncforge.syncforge.tenant.model.Tenant;
import com.syncforge.syncforge.tenant.repository.TenantRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class TenantService {

    private final TenantRepository tenantRepository;

    public TenantService(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    public TenantResponse createTenant(CreateTenantRequest request) {
        Tenant tenant = new Tenant(request.companyName());

        Tenant savedTenant = tenantRepository.save(tenant);

        return toResponse(savedTenant);
    }

    public TenantResponse getTenantById(Long tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Tenant not found"
                ));

        return toResponse(tenant);
    }

    public List<TenantResponse> getAllTenants() {
        return tenantRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private TenantResponse toResponse(Tenant tenant) {
        return new TenantResponse(
                tenant.getId(),
                tenant.getCompanyName(),
                tenant.getStatus().name(),
                tenant.getCreatedAt()
        );
    }
}
