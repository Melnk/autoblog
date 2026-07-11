package com.autoblog.reminder.application;

import com.autoblog.reminder.domain.ReminderDueState;
import com.autoblog.reminder.domain.ReminderStatus;
import com.autoblog.reminder.domain.ReminderType;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record MaintenanceReminderView(
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
}
