package com.autoblog.identity.api;

import com.autoblog.identity.api.dto.AuthResponse;
import com.autoblog.identity.api.dto.LoginRequest;
import com.autoblog.identity.api.dto.RegisterRequest;
import com.autoblog.identity.api.dto.UserResponse;
import com.autoblog.identity.application.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(
            summary = "Register a user account",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = RegisterRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "email": "owner@example.com",
                                      "password": "StrongPassword123!",
                                      "displayName": "Owner"
                                    }
                                    """)
                    )
            )
    )
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return AuthResponse.from(authService.register(request.email(), request.password(), request.displayName()));
    }

    @PostMapping("/login")
    @Operation(
            summary = "Login with email and password",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = LoginRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "email": "owner@example.com",
                                      "password": "StrongPassword123!"
                                    }
                                    """)
                    )
            )
    )
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return AuthResponse.from(authService.login(request.email(), request.password()));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user", security = @SecurityRequirement(name = "bearerAuth"))
    public UserResponse me() {
        return UserResponse.from(authService.currentUser());
    }
}
