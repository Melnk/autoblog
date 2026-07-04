package com.autoblog.identity.api.dto;

import com.autoblog.identity.application.UserView;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String displayName
) {
    public static UserResponse from(UserView view) {
        return new UserResponse(view.id(), view.email(), view.displayName());
    }
}
