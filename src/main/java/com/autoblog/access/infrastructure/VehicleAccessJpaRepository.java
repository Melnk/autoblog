package com.autoblog.access.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleAccessJpaRepository extends JpaRepository<VehicleAccessEntity, UUID> {

    Optional<VehicleAccessEntity> findByVehicle_IdAndUser_Id(UUID vehicleId, UUID userId);

    List<VehicleAccessEntity> findByVehicle_IdOrderByCreatedAtAsc(UUID vehicleId);

    Page<VehicleAccessEntity> findByVehicle_Id(UUID vehicleId, Pageable pageable);

    List<VehicleAccessEntity> findByUser_IdOrderByCreatedAtAsc(UUID userId);

    Page<VehicleAccessEntity> findByUser_Id(UUID userId, Pageable pageable);
}
