package com.autoblog.api.dto;

import com.autoblog.access.domain.VehicleAccessRole;
import com.autoblog.application.VehicleView;
import java.time.Instant;
import java.util.UUID;

public record VehicleResponse(
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
    public static VehicleResponse from(VehicleView vehicle) {
        return new VehicleResponse(
                vehicle.id(),
                vehicle.vin(),
                vehicle.make(),
                vehicle.model(),
                vehicle.generation(),
                vehicle.year(),
                vehicle.engine(),
                vehicle.transmission(),
                vehicle.trim(),
                vehicle.market(),
                vehicle.role(),
                vehicle.createdAt(),
                vehicle.updatedAt()
        );
    }
}
