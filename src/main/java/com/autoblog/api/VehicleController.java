package com.autoblog.api;

import com.autoblog.api.dto.CreateVehicleRequest;
import com.autoblog.api.dto.VehicleResponse;
import com.autoblog.application.CreateVehicleCommand;
import com.autoblog.application.VehicleApplicationService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/vehicles")
@Validated
@Tag(name = "Vehicles")
@SecurityRequirement(name = "bearerAuth")
public class VehicleController {

    private final VehicleApplicationService vehicles;

    public VehicleController(VehicleApplicationService vehicles) {
        this.vehicles = vehicles;
    }

    @PostMapping
    @Operation(
            summary = "Create a vehicle",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CreateVehicleRequest.class),
                            examples = @ExampleObject(
                                    name = "Lada Priora",
                                    value = """
                                            {
                                              "vin": "XTA217030C0000000",
                                              "make": "Lada",
                                              "model": "Priora",
                                              "generation": "2170",
                                              "year": 2012,
                                              "engine": "1.6",
                                              "transmission": "MT",
                                              "trim": "Norma",
                                              "market": "RU"
                                            }
                                            """
                            )
                    )
            )
    )
    public ResponseEntity<VehicleResponse> createVehicle(@Valid @RequestBody CreateVehicleRequest request) {
        var view = vehicles.createVehicle(new CreateVehicleCommand(
                request.vin(),
                request.make(),
                request.model(),
                request.generation(),
                request.year(),
                request.engine(),
                request.transmission(),
                request.trim(),
                request.market()
        ));

        return ResponseEntity
                .created(URI.create("/api/v1/vehicles/" + view.id()))
                .body(VehicleResponse.from(view));
    }

    @GetMapping("/{vehicleId}")
    @Operation(summary = "Get a vehicle by id")
    public VehicleResponse getVehicle(@PathVariable UUID vehicleId) {
        return VehicleResponse.from(vehicles.getVehicle(vehicleId));
    }

    @GetMapping
    @Operation(summary = "List vehicles current user can access")
    public List<VehicleResponse> getVehicles() {
        return vehicles.getAccessibleVehicles().stream()
                .map(VehicleResponse::from)
                .toList();
    }

    @GetMapping("/by-vin/{vin}")
    @Operation(summary = "Get a vehicle by VIN")
    public VehicleResponse getVehicleByVin(@PathVariable String vin) {
        return VehicleResponse.from(vehicles.getVehicleByVin(vin));
    }
}
