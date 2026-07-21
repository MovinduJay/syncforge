package com.syncforge.syncforge.integration.dto;

import com.syncforge.syncforge.integration.model.IntegrationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateIntegrationRequest(

        @NotNull(message = "Integration type is required")
        IntegrationType type,

        @NotBlank(message = "Display name is required")
        @Size(
                max = 150,
                message = "Display name cannot exceed 150 characters"
        )
        String displayName

) {
}