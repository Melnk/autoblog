package com.autoblog.api.dto;

import com.autoblog.application.VehicleEventType;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateVehicleEventRequest(
        @NotNull(message = "Event type is required")
        VehicleEventType type,

        @NotNull(message = "Event date is required")
        LocalDate eventDate,

        @PositiveOrZero(message = "Odometer must be zero or positive")
        Integer odometerKm,

        @NotBlank(message = "Title is required")
        @Size(max = 240, message = "Title must be 240 characters or fewer")
        String title,

        @Size(max = 2000, message = "Description must be 2000 characters or fewer")
        String description,

        @DecimalMin(value = "0.00", message = "Cost amount must be zero or positive")
        BigDecimal costAmount,

        @Size(min = 3, max = 3, message = "Cost currency must be a 3-letter code")
        String costCurrency,

        @Size(max = 240, message = "Service name must be 240 characters or fewer")
        String serviceName,

        JsonNode payload
) {
}
