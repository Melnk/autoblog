package com.autoblog.api.v2;

import com.autoblog.access.api.dto.VehicleAccessResponse;
import com.autoblog.access.application.VehicleAccessService;
import com.autoblog.api.dto.VehicleEventResponse;
import com.autoblog.api.dto.VehicleResponse;
import com.autoblog.api.pagination.PagedResponse;
import com.autoblog.api.pagination.PaginationValidator;
import com.autoblog.application.VehicleApplicationService;
import com.autoblog.attachment.api.dto.EventAttachmentResponse;
import com.autoblog.attachment.application.EventAttachmentService;
import com.autoblog.reminder.api.dto.MaintenanceReminderResponse;
import com.autoblog.reminder.application.MaintenanceReminderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v2/vehicles")
@Tag(name = "Paginated vehicle queries")
@SecurityRequirement(name = "bearerAuth")
public class VehicleQueryController {

    private final VehicleAccessService access;
    private final EventAttachmentService attachments;
    private final VehicleApplicationService vehicles;
    private final PaginationValidator pagination;
    private final MaintenanceReminderService reminders;

    public VehicleQueryController(
            VehicleAccessService access,
            EventAttachmentService attachments,
            VehicleApplicationService vehicles,
            PaginationValidator pagination,
            MaintenanceReminderService reminders
    ) {
        this.access = access;
        this.attachments = attachments;
        this.vehicles = vehicles;
        this.pagination = pagination;
        this.reminders = reminders;
    }

    @GetMapping
    @Operation(summary = "List accessible vehicles with pagination")
    public PagedResponse<VehicleResponse> listVehicles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        var pageable = pagination.validateAndCreate(
                page,
                size,
                Sort.by(Sort.Direction.ASC, "createdAt")
        );
        return PagedResponse.from(vehicles.getAccessibleVehicles(pageable), VehicleResponse::from);
    }

    @GetMapping("/{vehicleId}/events")
    @Operation(summary = "List vehicle events with pagination")
    public PagedResponse<VehicleEventResponse> listEvents(
            @PathVariable UUID vehicleId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        var pageable = pagination.validateAndCreate(
                page,
                size,
                Sort.by(Sort.Direction.ASC, "sequenceNumber")
        );
        return PagedResponse.from(vehicles.getEvents(vehicleId, pageable), VehicleEventResponse::from);
    }

    @GetMapping("/{vehicleId}/events/{eventId}/attachments")
    @Operation(summary = "List event attachments with pagination")
    public PagedResponse<EventAttachmentResponse> listAttachments(
            @PathVariable UUID vehicleId,
            @PathVariable UUID eventId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        var pageable = pagination.validateAndCreate(
                page,
                size,
                Sort.by(Sort.Direction.ASC, "createdAt")
        );
        return PagedResponse.from(
                attachments.list(vehicleId, eventId, pageable),
                EventAttachmentResponse::from
        );
    }

    @GetMapping("/{vehicleId}/reminders")
    @Operation(summary = "List maintenance reminders with pagination")
    public PagedResponse<MaintenanceReminderResponse> listReminders(
            @PathVariable UUID vehicleId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        var pageable = pagination.validateAndCreate(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
        return PagedResponse.from(reminders.list(vehicleId, pageable), MaintenanceReminderResponse::from);
    }

    @GetMapping("/{vehicleId}/access")
    @Operation(summary = "List vehicle access entries with pagination")
    public PagedResponse<VehicleAccessResponse> listAccess(
            @PathVariable UUID vehicleId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        var pageable = pagination.validateAndCreate(
                page,
                size,
                Sort.by(Sort.Direction.ASC, "createdAt")
        );
        return PagedResponse.from(access.listAccess(vehicleId, pageable), VehicleAccessResponse::from);
    }
}
