package com.autoblog.application;

import java.time.Instant;
import java.util.UUID;

public record VehicleView(
        UUID id,
        String vin,
        String make,
        String model,
        String generation,
        Integer year,
        String engine,
        String transmission,
        String trim,
        String market,
        Instant createdAt,
        Instant updatedAt
) {
}
