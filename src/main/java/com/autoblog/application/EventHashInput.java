package com.autoblog.application;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record EventHashInput(
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
        String payloadCanonicalJson,
        String previousEventHash
) {
}
