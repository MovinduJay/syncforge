package com.syncforge.syncforge.conflict.controller;

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

    public ConflictRuleController(ConflictRuleService conflictRuleService) {
        this.conflictRuleService = conflictRuleService;
    }

    @PostMapping
    public ConflictRuleResponse createConflictRule(
            @PathVariable Long tenantId,
            @Valid @RequestBody CreateConflictRuleRequest request
    ) {
        return conflictRuleService.createConflictRule(tenantId, request);
    }

    @GetMapping
    public List<ConflictRuleResponse> getConflictRulesByTenant(
            @PathVariable Long tenantId
    ) {
        return conflictRuleService.getConflictRulesByTenant(tenantId);
    }
}
