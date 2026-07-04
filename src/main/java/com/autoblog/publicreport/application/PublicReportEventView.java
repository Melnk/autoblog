package com.autoblog.publicreport.application;

import com.autoblog.application.VehicleEventType;
import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigDecimal;
import java.time.LocalDate;

public record PublicReportEventView(
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
        String eventHash
) {
}
