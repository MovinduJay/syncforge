package com.syncforge.syncforge.syncjob.service;

import com.syncforge.syncforge.integration.model.Integration;
import com.syncforge.syncforge.integration.model.IntegrationStatus;
import com.syncforge.syncforge.integration.repository.IntegrationRepository;
import com.syncforge.syncforge.syncjob.dto.ProcessSyncJobRequest;
import com.syncforge.syncforge.syncjob.dto.SyncJobResponse;
import com.syncforge.syncforge.syncjob.model.SyncJob;
import com.syncforge.syncforge.syncjob.model.SyncJobStatus;
import com.syncforge.syncforge.syncjob.repository.SyncJobRepository;
import com.syncforge.syncforge.webhook.model.WebhookEvent;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.syncforge.syncforge.messaging.publisher.SyncJobPublisher;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SyncJobService {

    private static final int MAX_RETRY_ATTEMPTS = 3;

    private final SyncJobRepository syncJobRepository;
    private final IntegrationRepository integrationRepository;
    private final SyncJobPublisher syncJobPublisher;

    public SyncJobService(
            SyncJobRepository syncJobRepository,
            IntegrationRepository integrationRepository,
            SyncJobPublisher syncJobPublisher
    ) {
        this.syncJobRepository = syncJobRepository;
        this.integrationRepository = integrationRepository;
        this.syncJobPublisher= syncJobPublisher;
    }

    @Transactional
    public List<SyncJobResponse> createJobsForWebhookEvent(WebhookEvent webhookEvent) {

        List<SyncJob> existingJobs = syncJobRepository.findByWebhookEventId(
                webhookEvent.getId()
        );

        if (!existingJobs.isEmpty()) {
            return existingJobs.stream()
                    .map(this::toResponse)
                    .toList();
        }

        Integration sourceIntegration = webhookEvent.getIntegration();

        List<Integration> targetIntegrations = integrationRepository
                .findByTenantId(webhookEvent.getTenant().getId())
                .stream()
                .filter(integration -> !integration.getId().equals(sourceIntegration.getId()))
                .filter(integration -> integration.getStatus() == IntegrationStatus.ACTIVE)
                .toList();

        String entityType = resolveEntityType(webhookEvent.getEventType());
        String operationType = resolveOperationType(webhookEvent.getEventType());

        List<SyncJob> syncJobs = targetIntegrations.stream()
                .map(targetIntegration -> new SyncJob(
                        webhookEvent.getTenant(),
                        webhookEvent,
                        sourceIntegration,
                        targetIntegration,
                        entityType,
                        operationType
                ))
                .toList();

        List<SyncJob> savedSyncJobs = syncJobRepository.saveAll(syncJobs);

        savedSyncJobs.forEach(syncJob ->
                syncJobPublisher.publishSyncJob(
                        syncJob.getTenant().getId(),
                        syncJob.getId()
                )
        );

        return savedSyncJobs.stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public SyncJobResponse processSyncJob(
            Long tenantId,
            Long jobId,
            ProcessSyncJobRequest request
    ) {
        return processSyncJobInternal(
                tenantId,
                jobId,
                request.simulateFailure(),
                request.errorMessage()
        );
    }

    @Transactional
    public SyncJobResponse processSyncJobFromQueue(
            Long tenantId,
            Long jobId
    ) {
        return processSyncJobInternal(
                tenantId,
                jobId,
                false,
                null
        );
    }

    private SyncJobResponse processSyncJobInternal(
            Long tenantId,
            Long jobId,
            boolean simulateFailure,
            String errorMessage
    ) {

        SyncJob syncJob = syncJobRepository.findById(jobId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Sync job not found"
                ));

        if (!syncJob.getTenant().getId().equals(tenantId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Sync job not found for this tenant"
            );
        }

        if (syncJob.getStatus() == SyncJobStatus.SUCCEEDED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Sync job already succeeded"
            );
        }

        if (syncJob.getStatus() == SyncJobStatus.DEAD_LETTER) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Sync job is already in dead letter state"
            );
        }

        syncJob.markProcessing();

        if (simulateFailure) {
            handleFailedJob(syncJob, errorMessage);
        } else {
            syncJob.markSucceeded();
        }

        return toResponse(syncJob);
    }

    @Transactional(readOnly = true)
    public List<SyncJobResponse> getSyncJobsByTenant(Long tenantId) {
        return syncJobRepository.findByTenantId(tenantId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private void handleFailedJob(
            SyncJob syncJob,
            String errorMessage
    ) {

        String finalErrorMessage = resolveErrorMessage(errorMessage);

        if (syncJob.getAttemptCount() + 1 >= MAX_RETRY_ATTEMPTS) {
            syncJob.markDeadLetter(finalErrorMessage);
            return;
        }

        LocalDateTime nextRetryAt = LocalDateTime.now().plusMinutes(5);

        syncJob.markFailed(finalErrorMessage, nextRetryAt);
    }

    private String resolveErrorMessage(String errorMessage) {
        if (errorMessage == null || errorMessage.isBlank()) {
            return "Sync job processing failed";
        }

        return errorMessage;
    }

    private String resolveEntityType(String eventType) {
        int underscoreIndex = eventType.indexOf("_");

        if (underscoreIndex == -1) {
            return "UNKNOWN";
        }

        return eventType.substring(0, underscoreIndex);
    }

    private String resolveOperationType(String eventType) {
        if (eventType.endsWith("_CREATED")) {
            return "CREATE";
        }

        if (eventType.endsWith("_UPDATED")) {
            return "UPDATE";
        }

        if (eventType.endsWith("_DELETED")) {
            return "DELETE";
        }

        return "UNKNOWN";
    }

    private SyncJobResponse toResponse(SyncJob syncJob) {
        return new SyncJobResponse(
                syncJob.getId(),
                syncJob.getTenant().getId(),
                syncJob.getWebhookEvent().getId(),
                syncJob.getSourceIntegration().getId(),
                syncJob.getTargetIntegration().getId(),
                syncJob.getEntityType(),
                syncJob.getOperationType(),
                syncJob.getStatus().name(),
                syncJob.getAttemptCount(),
                syncJob.getLastError(),
                syncJob.getNextRetryAt(),
                syncJob.getCreatedAt(),
                syncJob.getUpdatedAt(),
                syncJob.getProcessedAt()
        );
    }
}