package com.autoblog.application;

import java.util.UUID;

public class VehicleNotFoundException extends RuntimeException {

    public VehicleNotFoundException(UUID vehicleId) {
        super("Vehicle " + vehicleId + " was not found");
    }
}
