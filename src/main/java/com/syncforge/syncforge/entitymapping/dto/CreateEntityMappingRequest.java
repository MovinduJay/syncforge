package com.syncforge.syncforge.entitymapping.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateEntityMappingRequest(

        @NotNull(message = "Integration ID is required")
        Long integrationId,

        @NotBlank(message = "Entity type is required")
        @Size(max = 50, message = "Entity type cannot exceed 50 characters")
        String entityType,

        @NotBlank(message = "External entity ID is required")
        @Size(max = 150, message = "External entity ID cannot exceed 150 characters")
        String externalEntityId,

        @NotBlank(message = "Canonical entity ID is required")
        @Size(max = 150, message = "Canonical entity ID cannot exceed 150 characters")
        String canonicalEntityId

) {
}
