package com.autoblog.identity.application;

import java.util.UUID;

public record UserView(
        UUID id,
        String email,
        String displayName
) {
}
