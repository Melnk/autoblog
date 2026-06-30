package com.autoblog.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "vehicles",
        uniqueConstraints = @UniqueConstraint(name = "uk_vehicles_vin", columnNames = "vin")
)
public class VehicleEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 17, unique = true)
    private String vin;

    @Column(length = 120)
    private String make;

    @Column(length = 120)
    private String model;

    @Column(name = "model_year")
    private Integer year;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected VehicleEntity() {
    }

    public VehicleEntity(UUID id, String vin, String make, String model, Integer year) {
        this.id = id;
        this.vin = vin;
        this.make = make;
        this.model = model;
        this.year = year;
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

    public String getVin() {
        return vin;
    }

    public String getMake() {
        return make;
    }

    public String getModel() {
        return model;
    }

    public Integer getYear() {
        return year;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
