package com.autoblog.access.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleAccessJpaRepository extends JpaRepository<VehicleAccessEntity, UUID> {

    Optional<VehicleAccessEntity> findByVehicle_IdAndUser_Id(UUID vehicleId, UUID userId);

    List<VehicleAccessEntity> findByVehicle_IdOrderByCreatedAtAsc(UUID vehicleId);

    List<VehicleAccessEntity> findByUser_IdOrderByCreatedAtAsc(UUID userId);
}
