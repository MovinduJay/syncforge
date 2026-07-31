package com.syncforge.syncforge.conflict.dto;

import com.syncforge.syncforge.conflict.model.ConflictStrategy;
import com.syncforge.syncforge.integration.model.IntegrationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateConflictRuleRequest(

        @NotBlank(message = "Entity type is required")
        @Size(max = 50, message = "Entity type cannot exceed 50 characters")
        String entityType,

        @NotBlank(message = "Field name is required")
        @Size(max = 100, message = "Field name cannot exceed 100 characters")
        String fieldName,

        @NotNull(message = "Owning integration type is required")
        IntegrationType owningIntegrationType,

        @NotNull(message = "Conflict strategy is required")
        ConflictStrategy strategy

) {
}
