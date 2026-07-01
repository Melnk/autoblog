package com.autoblog.application;

import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record AddVehicleEventCommand(
        UUID vehicleId,
        VehicleEventType type,
        LocalDate eventDate,
        Integer odometerKm,
        String title,
        String description,
        BigDecimal costAmount,
        String costCurrency,
        String serviceName,
        JsonNode payload
) {
}
