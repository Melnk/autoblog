package com.autoblog.publicreport.infrastructure;

import com.autoblog.infrastructure.persistence.VehicleEntity;
import com.autoblog.publicreport.domain.PublicReportStatus;
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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "public_vehicle_reports",
        indexes = {
                @Index(name = "idx_public_vehicle_reports_public_token", columnList = "public_token"),
                @Index(name = "idx_public_vehicle_reports_vehicle_id", columnList = "vehicle_id"),
                @Index(name = "idx_public_vehicle_reports_status", columnList = "status")
        },
        uniqueConstraints = @UniqueConstraint(
                name = "uk_public_vehicle_reports_public_token",
                columnNames = "public_token"
        )
)
public class PublicVehicleReportEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private VehicleEntity vehicle;

    @Column(nullable = false, length = 96, unique = true)
    private String publicToken;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private PublicReportStatus status;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected PublicVehicleReportEntity() {
    }

    public PublicVehicleReportEntity(UUID id, VehicleEntity vehicle, String publicToken, PublicReportStatus status) {
        this.id = id;
        this.vehicle = vehicle;
        this.publicToken = publicToken;
        this.status = status;
    }

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (status == null) {
            status = PublicReportStatus.ACTIVE;
        }
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public VehicleEntity getVehicle() {
        return vehicle;
    }

    public String getPublicToken() {
        return publicToken;
    }

    public PublicReportStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
