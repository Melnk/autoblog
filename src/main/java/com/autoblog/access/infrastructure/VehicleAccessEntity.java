package com.autoblog.access.infrastructure;

import com.autoblog.access.domain.VehicleAccessRole;
import com.autoblog.identity.infrastructure.UserAccountEntity;
import com.autoblog.infrastructure.persistence.VehicleEntity;
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
        name = "vehicle_access",
        indexes = {
                @Index(name = "idx_vehicle_access_vehicle_id", columnList = "vehicle_id"),
                @Index(name = "idx_vehicle_access_user_id", columnList = "user_id"),
                @Index(name = "idx_vehicle_access_vehicle_role", columnList = "vehicle_id, role")
        },
        uniqueConstraints = @UniqueConstraint(
                name = "uk_vehicle_access_vehicle_user",
                columnNames = {"vehicle_id", "user_id"}
        )
)
public class VehicleAccessEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private VehicleEntity vehicle;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccountEntity user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private VehicleAccessRole role;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected VehicleAccessEntity() {
    }

    public VehicleAccessEntity(UUID id, VehicleEntity vehicle, UserAccountEntity user, VehicleAccessRole role) {
        this.id = id;
        this.vehicle = vehicle;
        this.user = user;
        this.role = role;
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

    public UserAccountEntity getUser() {
        return user;
    }

    public VehicleAccessRole getRole() {
        return role;
    }

    public void setRole(VehicleAccessRole role) {
        this.role = role;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
