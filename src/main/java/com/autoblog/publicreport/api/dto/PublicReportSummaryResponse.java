package com.autoblog.publicreport.api.dto;

import com.autoblog.publicreport.application.PublicReportSummaryView;
import java.math.BigDecimal;
import java.time.LocalDate;

public record PublicReportSummaryResponse(
        int eventsCount,
        LocalDate firstEventDate,
        LocalDate lastEventDate,
        Integer latestOdometerKm,
        BigDecimal totalKnownCostAmount,
        String costCurrency,
        boolean hashChainValid
) {
    public static PublicReportSummaryResponse from(PublicReportSummaryView view) {
        return new PublicReportSummaryResponse(
                view.eventsCount(),
                view.firstEventDate(),
                view.lastEventDate(),
                view.latestOdometerKm(),
                view.totalKnownCostAmount(),
                view.costCurrency(),
                view.hashChainValid()
        );
    }
}
