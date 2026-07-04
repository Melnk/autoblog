package com.autoblog.publicreport.application;

import com.autoblog.application.VehicleEventType;
import com.autoblog.attachment.application.PublicAttachmentView;
import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

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
        String eventHash,
        List<PublicAttachmentView> attachments
) {
}
