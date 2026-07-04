package com.autoblog.publicreport.application;

import com.autoblog.publicreport.domain.PublicReportStatus;
import java.time.Instant;
import java.util.UUID;

public record PublicReportMetadataView(
        UUID id,
        UUID vehicleId,
        String publicToken,
        String publicUrl,
        PublicReportStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
