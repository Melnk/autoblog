package com.autoblog.trust.application;

import com.autoblog.trust.domain.TrustSignalImpact;

public record TrustScoreSignalView(
        String code,
        TrustSignalImpact impact,
        int points,
        String message
) {
}
