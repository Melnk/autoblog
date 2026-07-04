package com.autoblog.publicreport.api.dto;

import com.autoblog.publicreport.application.PublicReportMetadataView;
import com.autoblog.publicreport.domain.PublicReportStatus;
import java.time.Instant;
import java.util.UUID;

public record PublicReportMetadataResponse(
        UUID id,
        UUID vehicleId,
        String publicToken,
        String publicUrl,
        PublicReportStatus status,
        Instant createdAt,
        Instant updatedAt
) {
    public static PublicReportMetadataResponse from(PublicReportMetadataView view) {
        return new PublicReportMetadataResponse(
                view.id(),
                view.vehicleId(),
                view.publicToken(),
                view.publicUrl(),
                view.status(),
                view.createdAt(),
                view.updatedAt()
        );
    }
}
