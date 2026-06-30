package com.autoblog.application;

import java.time.Instant;
import java.util.UUID;

public record VehicleView(
        UUID id,
        String vin,
        String make,
        String model,
        Integer year,
        Instant createdAt
) {
}
