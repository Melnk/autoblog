package com.autoblog.publicreport.application;

import java.util.List;

public record PublicVehicleReportView(
        PublicReportInfoView report,
        PublicVehicleView vehicle,
        PublicReportSummaryView summary,
        List<PublicReportEventView> events
) {
}
