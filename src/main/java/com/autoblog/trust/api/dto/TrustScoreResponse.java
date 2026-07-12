package com.autoblog.trust.api.dto;

import com.autoblog.trust.application.TrustScoreView;
import com.autoblog.trust.domain.TrustScoreLevel;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record TrustScoreResponse(
        UUID vehicleId,
        int score,
        TrustScoreLevel level,
        Instant calculatedAt,
        String summary,
        List<TrustScoreSignalResponse> signals,
        TrustScoreMetricsResponse metrics
) {
    public static TrustScoreResponse from(TrustScoreView view) {
        return new TrustScoreResponse(
                view.vehicleId(),
                view.score(),
                view.level(),
                view.calculatedAt(),
                view.summary(),
                view.signals().stream()
                        .map(TrustScoreSignalResponse::from)
                        .toList(),
                TrustScoreMetricsResponse.from(view.metrics())
        );
    }
}
