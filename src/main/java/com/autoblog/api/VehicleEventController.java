package com.autoblog.api;

import com.autoblog.api.dto.CreateVehicleEventRequest;
import com.autoblog.api.dto.VehicleEventResponse;
import com.autoblog.application.AddVehicleEventCommand;
import com.autoblog.application.VehicleApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
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
@RequestMapping("/api/vehicles/{vehicleId}")
@Validated
@Tag(name = "Vehicle timeline")
public class VehicleEventController {

    private final VehicleApplicationService vehicles;

    public VehicleEventController(VehicleApplicationService vehicles) {
        this.vehicles = vehicles;
    }

    @PostMapping("/events")
    @Operation(summary = "Add a vehicle event")
    public ResponseEntity<VehicleEventResponse> addEvent(
            @PathVariable UUID vehicleId,
            @Valid @RequestBody CreateVehicleEventRequest request
    ) {
        var view = vehicles.addEvent(new AddVehicleEventCommand(
                vehicleId,
                request.eventType(),
                request.occurredAt(),
                request.description()
        ));

        return ResponseEntity
                .created(URI.create("/api/vehicles/" + vehicleId + "/events/" + view.id()))
                .body(VehicleEventResponse.from(view));
    }

    @GetMapping("/timeline")
    @Operation(summary = "Get a vehicle timeline")
    public List<VehicleEventResponse> timeline(@PathVariable UUID vehicleId) {
        return vehicles.timeline(vehicleId).stream()
                .map(VehicleEventResponse::from)
                .toList();
    }
}
