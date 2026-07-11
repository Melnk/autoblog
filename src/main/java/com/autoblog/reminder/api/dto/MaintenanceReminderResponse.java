package com.autoblog.reminder.api.dto;

import com.autoblog.reminder.application.MaintenanceReminderView;
import com.autoblog.reminder.domain.ReminderDueState;
import com.autoblog.reminder.domain.ReminderStatus;
import com.autoblog.reminder.domain.ReminderType;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record MaintenanceReminderResponse(
        UUID id,
        UUID vehicleId,
        String title,
        String description,
        ReminderType type,
        LocalDate dueDate,
        Integer dueOdometerKm,
        ReminderStatus status,
        ReminderDueState dueState,
        Integer latestOdometerKm,
        Instant createdAt,
        Instant updatedAt,
        Instant completedAt,
        Instant cancelledAt
) {
    public static MaintenanceReminderResponse from(MaintenanceReminderView reminder) {
        return new MaintenanceReminderResponse(
                reminder.id(),
                reminder.vehicleId(),
                reminder.title(),
                reminder.description(),
                reminder.type(),
                reminder.dueDate(),
                reminder.dueOdometerKm(),
                reminder.status(),
                reminder.dueState(),
                reminder.latestOdometerKm(),
                reminder.createdAt(),
                reminder.updatedAt(),
                reminder.completedAt(),
                reminder.cancelledAt()
        );
    }
}
