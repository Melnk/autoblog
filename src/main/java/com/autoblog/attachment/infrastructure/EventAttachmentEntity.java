package com.autoblog.attachment.infrastructure;

import com.autoblog.attachment.domain.AttachmentType;
import com.autoblog.attachment.domain.AttachmentVisibility;
import com.autoblog.infrastructure.persistence.VehicleEntity;
import com.autoblog.infrastructure.persistence.VehicleEventEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "event_attachments",
        indexes = {
                @Index(name = "idx_event_attachments_vehicle_id", columnList = "vehicle_id"),
                @Index(name = "idx_event_attachments_event_id", columnList = "event_id"),
                @Index(name = "idx_event_attachments_visibility", columnList = "visibility"),
                @Index(name = "idx_event_attachments_checksum_sha256", columnList = "checksum_sha256")
        },
        uniqueConstraints = @UniqueConstraint(
                name = "uk_event_attachments_storage_key",
                columnNames = "storage_key"
        )
)
public class EventAttachmentEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private VehicleEntity vehicle;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private VehicleEventEntity event;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private AttachmentType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private AttachmentVisibility visibility;

    @Column(nullable = false, length = 255)
    private String originalFilename;

    @Column(nullable = false, length = 120)
    private String contentType;

    @Column(nullable = false)
    private long sizeBytes;

    @Column(nullable = false, length = 64)
    private String checksumSha256;

    @Column(nullable = false, length = 512, unique = true)
    private String storageKey;

    @Column(columnDefinition = "text")
    private String description;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected EventAttachmentEntity() {
    }

    public EventAttachmentEntity(
            UUID id,
            VehicleEntity vehicle,
            VehicleEventEntity event,
            AttachmentType type,
            AttachmentVisibility visibility,
            String originalFilename,
            String contentType,
            long sizeBytes,
            String checksumSha256,
            String storageKey,
            String description
    ) {
        this.id = id;
        this.vehicle = vehicle;
        this.event = event;
        this.type = type;
        this.visibility = visibility;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.sizeBytes = sizeBytes;
        this.checksumSha256 = checksumSha256;
        this.storageKey = storageKey;
        this.description = description;
    }

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (visibility == null) {
            visibility = AttachmentVisibility.PRIVATE;
        }
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public UUID getId() {
        return id;
    }

    public VehicleEntity getVehicle() {
        return vehicle;
    }

    public VehicleEventEntity getEvent() {
        return event;
    }

    public AttachmentType getType() {
        return type;
    }

    public AttachmentVisibility getVisibility() {
        return visibility;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public String getContentType() {
        return contentType;
    }

    public long getSizeBytes() {
        return sizeBytes;
    }

    public String getChecksumSha256() {
        return checksumSha256;
    }

    public String getStorageKey() {
        return storageKey;
    }

    public String getDescription() {
        return description;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
