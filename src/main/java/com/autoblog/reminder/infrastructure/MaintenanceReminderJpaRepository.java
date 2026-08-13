package com.autoblog.reminder.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaintenanceReminderJpaRepository extends JpaRepository<MaintenanceReminderEntity, UUID> {

    List<MaintenanceReminderEntity> findByVehicle_Id(UUID vehicleId);

    Page<MaintenanceReminderEntity> findByVehicle_Id(UUID vehicleId, Pageable pageable);

    Optional<MaintenanceReminderEntity> findByIdAndVehicle_Id(UUID reminderId, UUID vehicleId);
}
