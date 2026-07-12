package com.autoblog.trust.application;

import com.autoblog.trust.domain.TrustScoreLevel;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record TrustScoreView(
        UUID vehicleId,
        int score,
        TrustScoreLevel level,
        Instant calculatedAt,
        String summary,
        List<TrustScoreSignalView> signals,
        TrustScoreMetricsView metrics
) {
}
