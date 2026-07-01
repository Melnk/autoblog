package com.autoblog.application;

public record CreateVehicleCommand(
        String vin,
        String make,
        String model,
        String generation,
        Integer year,
        String engine,
        String transmission,
        String trim,
        String market
) {
}
