package com.autoblog.publicreport.application;

import com.autoblog.publicreport.domain.PublicReportStatus;
import java.time.Instant;
import java.util.UUID;

public record PublicReportInfoView(
        UUID id,
        String publicToken,
        PublicReportStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
