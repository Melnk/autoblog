package com.autoblog.access.application;

import java.util.UUID;

public class VehicleAccessNotFoundException extends RuntimeException {

    public VehicleAccessNotFoundException(UUID vehicleId, UUID userId) {
        super("Vehicle access for user " + userId + " on vehicle " + vehicleId + " was not found");
    }
}
