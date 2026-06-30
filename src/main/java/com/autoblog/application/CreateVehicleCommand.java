package com.autoblog.application;

public record CreateVehicleCommand(
        String vin,
        String make,
        String model,
        Integer year
) {
}
