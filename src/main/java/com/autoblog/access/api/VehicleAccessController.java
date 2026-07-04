package com.autoblog.access.api;

import com.autoblog.access.api.dto.GrantVehicleAccessRequest;
import com.autoblog.access.api.dto.VehicleAccessResponse;
import com.autoblog.access.application.VehicleAccessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/vehicles/{vehicleId}/access")
@Tag(name = "Vehicle access")
@SecurityRequirement(name = "bearerAuth")
public class VehicleAccessController {

    private final VehicleAccessService vehicleAccess;

    public VehicleAccessController(VehicleAccessService vehicleAccess) {
        this.vehicleAccess = vehicleAccess;
    }

    @PostMapping
    @Operation(
            summary = "Grant vehicle access",
            description = "Grants EDITOR or VIEWER access to an existing user by email. userId is not accepted in this request body.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = GrantVehicleAccessRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "email": "editor@example.com",
                                      "role": "EDITOR"
                                    }
                                    """)
                    )
            )
    )
    public VehicleAccessResponse grantAccess(
            @PathVariable UUID vehicleId,
            @Valid @RequestBody GrantVehicleAccessRequest request
    ) {
        return VehicleAccessResponse.from(vehicleAccess.grantAccess(vehicleId, request.email(), request.role()));
    }

    @GetMapping
    @Operation(summary = "List vehicle access")
    public List<VehicleAccessResponse> listAccess(@PathVariable UUID vehicleId) {
        return vehicleAccess.listAccess(vehicleId).stream()
                .map(VehicleAccessResponse::from)
                .toList();
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "Revoke vehicle access")
    public ResponseEntity<Void> revokeAccess(
            @PathVariable UUID vehicleId,
            @PathVariable UUID userId
    ) {
        vehicleAccess.revokeAccess(vehicleId, userId);
        return ResponseEntity.noContent().build();
    }
}
