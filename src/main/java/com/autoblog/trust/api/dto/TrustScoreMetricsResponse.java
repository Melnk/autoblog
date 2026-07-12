package com.autoblog.trust.api.dto;

import com.autoblog.trust.application.TrustScoreMetricsView;
import java.math.BigDecimal;
import java.time.LocalDate;

public record TrustScoreMetricsResponse(
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
    public static TrustScoreMetricsResponse from(TrustScoreMetricsView metrics) {
        return new TrustScoreMetricsResponse(
                metrics.eventsCount(),
                metrics.eventsWithAttachmentsCount(),
                metrics.publicAttachmentsCount(),
                metrics.privateAttachmentsCount(),
                metrics.odometerEventsCount(),
                metrics.latestOdometerKm(),
                metrics.totalKnownCostAmount(),
                metrics.hashChainValid(),
                metrics.odometerConsistent(),
                metrics.firstEventDate(),
                metrics.lastEventDate(),
                metrics.activeRemindersCount(),
                metrics.overdueRemindersCount()
        );
    }
}
