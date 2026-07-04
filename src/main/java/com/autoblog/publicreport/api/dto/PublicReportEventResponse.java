package com.autoblog.publicreport.api.dto;

import com.autoblog.application.VehicleEventType;
import com.autoblog.publicreport.application.PublicReportEventView;
import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigDecimal;
import java.time.LocalDate;

public record PublicReportEventResponse(
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
    public static PublicReportEventResponse from(PublicReportEventView view) {
        return new PublicReportEventResponse(
                view.sequenceNumber(),
                view.type(),
                view.eventDate(),
                view.odometerKm(),
                view.title(),
                view.description(),
                view.costAmount(),
                view.costCurrency(),
                view.serviceName(),
                view.payload(),
                view.previousEventHash(),
                view.eventHash()
        );
    }
}
