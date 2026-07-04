package com.autoblog.attachment.application;

import com.autoblog.application.VehicleEventNotFoundException;
import com.autoblog.application.VehicleNotFoundException;
import com.autoblog.attachment.domain.AttachmentNotFoundException;
import com.autoblog.attachment.domain.AttachmentType;
import com.autoblog.attachment.domain.AttachmentVisibility;
import com.autoblog.attachment.domain.InvalidAttachmentException;
import com.autoblog.attachment.infrastructure.EventAttachmentEntity;
import com.autoblog.attachment.infrastructure.EventAttachmentJpaRepository;
import com.autoblog.infrastructure.persistence.VehicleEntity;
import com.autoblog.infrastructure.persistence.VehicleEventEntity;
import com.autoblog.infrastructure.persistence.VehicleEventJpaRepository;
import com.autoblog.infrastructure.persistence.VehicleJpaRepository;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class EventAttachmentService {

    private static final Map<String, String> ALLOWED_CONTENT_TYPES = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/webp", ".webp",
            "application/pdf", ".pdf"
    );

    private final EventAttachmentJpaRepository attachments;
    private final VehicleJpaRepository vehicles;
    private final VehicleEventJpaRepository events;
    private final AttachmentStorage storage;
    private final AttachmentChecksumService checksumService;
    private final AttachmentProperties properties;

    public EventAttachmentService(
            EventAttachmentJpaRepository attachments,
            VehicleJpaRepository vehicles,
            VehicleEventJpaRepository events,
            AttachmentStorage storage,
            AttachmentChecksumService checksumService,
            AttachmentProperties properties
    ) {
        this.attachments = attachments;
        this.vehicles = vehicles;
        this.events = events;
        this.storage = storage;
        this.checksumService = checksumService;
        this.properties = properties;
    }

    @Transactional
    public EventAttachmentView upload(
            UUID vehicleId,
            UUID eventId,
            MultipartFile file,
            AttachmentType type,
            AttachmentVisibility visibility,
            String description
    ) {
        VehicleEntity vehicle = findVehicle(vehicleId);
        VehicleEventEntity event = findEvent(vehicleId, eventId);
        byte[] content = validateAndRead(file);
        String contentType = normalizeContentType(file.getContentType());
        String checksum = checksumService.sha256(content);
        String storageKey = storageKey(vehicleId, eventId, contentType);

        storage.store(storageKey, content);

        EventAttachmentEntity attachment = new EventAttachmentEntity(
                UUID.randomUUID(),
                vehicle,
                event,
                type,
                visibility == null ? AttachmentVisibility.PRIVATE : visibility,
                originalFilename(file.getOriginalFilename()),
                contentType,
                content.length,
                checksum,
                storageKey,
                trimToNull(description)
        );

        return toView(attachments.save(attachment));
    }

    @Transactional(readOnly = true)
    public List<EventAttachmentView> list(UUID vehicleId, UUID eventId) {
        findVehicle(vehicleId);
        findEvent(vehicleId, eventId);
        return attachments.findByEvent_IdOrderByCreatedAtAsc(eventId).stream()
                .map(this::toView)
                .toList();
    }

    @Transactional(readOnly = true)
    public AttachmentContentView download(UUID vehicleId, UUID eventId, UUID attachmentId) {
        findVehicle(vehicleId);
        findEvent(vehicleId, eventId);
        EventAttachmentEntity attachment = attachments.findByIdAndVehicle_IdAndEvent_Id(attachmentId, vehicleId, eventId)
                .orElseThrow(() -> new AttachmentNotFoundException(attachmentId));
        return toContentView(attachment);
    }

    @Transactional(readOnly = true)
    public List<PublicAttachmentView> publicAttachments(
            List<UUID> eventIds,
            String publicToken
    ) {
        if (eventIds.isEmpty()) {
            return List.of();
        }
        return attachments.findByEvent_IdInAndVisibilityOrderByCreatedAtAsc(eventIds, AttachmentVisibility.PUBLIC).stream()
                .map(attachment -> toPublicView(attachment, publicToken))
                .toList();
    }

    @Transactional(readOnly = true)
    public AttachmentContentView downloadPublic(UUID vehicleId, UUID attachmentId) {
        EventAttachmentEntity attachment = attachments.findByIdAndVisibility(attachmentId, AttachmentVisibility.PUBLIC)
                .filter(candidate -> candidate.getVehicle().getId().equals(vehicleId))
                .orElseThrow(() -> new AttachmentNotFoundException(attachmentId));
        return toContentView(attachment);
    }

    private VehicleEntity findVehicle(UUID vehicleId) {
        return vehicles.findById(vehicleId)
                .orElseThrow(() -> new VehicleNotFoundException(vehicleId));
    }

    private VehicleEventEntity findEvent(UUID vehicleId, UUID eventId) {
        return events.findByIdAndVehicle_Id(eventId, vehicleId)
                .orElseThrow(() -> new VehicleEventNotFoundException(eventId));
    }

    private byte[] validateAndRead(MultipartFile file) {
        if (file == null) {
            throw new InvalidAttachmentException("file", "File is required");
        }
        if (file.isEmpty()) {
            throw new InvalidAttachmentException("file", "File must not be empty");
        }
        if (file.getSize() > properties.maxFileSizeBytes()) {
            throw new InvalidAttachmentException("file", "File exceeds maximum size of " + properties.getMaxFileSizeMb() + " MB");
        }
        normalizeContentType(file.getContentType());
        try {
            return file.getBytes();
        } catch (IOException exception) {
            throw new InvalidAttachmentException("file", "File could not be read");
        }
    }

    private String normalizeContentType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            throw new InvalidAttachmentException("file", "Content type is required");
        }
        String normalized = contentType.toLowerCase();
        if (!ALLOWED_CONTENT_TYPES.containsKey(normalized)) {
            throw new InvalidAttachmentException("file", "Unsupported content type: " + contentType);
        }
        return normalized;
    }

    private String storageKey(UUID vehicleId, UUID eventId, String contentType) {
        return vehicleId + "/" + eventId + "/" + UUID.randomUUID() + ALLOWED_CONTENT_TYPES.get(contentType);
    }

    private String originalFilename(String value) {
        if (value == null || value.isBlank()) {
            return "attachment";
        }
        String normalized = value.replace("\\", "/");
        int lastSlash = normalized.lastIndexOf('/');
        return lastSlash >= 0 ? normalized.substring(lastSlash + 1) : normalized;
    }

    private EventAttachmentView toView(EventAttachmentEntity attachment) {
        return new EventAttachmentView(
                attachment.getId(),
                attachment.getVehicle().getId(),
                attachment.getEvent().getId(),
                attachment.getType(),
                attachment.getVisibility(),
                attachment.getOriginalFilename(),
                attachment.getContentType(),
                attachment.getSizeBytes(),
                attachment.getChecksumSha256(),
                attachment.getDescription(),
                attachment.getCreatedAt()
        );
    }

    private PublicAttachmentView toPublicView(EventAttachmentEntity attachment, String publicToken) {
        return new PublicAttachmentView(
                attachment.getId(),
                attachment.getEvent().getId(),
                attachment.getType(),
                attachment.getOriginalFilename(),
                attachment.getContentType(),
                attachment.getSizeBytes(),
                attachment.getChecksumSha256(),
                attachment.getDescription(),
                "/api/v1/public/reports/" + publicToken + "/attachments/" + attachment.getId()
        );
    }

    private AttachmentContentView toContentView(EventAttachmentEntity attachment) {
        return new AttachmentContentView(
                attachment.getOriginalFilename(),
                attachment.getContentType(),
                storage.load(attachment.getStorageKey())
        );
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
