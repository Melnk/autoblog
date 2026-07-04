package com.autoblog.access.api.dto;

import com.autoblog.access.application.VehicleAccessView;
import com.autoblog.access.domain.VehicleAccessRole;
import java.time.Instant;
import java.util.UUID;

public record VehicleAccessResponse(
        UUID id,
        UUID vehicleId,
        UUID userId,
        String email,
        VehicleAccessRole role,
        Instant createdAt
) {
    public static VehicleAccessResponse from(VehicleAccessView view) {
        return new VehicleAccessResponse(
                view.id(),
                view.vehicleId(),
                view.userId(),
                view.email(),
                view.role(),
                view.createdAt()
        );
    }
}
