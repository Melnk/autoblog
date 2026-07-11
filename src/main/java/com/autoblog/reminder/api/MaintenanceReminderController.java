package com.autoblog.reminder.api;

import com.autoblog.reminder.api.dto.CreateMaintenanceReminderRequest;
import com.autoblog.reminder.api.dto.MaintenanceReminderResponse;
import com.autoblog.reminder.application.CreateMaintenanceReminderCommand;
import com.autoblog.reminder.application.MaintenanceReminderService;
import com.autoblog.reminder.domain.ReminderDueState;
import com.autoblog.reminder.domain.ReminderStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/vehicles/{vehicleId}/reminders")
@Validated
@Tag(name = "Maintenance reminders")
@SecurityRequirement(name = "bearerAuth")
public class MaintenanceReminderController {

    private final MaintenanceReminderService reminders;

    public MaintenanceReminderController(MaintenanceReminderService reminders) {
        this.reminders = reminders;
    }

    @PostMapping
    @Operation(
            summary = "Create a maintenance reminder",
            description = "Creates a planning reminder for a vehicle. Required fields: title, type, and at least one of dueDate or dueOdometerKm.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CreateMaintenanceReminderRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "title": "Заменить масло",
                                      "description": "Следующая замена масла после ТО",
                                      "type": "OIL_CHANGE",
                                      "dueDate": "2026-09-01",
                                      "dueOdometerKm": 135000
                                    }
                                    """)
                    )
            ),
            responses = @ApiResponse(
                    responseCode = "201",
                    description = "Created reminder",
                    content = @Content(schema = @Schema(implementation = MaintenanceReminderResponse.class))
            )
    )
    public ResponseEntity<MaintenanceReminderResponse> createReminder(
            @PathVariable UUID vehicleId,
            @Valid @RequestBody CreateMaintenanceReminderRequest request
    ) {
        var view = reminders.create(vehicleId, new CreateMaintenanceReminderCommand(
                request.title(),
                request.description(),
                request.type(),
                request.dueDate(),
                request.dueOdometerKm()
        ));

        return ResponseEntity
                .created(URI.create("/api/v1/vehicles/" + vehicleId + "/reminders/" + view.id()))
                .body(MaintenanceReminderResponse.from(view));
    }

    @GetMapping
    @Operation(
            summary = "List maintenance reminders for a vehicle",
            description = "Optionally filter by status or dueState.",
            responses = @ApiResponse(
                    responseCode = "200",
                    description = "Vehicle reminders sorted by active status and due state",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    [
                                      {
                                        "id": "11111111-1111-1111-1111-111111111111",
                                        "vehicleId": "22222222-2222-2222-2222-222222222222",
                                        "title": "Заменить масло",
                                        "description": "Следующая замена масла после ТО",
                                        "type": "OIL_CHANGE",
                                        "dueDate": "2026-09-01",
                                        "dueOdometerKm": 135000,
                                        "status": "ACTIVE",
                                        "dueState": "UPCOMING",
                                        "latestOdometerKm": 127842,
                                        "createdAt": "2026-07-04T12:00:00Z",
                                        "updatedAt": "2026-07-04T12:00:00Z",
                                        "completedAt": null,
                                        "cancelledAt": null
                                      }
                                    ]
                                    """)
                    )
            )
    )
    public List<MaintenanceReminderResponse> listReminders(
            @PathVariable UUID vehicleId,
            @RequestParam(required = false) ReminderStatus status,
            @RequestParam(required = false) ReminderDueState dueState
    ) {
        return reminders.list(vehicleId, status, dueState).stream()
                .map(MaintenanceReminderResponse::from)
                .toList();
    }

    @PatchMapping("/{reminderId}/complete")
    @Operation(
            summary = "Complete a maintenance reminder",
            responses = @ApiResponse(
                    responseCode = "200",
                    description = "Completed reminder",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = MaintenanceReminderResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "id": "11111111-1111-1111-1111-111111111111",
                                      "vehicleId": "22222222-2222-2222-2222-222222222222",
                                      "title": "Заменить масло",
                                      "type": "OIL_CHANGE",
                                      "dueDate": "2026-09-01",
                                      "dueOdometerKm": 135000,
                                      "status": "COMPLETED",
                                      "dueState": "COMPLETED",
                                      "latestOdometerKm": 127842,
                                      "createdAt": "2026-07-04T12:00:00Z",
                                      "updatedAt": "2026-07-05T12:00:00Z",
                                      "completedAt": "2026-07-05T12:00:00Z",
                                      "cancelledAt": null
                                    }
                                    """)
                    )
            )
    )
    public MaintenanceReminderResponse completeReminder(
            @PathVariable UUID vehicleId,
            @PathVariable UUID reminderId
    ) {
        return MaintenanceReminderResponse.from(reminders.complete(vehicleId, reminderId));
    }

    @PatchMapping("/{reminderId}/cancel")
    @Operation(
            summary = "Cancel a maintenance reminder",
            responses = @ApiResponse(
                    responseCode = "200",
                    description = "Cancelled reminder",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = MaintenanceReminderResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "id": "11111111-1111-1111-1111-111111111111",
                                      "vehicleId": "22222222-2222-2222-2222-222222222222",
                                      "title": "Проверить тормоза",
                                      "type": "BRAKE_SERVICE",
                                      "dueDate": null,
                                      "dueOdometerKm": 128300,
                                      "status": "CANCELLED",
                                      "dueState": "CANCELLED",
                                      "latestOdometerKm": 127842,
                                      "createdAt": "2026-07-04T12:00:00Z",
                                      "updatedAt": "2026-07-05T12:00:00Z",
                                      "completedAt": null,
                                      "cancelledAt": "2026-07-05T12:00:00Z"
                                    }
                                    """)
                    )
            )
    )
    public MaintenanceReminderResponse cancelReminder(
            @PathVariable UUID vehicleId,
            @PathVariable UUID reminderId
    ) {
        return MaintenanceReminderResponse.from(reminders.cancel(vehicleId, reminderId));
    }
}
