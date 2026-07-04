package com.autoblog.application;

import java.util.UUID;

public class VehicleEventNotFoundException extends RuntimeException {

    public VehicleEventNotFoundException(UUID eventId) {
        super("Vehicle event " + eventId + " was not found");
    }
}
