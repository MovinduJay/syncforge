package com.syncforge.syncforge.syncjob.controller;

import com.syncforge.syncforge.syncjob.dto.ProcessSyncJobRequest;
import com.syncforge.syncforge.syncjob.dto.SyncJobResponse;
import com.syncforge.syncforge.syncjob.service.SyncJobService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tenants/{tenantId}/sync-jobs")
public class SyncJobController {

    private final SyncJobService syncJobService;

    public SyncJobController(SyncJobService syncJobService) {
        this.syncJobService = syncJobService;
    }

    @GetMapping
    public List<SyncJobResponse> getSyncJobsByTenant(
            @PathVariable Long tenantId
    ) {
        return syncJobService.getSyncJobsByTenant(tenantId);
    }

    @PostMapping("/{jobId}/process")
    public SyncJobResponse processSyncJob(
            @PathVariable Long tenantId,
            @PathVariable Long jobId,
            @RequestBody ProcessSyncJobRequest request
    ) {
        return syncJobService.processSyncJob(tenantId, jobId, request);
    }
}