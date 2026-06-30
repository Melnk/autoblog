package com.autoblog.application;

import java.time.Instant;
import java.util.UUID;

public record EventHashInput(
        UUID vehicleId,
        long sequenceNumber,
        Instant occurredAt,
        String eventType,
        String description,
        String previousHash
) {
}
