package com.syncforge.syncforge.audit.controller;

import com.syncforge.syncforge.audit.dto.AuditLogResponse;
import com.syncforge.syncforge.audit.service.AuditLogService;
import com.syncforge.syncforge.auth.service.TenantAccessService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tenants/{tenantId}/audit-logs")
public class AuditLogController {

    private final AuditLogService auditLogService;
    private final TenantAccessService tenantAccessService;

    public AuditLogController(
            AuditLogService auditLogService,
            TenantAccessService tenantAccessService
    ) {
        this.auditLogService = auditLogService;
        this.tenantAccessService = tenantAccessService;
    }

    @GetMapping
    public List<AuditLogResponse> getAuditLogsByTenant(
            @PathVariable Long tenantId
    ) {
        tenantAccessService.requireCurrentTenant(tenantId);
        return auditLogService.getAuditLogsByTenant(tenantId);
    }
}
