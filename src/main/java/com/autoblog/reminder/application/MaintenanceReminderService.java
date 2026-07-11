package com.autoblog.reminder.application;

import com.autoblog.access.application.VehicleAccessService;
import com.autoblog.application.VehicleNotFoundException;
import com.autoblog.infrastructure.persistence.VehicleEntity;
import com.autoblog.infrastructure.persistence.VehicleEventEntity;
import com.autoblog.infrastructure.persistence.VehicleEventJpaRepository;
import com.autoblog.infrastructure.persistence.VehicleJpaRepository;
import com.autoblog.reminder.domain.InvalidMaintenanceReminderException;
import com.autoblog.reminder.domain.MaintenanceReminderNotFoundException;
import com.autoblog.reminder.domain.ReminderDueState;
import com.autoblog.reminder.domain.ReminderStatus;
import com.autoblog.reminder.infrastructure.MaintenanceReminderEntity;
import com.autoblog.reminder.infrastructure.MaintenanceReminderJpaRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MaintenanceReminderService {

    private final MaintenanceReminderJpaRepository reminders;
    private final VehicleJpaRepository vehicles;
    private final VehicleEventJpaRepository events;
    private final VehicleAccessService vehicleAccess;

    public MaintenanceReminderService(
            MaintenanceReminderJpaRepository reminders,
            VehicleJpaRepository vehicles,
            VehicleEventJpaRepository events,
            VehicleAccessService vehicleAccess
    ) {
        this.reminders = reminders;
        this.vehicles = vehicles;
        this.events = events;
        this.vehicleAccess = vehicleAccess;
    }

    @Transactional
    public MaintenanceReminderView create(UUID vehicleId, CreateMaintenanceReminderCommand command) {
        vehicleAccess.requireEditAccess(vehicleId);
        VehicleEntity vehicle = findVehicle(vehicleId);
        validate(command);

        MaintenanceReminderEntity reminder = reminders.save(new MaintenanceReminderEntity(
                UUID.randomUUID(),
                vehicle,
                trimToNull(command.title()),
                trimToNull(command.description()),
                command.type(),
                command.dueDate(),
                command.dueOdometerKm()
        ));

        return toView(reminder, latestOdometerKm(vehicleId));
    }

    @Transactional(readOnly = true)
    public List<MaintenanceReminderView> list(
            UUID vehicleId,
            ReminderStatus status,
            ReminderDueState dueState
    ) {
        vehicleAccess.requireViewAccess(vehicleId);
        findVehicle(vehicleId);
        Integer latestOdometerKm = latestOdometerKm(vehicleId);

        return reminders.findByVehicle_Id(vehicleId).stream()
                .map(reminder -> toView(reminder, latestOdometerKm))
                .filter(reminder -> status == null || reminder.status() == status)
                .filter(reminder -> dueState == null || reminder.dueState() == dueState)
                .sorted(reminderComparator())
                .toList();
    }

    @Transactional
    public MaintenanceReminderView complete(UUID vehicleId, UUID reminderId) {
        vehicleAccess.requireEditAccess(vehicleId);
        findVehicle(vehicleId);
        MaintenanceReminderEntity reminder = findReminder(vehicleId, reminderId);
        reminder.complete(Instant.now());
        return toView(reminder, latestOdometerKm(vehicleId));
    }

    @Transactional
    public MaintenanceReminderView cancel(UUID vehicleId, UUID reminderId) {
        vehicleAccess.requireEditAccess(vehicleId);
        findVehicle(vehicleId);
        MaintenanceReminderEntity reminder = findReminder(vehicleId, reminderId);
        reminder.cancel(Instant.now());
        return toView(reminder, latestOdometerKm(vehicleId));
    }

    private void validate(CreateMaintenanceReminderCommand command) {
        if (command.dueDate() == null && command.dueOdometerKm() == null) {
            throw new InvalidMaintenanceReminderException(
                    "dueDate",
                    "Either due date or due odometer is required"
            );
        }
    }

    private VehicleEntity findVehicle(UUID vehicleId) {
        return vehicles.findById(vehicleId)
                .orElseThrow(() -> new VehicleNotFoundException(vehicleId));
    }

    private MaintenanceReminderEntity findReminder(UUID vehicleId, UUID reminderId) {
        return reminders.findByIdAndVehicle_Id(reminderId, vehicleId)
                .orElseThrow(() -> new MaintenanceReminderNotFoundException(reminderId));
    }

    private Integer latestOdometerKm(UUID vehicleId) {
        return events.findFirstByVehicle_IdAndOdometerKmIsNotNullOrderBySequenceNumberDesc(vehicleId)
                .map(VehicleEventEntity::getOdometerKm)
                .orElse(null);
    }

    private MaintenanceReminderView toView(MaintenanceReminderEntity reminder, Integer latestOdometerKm) {
        return new MaintenanceReminderView(
                reminder.getId(),
                reminder.getVehicle().getId(),
                reminder.getTitle(),
                reminder.getDescription(),
                reminder.getType(),
                reminder.getDueDate(),
                reminder.getDueOdometerKm(),
                reminder.getStatus(),
                dueState(reminder, latestOdometerKm, LocalDate.now()),
                latestOdometerKm,
                reminder.getCreatedAt(),
                reminder.getUpdatedAt(),
                reminder.getCompletedAt(),
                reminder.getCancelledAt()
        );
    }

    private ReminderDueState dueState(MaintenanceReminderEntity reminder, Integer latestOdometerKm, LocalDate today) {
        if (reminder.getStatus() == ReminderStatus.COMPLETED) {
            return ReminderDueState.COMPLETED;
        }
        if (reminder.getStatus() == ReminderStatus.CANCELLED) {
            return ReminderDueState.CANCELLED;
        }

        LocalDate dueDate = reminder.getDueDate();
        Integer dueOdometerKm = reminder.getDueOdometerKm();
        if (dueDate != null && dueDate.isBefore(today)) {
            return ReminderDueState.OVERDUE;
        }
        if (latestOdometerKm != null && dueOdometerKm != null && dueOdometerKm <= latestOdometerKm) {
            return ReminderDueState.OVERDUE;
        }
        if (dueDate != null && !dueDate.isBefore(today) && !dueDate.isAfter(today.plusDays(14))) {
            return ReminderDueState.DUE_SOON;
        }
        if (latestOdometerKm != null && dueOdometerKm != null
                && dueOdometerKm > latestOdometerKm
                && dueOdometerKm - latestOdometerKm <= 500) {
            return ReminderDueState.DUE_SOON;
        }
        return ReminderDueState.UPCOMING;
    }

    private Comparator<MaintenanceReminderView> reminderComparator() {
        return Comparator
                .comparingInt((MaintenanceReminderView reminder) -> reminder.status() == ReminderStatus.ACTIVE ? 0 : 1)
                .thenComparingInt(reminder -> dueStatePriority(reminder.dueState()))
                .thenComparing(MaintenanceReminderView::dueDate, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(MaintenanceReminderView::dueOdometerKm, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(MaintenanceReminderView::createdAt, Comparator.reverseOrder());
    }

    private int dueStatePriority(ReminderDueState dueState) {
        return switch (dueState) {
            case OVERDUE -> 0;
            case DUE_SOON -> 1;
            case UPCOMING -> 2;
            case COMPLETED -> 3;
            case CANCELLED -> 4;
        };
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
