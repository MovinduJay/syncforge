package com.syncforge.syncforge.conflict.service;

import com.syncforge.syncforge.conflict.model.ConflictRule;
import com.syncforge.syncforge.conflict.model.ConflictStrategy;
import com.syncforge.syncforge.conflict.repository.ConflictRuleRepository;
import com.syncforge.syncforge.syncjob.model.SyncJob;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.Set;

@Service
public class ConflictRuleEvaluator {

    private static final Set<String> SYSTEM_FIELDS = Set.of(
            "customerId",
            "failTarget"
    );

    private final ConflictRuleRepository conflictRuleRepository;
    private final ObjectMapper objectMapper;

    public ConflictRuleEvaluator(
            ConflictRuleRepository conflictRuleRepository,
            ObjectMapper objectMapper
    ) {
        this.conflictRuleRepository = conflictRuleRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public ConflictEvaluationResult evaluate(SyncJob syncJob) {

        Map<?, ?> payloadFields = readPayload(syncJob.getWebhookEvent().getPayloadJson());

        for (Object fieldKey : payloadFields.keySet()) {
            String fieldName = String.valueOf(fieldKey);

            if (SYSTEM_FIELDS.contains(fieldName)) {
                continue;
            }

            ConflictEvaluationResult result = evaluateField(syncJob, fieldName);

            if (!result.allowed()) {
                return result;
            }
        }

        return new ConflictEvaluationResult(
                true,
                "Conflict rules passed"
        );
    }

    private ConflictEvaluationResult evaluateField(
            SyncJob syncJob,
            String fieldName
    ) {

        return conflictRuleRepository
                .findByTenantIdAndEntityTypeAndFieldNameAndActiveTrue(
                        syncJob.getTenant().getId(),
                        syncJob.getEntityType(),
                        fieldName
                )
                .map(conflictRule -> applyRule(syncJob, conflictRule))
                .orElseGet(() -> new ConflictEvaluationResult(
                        true,
                        "No conflict rule configured for field: " + fieldName
                ));
    }

    private ConflictEvaluationResult applyRule(
            SyncJob syncJob,
            ConflictRule conflictRule
    ) {

        if (conflictRule.getStrategy() == ConflictStrategy.SOURCE_WINS) {
            return new ConflictEvaluationResult(
                    true,
                    "Source wins rule allowed update"
            );
        }

        if (conflictRule.getStrategy() == ConflictStrategy.IGNORE) {
            return new ConflictEvaluationResult(
                    false,
                    "Field ignored by conflict rule: " + conflictRule.getFieldName()
            );
        }

        if (conflictRule.getStrategy() == ConflictStrategy.MANUAL_REVIEW) {
            return new ConflictEvaluationResult(
                    false,
                    "Manual review required for field: " + conflictRule.getFieldName()
            );
        }

        if (conflictRule.getStrategy() == ConflictStrategy.OWNER_WINS) {
            boolean sourceIsOwner = syncJob.getSourceIntegration()
                    .getType()
                    .equals(conflictRule.getOwningIntegrationType());

            if (sourceIsOwner) {
                return new ConflictEvaluationResult(
                        true,
                        "Owning integration allowed update"
                );
            }

            return new ConflictEvaluationResult(
                    false,
                    "Field " + conflictRule.getFieldName()
                            + " is owned by " + conflictRule.getOwningIntegrationType()
                            + ", but source was " + syncJob.getSourceIntegration().getType()
            );
        }

        return new ConflictEvaluationResult(
                false,
                "Unsupported conflict strategy"
        );
    }

    private Map<?, ?> readPayload(String payloadJson) {
        try {
            return objectMapper.readValue(payloadJson, Map.class);
        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Failed to read webhook payload for conflict evaluation",
                    exception
            );
        }
    }
}
