package com.autoblog.api.dto;

import com.autoblog.application.VehicleEventType;
import com.autoblog.application.VehicleEventView;
import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record VehicleEventResponse(
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
    public static VehicleEventResponse from(VehicleEventView event) {
        return new VehicleEventResponse(
                event.id(),
                event.vehicleId(),
                event.sequenceNumber(),
                event.type(),
                event.eventDate(),
                event.odometerKm(),
                event.title(),
                event.description(),
                event.costAmount(),
                event.costCurrency(),
                event.serviceName(),
                event.payload(),
                event.previousEventHash(),
                event.eventHash(),
                event.createdAt()
        );
    }
}
