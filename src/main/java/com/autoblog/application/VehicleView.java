package com.autoblog.application;

import com.autoblog.access.domain.VehicleAccessRole;
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
        VehicleAccessRole role,
        Instant createdAt,
        Instant updatedAt
) {
}
