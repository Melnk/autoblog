package com.autoblog.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
        name = "vehicle_events",
        indexes = @Index(name = "idx_vehicle_events_vehicle_sequence", columnList = "vehicle_id, sequence_number"),
        uniqueConstraints = @UniqueConstraint(
                name = "uk_vehicle_events_vehicle_sequence",
                columnNames = {"vehicle_id", "sequence_number"}
        )
)
public class VehicleEventEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private VehicleEntity vehicle;

    @Column(nullable = false)
    private long sequenceNumber;

    @Column(nullable = false)
    private Instant occurredAt;

    @Column(nullable = false, length = 64)
    private String eventType;

    @Column(length = 2000)
    private String description;

    @Column(length = 64)
    private String previousHash;

    @Column(nullable = false, length = 64)
    private String hash;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected VehicleEventEntity() {
    }

    public VehicleEventEntity(
            UUID id,
            VehicleEntity vehicle,
            long sequenceNumber,
            Instant occurredAt,
            String eventType,
            String description,
            String previousHash,
            String hash
    ) {
        this.id = id;
        this.vehicle = vehicle;
        this.sequenceNumber = sequenceNumber;
        this.occurredAt = occurredAt;
        this.eventType = eventType;
        this.description = description;
        this.previousHash = previousHash;
        this.hash = hash;
    }

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
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

    public long getSequenceNumber() {
        return sequenceNumber;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public String getEventType() {
        return eventType;
    }

    public String getDescription() {
        return description;
    }

    public String getPreviousHash() {
        return previousHash;
    }

    public String getHash() {
        return hash;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
