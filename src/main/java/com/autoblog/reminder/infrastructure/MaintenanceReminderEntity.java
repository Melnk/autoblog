package com.autoblog.reminder.infrastructure;

import com.autoblog.infrastructure.persistence.VehicleEntity;
import com.autoblog.reminder.domain.ReminderStatus;
import com.autoblog.reminder.domain.ReminderType;
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
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(
        name = "maintenance_reminders",
        indexes = {
                @Index(name = "idx_maintenance_reminders_vehicle_id", columnList = "vehicle_id"),
                @Index(name = "idx_maintenance_reminders_vehicle_status", columnList = "vehicle_id, status"),
                @Index(name = "idx_maintenance_reminders_due_date", columnList = "due_date"),
                @Index(name = "idx_maintenance_reminders_due_odometer_km", columnList = "due_odometer_km")
        }
)
public class MaintenanceReminderEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private VehicleEntity vehicle;

    @Column(nullable = false, length = 240)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ReminderType type;

    private LocalDate dueDate;

    private Integer dueOdometerKm;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ReminderStatus status;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    private Instant completedAt;

    private Instant cancelledAt;

    protected MaintenanceReminderEntity() {
    }

    public MaintenanceReminderEntity(
            UUID id,
            VehicleEntity vehicle,
            String title,
            String description,
            ReminderType type,
            LocalDate dueDate,
            Integer dueOdometerKm
    ) {
        this.id = id;
        this.vehicle = vehicle;
        this.title = title;
        this.description = description;
        this.type = type;
        this.dueDate = dueDate;
        this.dueOdometerKm = dueOdometerKm;
        this.status = ReminderStatus.ACTIVE;
    }

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (status == null) {
            status = ReminderStatus.ACTIVE;
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

    public void complete(Instant now) {
        if (status == ReminderStatus.COMPLETED) {
            return;
        }
        status = ReminderStatus.COMPLETED;
        completedAt = now;
        updatedAt = now;
    }

    public void cancel(Instant now) {
        if (status == ReminderStatus.CANCELLED) {
            return;
        }
        status = ReminderStatus.CANCELLED;
        cancelledAt = now;
        updatedAt = now;
    }

    public UUID getId() {
        return id;
    }

    public VehicleEntity getVehicle() {
        return vehicle;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public ReminderType getType() {
        return type;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public Integer getDueOdometerKm() {
        return dueOdometerKm;
    }

    public ReminderStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public Instant getCancelledAt() {
        return cancelledAt;
    }
}
