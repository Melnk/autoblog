package com.autoblog.identity.application;

public record AuthView(
        String accessToken,
        String tokenType,
        long expiresInSeconds,
        UserView user
) {
}
