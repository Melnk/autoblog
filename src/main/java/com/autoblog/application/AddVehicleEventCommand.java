package com.autoblog.application;

import java.time.Instant;
import java.util.UUID;

public record AddVehicleEventCommand(
        UUID vehicleId,
        String eventType,
        Instant occurredAt,
        String description
) {
}
