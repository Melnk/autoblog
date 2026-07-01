package com.autoblog.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "vehicles",
        indexes = @Index(name = "idx_vehicles_vin", columnList = "vin"),
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

    @Column(length = 120)
    private String generation;

    @Column(name = "model_year")
    private Integer year;

    @Column(length = 120)
    private String engine;

    @Column(length = 64)
    private String transmission;

    @Column(name = "trim_name", length = 120)
    private String trim;

    @Column(nullable = false, length = 8)
    private String market;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected VehicleEntity() {
    }

    public VehicleEntity(
            UUID id,
            String vin,
            String make,
            String model,
            String generation,
            Integer year,
            String engine,
            String transmission,
            String trim,
            String market
    ) {
        this.id = id;
        this.vin = vin;
        this.make = make;
        this.model = model;
        this.generation = generation;
        this.year = year;
        this.engine = engine;
        this.transmission = transmission;
        this.trim = trim;
        this.market = market;
    }

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (market == null || market.isBlank()) {
            market = "RU";
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

    public String getVin() {
        return vin;
    }

    public String getMake() {
        return make;
    }

    public String getModel() {
        return model;
    }

    public String getGeneration() {
        return generation;
    }

    public Integer getYear() {
        return year;
    }

    public String getEngine() {
        return engine;
    }

    public String getTransmission() {
        return transmission;
    }

    public String getTrim() {
        return trim;
    }

    public String getMarket() {
        return market;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
