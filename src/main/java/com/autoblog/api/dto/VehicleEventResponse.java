package com.autoblog.api.dto;

import com.autoblog.application.VehicleEventView;
import java.time.Instant;
import java.util.UUID;

public record VehicleEventResponse(
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
    public static VehicleEventResponse from(VehicleEventView event) {
        return new VehicleEventResponse(
                event.id(),
                event.vehicleId(),
                event.sequenceNumber(),
                event.eventType(),
                event.occurredAt(),
                event.description(),
                event.previousHash(),
                event.hash(),
                event.createdAt()
        );
    }
}
