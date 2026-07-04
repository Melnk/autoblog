package com.autoblog.publicreport.api.dto;

import com.autoblog.publicreport.application.PublicReportInfoView;
import com.autoblog.publicreport.domain.PublicReportStatus;
import java.time.Instant;
import java.util.UUID;

public record PublicReportInfoResponse(
        UUID id,
        String publicToken,
        PublicReportStatus status,
        Instant createdAt,
        Instant updatedAt
) {
    public static PublicReportInfoResponse from(PublicReportInfoView view) {
        return new PublicReportInfoResponse(
                view.id(),
                view.publicToken(),
                view.status(),
                view.createdAt(),
                view.updatedAt()
        );
    }
}
