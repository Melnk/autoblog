package com.autoblog.attachment.infrastructure;

import com.autoblog.attachment.domain.AttachmentVisibility;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventAttachmentJpaRepository extends JpaRepository<EventAttachmentEntity, UUID> {

    List<EventAttachmentEntity> findByEvent_IdOrderByCreatedAtAsc(UUID eventId);

    List<EventAttachmentEntity> findByEvent_IdInAndVisibilityOrderByCreatedAtAsc(
            List<UUID> eventIds,
            AttachmentVisibility visibility
    );

    Optional<EventAttachmentEntity> findByIdAndVehicle_IdAndEvent_Id(UUID id, UUID vehicleId, UUID eventId);

    Optional<EventAttachmentEntity> findByIdAndVisibility(UUID id, AttachmentVisibility visibility);
}
