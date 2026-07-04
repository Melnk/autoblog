package com.autoblog.access.api.dto;

import com.autoblog.access.domain.VehicleAccessRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record GrantVehicleAccessRequest(
        @Schema(
                description = "Email of an existing user. This endpoint grants access by email, not by userId.",
                example = "editor@example.com"
        )
        @NotBlank(message = "email is required")
        @Email(message = "email must be well-formed")
        String email,

        @Schema(description = "Access role to grant. OWNER cannot be granted in this endpoint.", example = "EDITOR")
        @NotNull(message = "role is required")
        VehicleAccessRole role
) {
}
