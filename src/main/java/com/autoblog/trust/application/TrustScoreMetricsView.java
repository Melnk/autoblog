package com.autoblog.trust.application;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TrustScoreMetricsView(
        int eventsCount,
        int eventsWithAttachmentsCount,
        int publicAttachmentsCount,
        int privateAttachmentsCount,
        int odometerEventsCount,
        Integer latestOdometerKm,
        BigDecimal totalKnownCostAmount,
        boolean hashChainValid,
        boolean odometerConsistent,
        LocalDate firstEventDate,
        LocalDate lastEventDate,
        int activeRemindersCount,
        int overdueRemindersCount
) {
}
