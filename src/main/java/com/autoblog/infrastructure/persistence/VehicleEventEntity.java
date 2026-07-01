package com.autoblog.infrastructure.persistence;

import com.autoblog.application.VehicleEventType;
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
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(
        name = "vehicle_events",
        indexes = {
                @Index(name = "idx_vehicle_events_vehicle_id", columnList = "vehicle_id"),
                @Index(name = "idx_vehicle_events_vehicle_sequence", columnList = "vehicle_id, sequence_number"),
                @Index(name = "idx_vehicle_events_event_hash", columnList = "event_hash")
        },
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

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 32)
    private VehicleEventType type;

    @Column(nullable = false)
    private LocalDate eventDate;

    private Integer odometerKm;

    @Column(nullable = false, length = 240)
    private String title;

    @Column(length = 2000)
    private String description;

    @Column(precision = 19, scale = 2)
    private BigDecimal costAmount;

    @Column(nullable = false, length = 3)
    private String costCurrency;

    @Column(length = 240)
    private String serviceName;

    @Column(columnDefinition = "text")
    private String payload;

    @Column(length = 64)
    private String previousEventHash;

    @Column(nullable = false, length = 64)
    private String eventHash;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected VehicleEventEntity() {
    }

    public VehicleEventEntity(
            UUID id,
            VehicleEntity vehicle,
            long sequenceNumber,
            VehicleEventType type,
            LocalDate eventDate,
            Integer odometerKm,
            String title,
            String description,
            BigDecimal costAmount,
            String costCurrency,
            String serviceName,
            String payload,
            String previousEventHash,
            String eventHash
    ) {
        this.id = id;
        this.vehicle = vehicle;
        this.sequenceNumber = sequenceNumber;
        this.type = type;
        this.eventDate = eventDate;
        this.odometerKm = odometerKm;
        this.title = title;
        this.description = description;
        this.costAmount = costAmount;
        this.costCurrency = costCurrency;
        this.serviceName = serviceName;
        this.payload = payload;
        this.previousEventHash = previousEventHash;
        this.eventHash = eventHash;
    }

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (costCurrency == null || costCurrency.isBlank()) {
            costCurrency = "RUB";
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

    public VehicleEventType getType() {
        return type;
    }

    public LocalDate getEventDate() {
        return eventDate;
    }

    public Integer getOdometerKm() {
        return odometerKm;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getCostAmount() {
        return costAmount;
    }

    public String getCostCurrency() {
        return costCurrency;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getPayload() {
        return payload;
    }

    public String getPreviousEventHash() {
        return previousEventHash;
    }

    public String getEventHash() {
        return eventHash;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
