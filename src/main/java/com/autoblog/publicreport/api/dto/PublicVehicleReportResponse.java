package com.autoblog.publicreport.api.dto;

import com.autoblog.publicreport.application.PublicVehicleReportView;
import java.util.List;

public record PublicVehicleReportResponse(
        PublicReportInfoResponse report,
        PublicVehicleResponse vehicle,
        PublicReportSummaryResponse summary,
        List<PublicReportEventResponse> events
) {
    public static PublicVehicleReportResponse from(PublicVehicleReportView view) {
        return new PublicVehicleReportResponse(
                PublicReportInfoResponse.from(view.report()),
                PublicVehicleResponse.from(view.vehicle()),
                PublicReportSummaryResponse.from(view.summary()),
                view.events().stream()
                        .map(PublicReportEventResponse::from)
                        .toList()
        );
    }
}
