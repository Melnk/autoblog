package com.autoblog.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleJpaRepository extends JpaRepository<VehicleEntity, UUID> {

    boolean existsByVin(String vin);

    Optional<VehicleEntity> findByVin(String vin);
}
