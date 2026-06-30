package com.autoblog.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleEventJpaRepository extends JpaRepository<VehicleEventEntity, UUID> {

    Optional<VehicleEventEntity> findTopByVehicle_IdOrderBySequenceNumberDesc(UUID vehicleId);

    List<VehicleEventEntity> findByVehicle_IdOrderBySequenceNumberAsc(UUID vehicleId);
}
