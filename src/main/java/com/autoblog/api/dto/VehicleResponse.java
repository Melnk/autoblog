package com.autoblog.api.dto;

import com.autoblog.application.VehicleView;
import java.time.Instant;
import java.util.UUID;

public record VehicleResponse(
        UUID id,
        String vin,
        String make,
        String model,
        Integer year,
        Instant createdAt
) {
    public static VehicleResponse from(VehicleView vehicle) {
        return new VehicleResponse(
                vehicle.id(),
                vehicle.vin(),
                vehicle.make(),
                vehicle.model(),
                vehicle.year(),
                vehicle.createdAt()
        );
    }
}
