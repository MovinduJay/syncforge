package com.syncforge.syncforge.audit.controller;

import com.syncforge.syncforge.audit.dto.AuditLogResponse;
import com.syncforge.syncforge.audit.service.AuditLogService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tenants/{tenantId}/audit-logs")
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public List<AuditLogResponse> getAuditLogsByTenant(
            @PathVariable Long tenantId
    ) {
        return auditLogService.getAuditLogsByTenant(tenantId);
    }
}