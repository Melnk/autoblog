package com.autoblog.trust.api.dto;

import com.autoblog.trust.application.TrustScoreView;
import com.autoblog.trust.domain.TrustScoreLevel;
import java.time.Instant;
import java.util.List;

public record PublicTrustScoreResponse(
        int score,
        TrustScoreLevel level,
        Instant calculatedAt,
        String summary,
        List<TrustScoreSignalResponse> signals,
        TrustScoreMetricsResponse metrics
) {
    public static PublicTrustScoreResponse from(TrustScoreView view) {
        return new PublicTrustScoreResponse(
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
