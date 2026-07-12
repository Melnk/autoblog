package com.autoblog.trust.api.dto;

import com.autoblog.trust.application.TrustScoreSignalView;
import com.autoblog.trust.domain.TrustSignalImpact;

public record TrustScoreSignalResponse(
        String code,
        TrustSignalImpact impact,
        int points,
        String message
) {
    public static TrustScoreSignalResponse from(TrustScoreSignalView signal) {
        return new TrustScoreSignalResponse(
                signal.code(),
                signal.impact(),
                signal.points(),
                signal.message()
        );
    }
}
