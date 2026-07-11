package com.autoblog.reminder.domain;

public class InvalidMaintenanceReminderException extends RuntimeException {

    private final String field;

    public InvalidMaintenanceReminderException(String field, String message) {
        super(message);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
