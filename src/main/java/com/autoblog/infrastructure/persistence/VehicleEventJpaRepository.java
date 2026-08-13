package com.autoblog.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleEventJpaRepository extends JpaRepository<VehicleEventEntity, UUID> {

    Optional<VehicleEventEntity> findTopByVehicle_IdOrderBySequenceNumberDesc(UUID vehicleId);

    Optional<VehicleEventEntity> findFirstByVehicle_IdAndOdometerKmIsNotNullOrderBySequenceNumberDesc(UUID vehicleId);

    List<VehicleEventEntity> findByVehicle_IdOrderBySequenceNumberAsc(UUID vehicleId);

    Page<VehicleEventEntity> findByVehicle_Id(UUID vehicleId, Pageable pageable);

    Optional<VehicleEventEntity> findByIdAndVehicle_Id(UUID eventId, UUID vehicleId);
}
