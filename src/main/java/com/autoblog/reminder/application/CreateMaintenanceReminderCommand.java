package com.autoblog.reminder.application;

import com.autoblog.reminder.domain.ReminderType;
import java.time.LocalDate;

public record CreateMaintenanceReminderCommand(
        String title,
        String description,
        ReminderType type,
        LocalDate dueDate,
        Integer dueOdometerKm
) {
}
