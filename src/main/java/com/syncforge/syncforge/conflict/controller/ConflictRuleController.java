package com.syncforge.syncforge.conflict.controller;

import com.syncforge.syncforge.auth.service.TenantAccessService;
import com.syncforge.syncforge.conflict.dto.ConflictRuleResponse;
import com.syncforge.syncforge.conflict.dto.CreateConflictRuleRequest;
import com.syncforge.syncforge.conflict.service.ConflictRuleService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tenants/{tenantId}/conflict-rules")
public class ConflictRuleController {

    private final ConflictRuleService conflictRuleService;
    private final TenantAccessService tenantAccessService;

    public ConflictRuleController(
            ConflictRuleService conflictRuleService,
            TenantAccessService tenantAccessService
    ) {
        this.conflictRuleService = conflictRuleService;
        this.tenantAccessService = tenantAccessService;
    }

    @PostMapping
    public ConflictRuleResponse createConflictRule(
            @PathVariable Long tenantId,
            @Valid @RequestBody CreateConflictRuleRequest request
    ) {
        tenantAccessService.requireCurrentTenant(tenantId);
        return conflictRuleService.createConflictRule(tenantId, request);
    }

    @GetMapping
    public List<ConflictRuleResponse> getConflictRulesByTenant(
            @PathVariable Long tenantId
    ) {
        tenantAccessService.requireCurrentTenant(tenantId);
        return conflictRuleService.getConflictRulesByTenant(tenantId);
    }
}
