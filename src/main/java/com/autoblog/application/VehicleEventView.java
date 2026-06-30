package com.autoblog.application;

import java.time.Instant;
import java.util.UUID;

public record VehicleEventView(
        UUID id,
        UUID vehicleId,
        long sequenceNumber,
        String eventType,
        Instant occurredAt,
        String description,
        String previousHash,
        String hash,
        Instant createdAt
) {
}
