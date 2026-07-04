package com.autoblog.api.dto;

import com.autoblog.application.VehicleEventType;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Append-only vehicle event creation request. Required fields: type, eventDate, title.")
public record CreateVehicleEventRequest(
        @NotNull(message = "Event type is required")
        @Schema(example = "MAINTENANCE", requiredMode = Schema.RequiredMode.REQUIRED)
        VehicleEventType type,

        @NotNull(message = "Event date is required")
        @Schema(example = "2026-07-02", requiredMode = Schema.RequiredMode.REQUIRED)
        LocalDate eventDate,

        @PositiveOrZero(message = "Odometer must be zero or positive")
        @Schema(description = "Canonical queryable odometer value. Send this top-level, not inside payload.", example = "120000")
        Integer odometerKm,

        @NotBlank(message = "Title is required")
        @Size(max = 240, message = "Title must be 240 characters or fewer")
        @Schema(example = "Замена масла", requiredMode = Schema.RequiredMode.REQUIRED)
        String title,

        @Size(max = 2000, message = "Description must be 2000 characters or fewer")
        @Schema(example = "Масло 5W-40, масляный фильтр")
        String description,

        @DecimalMin(value = "0.00", message = "Cost amount must be zero or positive")
        @Schema(description = "Canonical queryable cost amount. Send this top-level, not inside payload.", example = "5000")
        BigDecimal costAmount,

        @Size(min = 3, max = 3, message = "Cost currency must be a 3-letter code")
        @Schema(description = "Canonical queryable cost currency. Defaults to RUB when omitted.", example = "RUB")
        String costCurrency,

        @Size(max = 240, message = "Service name must be 240 characters or fewer")
        @Schema(description = "Canonical queryable service name. Send this top-level, not inside payload.", example = "Гаражный сервис")
        String serviceName,

        @Schema(description = "Extension data only for event-specific details.", example = "{\"oil\":\"5W-40\",\"parts\":[\"oil_filter\"]}")
        JsonNode payload
) {
}
