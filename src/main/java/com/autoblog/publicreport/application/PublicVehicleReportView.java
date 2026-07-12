package com.autoblog.publicreport.application;

import com.autoblog.trust.application.TrustScoreView;
import java.util.List;

public record PublicVehicleReportView(
        PublicReportInfoView report,
        PublicVehicleView vehicle,
        PublicReportSummaryView summary,
        TrustScoreView trustScore,
        List<PublicReportEventView> events
) {
}
