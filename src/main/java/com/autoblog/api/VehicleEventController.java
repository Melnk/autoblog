package com.autoblog.api;

import com.autoblog.api.dto.CreateVehicleEventRequest;
import com.autoblog.api.dto.VehicleEventResponse;
import com.autoblog.application.AddVehicleEventCommand;
import com.autoblog.application.VehicleApplicationService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
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
@RequestMapping("/api/v1/vehicles/{vehicleId}/events")
@Validated
@Tag(name = "Vehicle events")
public class VehicleEventController {

    private final VehicleApplicationService vehicles;

    public VehicleEventController(VehicleApplicationService vehicles) {
        this.vehicles = vehicles;
    }

    @PostMapping
    @Operation(
            summary = "Add a vehicle event",
            description = "Appends an event to the vehicle history. Required fields: type, eventDate, title.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CreateVehicleEventRequest.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Maintenance",
                                            value = """
                                                    {
                                                      "type": "MAINTENANCE",
                                                      "eventDate": "2026-07-02",
                                                      "odometerKm": 120000,
                                                      "title": "Замена масла",
                                                      "description": "Масло 5W-40, масляный фильтр",
                                                      "costAmount": 5000,
                                                      "costCurrency": "RUB",
                                                      "serviceName": "Гаражный сервис",
                                                      "payload": {
                                                        "oil": "5W-40",
                                                        "parts": ["oil_filter"]
                                                      }
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "Repair",
                                            value = """
                                                    {
                                                      "type": "REPAIR",
                                                      "eventDate": "2026-07-10",
                                                      "odometerKm": 120500,
                                                      "title": "Замена передних тормозных колодок",
                                                      "description": "Заменены передние тормозные колодки",
                                                      "costAmount": 3500,
                                                      "costCurrency": "RUB",
                                                      "serviceName": "Гаражный сервис",
                                                      "payload": {
                                                        "parts": ["front_brake_pads"]
                                                      }
                                                    }
                                                    """
                                    )
                            }
                    )
            )
    )
    public ResponseEntity<VehicleEventResponse> addEvent(
            @PathVariable UUID vehicleId,
            @Valid @RequestBody CreateVehicleEventRequest request
    ) {
        var view = vehicles.addEvent(new AddVehicleEventCommand(
                vehicleId,
                request.type(),
                request.eventDate(),
                request.odometerKm(),
                request.title(),
                request.description(),
                request.costAmount(),
                request.costCurrency(),
                request.serviceName(),
                request.payload()
        ));

        return ResponseEntity
                .created(URI.create("/api/v1/vehicles/" + vehicleId + "/events/" + view.id()))
                .body(VehicleEventResponse.from(view));
    }

    @GetMapping
    @Operation(summary = "Get vehicle events")
    public List<VehicleEventResponse> getEvents(@PathVariable UUID vehicleId) {
        return vehicles.getEvents(vehicleId).stream()
                .map(VehicleEventResponse::from)
                .toList();
    }
}
