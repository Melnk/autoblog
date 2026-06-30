package com.autoblog.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public record CreateVehicleEventRequest(
        @NotBlank(message = "Event type is required")
        @Size(max = 64, message = "Event type must be 64 characters or fewer")
        String eventType,

        @NotNull(message = "Occurred-at timestamp is required")
        Instant occurredAt,

        @Size(max = 2000, message = "Description must be 2000 characters or fewer")
        String description
) {
}
