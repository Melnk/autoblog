package com.autoblog.reminder.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaintenanceReminderJpaRepository extends JpaRepository<MaintenanceReminderEntity, UUID> {

    List<MaintenanceReminderEntity> findByVehicle_Id(UUID vehicleId);

    Optional<MaintenanceReminderEntity> findByIdAndVehicle_Id(UUID reminderId, UUID vehicleId);
}
