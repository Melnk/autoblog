package com.autoblog.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VehicleJpaRepository extends JpaRepository<VehicleEntity, UUID> {

    boolean existsByVin(String vin);

    Optional<VehicleEntity> findByVin(String vin);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select vehicle from VehicleEntity vehicle where vehicle.id = :vehicleId")
    Optional<VehicleEntity> findByIdForUpdate(@Param("vehicleId") UUID vehicleId);
}
