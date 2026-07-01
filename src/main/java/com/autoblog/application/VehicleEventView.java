package com.autoblog.application;

import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record VehicleEventView(
        UUID id,
        UUID vehicleId,
        long sequenceNumber,
        VehicleEventType type,
        LocalDate eventDate,
        Integer odometerKm,
        String title,
        String description,
        BigDecimal costAmount,
        String costCurrency,
        String serviceName,
        JsonNode payload,
        String previousEventHash,
        String eventHash,
        Instant createdAt
) {
}
