package com.autoblog.api;

import com.autoblog.api.dto.CreateVehicleRequest;
import com.autoblog.api.dto.VehicleResponse;
import com.autoblog.application.CreateVehicleCommand;
import com.autoblog.application.VehicleApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
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
public class VehicleController {

    private final VehicleApplicationService vehicles;

    public VehicleController(VehicleApplicationService vehicles) {
        this.vehicles = vehicles;
    }

    @PostMapping
    @Operation(summary = "Create a vehicle")
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

    @GetMapping("/by-vin/{vin}")
    @Operation(summary = "Get a vehicle by VIN")
    public VehicleResponse getVehicleByVin(@PathVariable String vin) {
        return VehicleResponse.from(vehicles.getVehicleByVin(vin));
    }
}
