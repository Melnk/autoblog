package com.autoblog.publicreport.application;

public record PublicVehicleView(
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
