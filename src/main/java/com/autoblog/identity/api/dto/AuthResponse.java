package com.autoblog.identity.api.dto;

import com.autoblog.identity.application.AuthView;

public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresInSeconds,
        UserResponse user
) {
    public static AuthResponse from(AuthView view) {
        return new AuthResponse(
                view.accessToken(),
                view.tokenType(),
                view.expiresInSeconds(),
                UserResponse.from(view.user())
        );
    }
}
