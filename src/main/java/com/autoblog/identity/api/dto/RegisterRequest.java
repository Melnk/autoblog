package com.autoblog.identity.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank
        String email,

        @NotBlank
        @Size(min = 8)
        String password,

        String displayName
) {
}
