package com.autoblog.publicreport.infrastructure;

import com.autoblog.publicreport.domain.PublicReportStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PublicVehicleReportJpaRepository extends JpaRepository<PublicVehicleReportEntity, UUID> {

    Optional<PublicVehicleReportEntity> findByVehicle_IdAndStatus(UUID vehicleId, PublicReportStatus status);

    Optional<PublicVehicleReportEntity> findByPublicTokenAndStatus(String publicToken, PublicReportStatus status);

    boolean existsByPublicToken(String publicToken);
}
