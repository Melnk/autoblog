package com.autoblog.publicreport.application;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PublicReportSummaryView(
        int eventsCount,
        LocalDate firstEventDate,
        LocalDate lastEventDate,
        Integer latestOdometerKm,
        BigDecimal totalKnownCostAmount,
        String costCurrency,
        boolean hashChainValid
) {
}
