package com.autoblog.reminder.domain;

import java.util.UUID;

public class MaintenanceReminderNotFoundException extends RuntimeException {

    public MaintenanceReminderNotFoundException(UUID reminderId) {
        super("Maintenance reminder " + reminderId + " was not found");
    }
}
