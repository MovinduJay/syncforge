package com.syncforge.syncforge.tenant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTenantRequest(

        @NotBlank(message = "Company name is required")
        @Size(
                max = 150,
                message = "Company name cannot exceed 150 characters"
        )
        String companyName

) {
}