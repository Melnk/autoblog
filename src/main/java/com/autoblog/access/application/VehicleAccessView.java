package com.autoblog.access.application;

import com.autoblog.access.domain.VehicleAccessRole;
import java.time.Instant;
import java.util.UUID;

public record VehicleAccessView(
        UUID id,
        UUID vehicleId,
        UUID userId,
        String email,
        VehicleAccessRole role,
        Instant createdAt
) {
}
