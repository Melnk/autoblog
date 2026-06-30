package com.autoblog.application;

import com.autoblog.infrastructure.persistence.VehicleEntity;
import com.autoblog.infrastructure.persistence.VehicleEventEntity;
import com.autoblog.infrastructure.persistence.VehicleEventJpaRepository;
import com.autoblog.infrastructure.persistence.VehicleJpaRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VehicleApplicationService {

    private final VehicleJpaRepository vehicles;
    private final VehicleEventJpaRepository events;
    private final VinNormalizer vinNormalizer;
    private final EventHashService eventHashService;

    public VehicleApplicationService(
            VehicleJpaRepository vehicles,
            VehicleEventJpaRepository events,
            VinNormalizer vinNormalizer,
            EventHashService eventHashService
    ) {
        this.vehicles = vehicles;
        this.events = events;
        this.vinNormalizer = vinNormalizer;
        this.eventHashService = eventHashService;
    }

    @Transactional
    public VehicleView createVehicle(CreateVehicleCommand command) {
        String vin = vinNormalizer.normalizeAndValidate(command.vin());
        if (vehicles.existsByVin(vin)) {
            throw new DuplicateVinException(vin);
        }

        VehicleEntity vehicle = new VehicleEntity(
                UUID.randomUUID(),
                vin,
                trimToNull(command.make()),
                trimToNull(command.model()),
                command.year()
        );

        return toView(vehicles.save(vehicle));
    }

    @Transactional(readOnly = true)
    public VehicleView getVehicle(UUID vehicleId) {
        return toView(findVehicle(vehicleId));
    }

    @Transactional
    public VehicleEventView addEvent(AddVehicleEventCommand command) {
        VehicleEntity vehicle = findVehicle(command.vehicleId());
        VehicleEventEntity previous = events.findTopByVehicle_IdOrderBySequenceNumberDesc(vehicle.getId())
                .orElse(null);
        long sequenceNumber = previous == null ? 1L : previous.getSequenceNumber() + 1L;
        String eventType = trimToNull(command.eventType());
        String description = trimToNull(command.description());
        String previousHash = previous == null ? null : previous.getHash();

        String hash = eventHashService.hash(new EventHashInput(
                vehicle.getId(),
                sequenceNumber,
                command.occurredAt(),
                eventType,
                description,
                previousHash
        ));

        VehicleEventEntity event = new VehicleEventEntity(
                UUID.randomUUID(),
                vehicle,
                sequenceNumber,
                command.occurredAt(),
                eventType,
                description,
                previousHash,
                hash
        );

        return toView(events.save(event));
    }

    @Transactional(readOnly = true)
    public List<VehicleEventView> timeline(UUID vehicleId) {
        findVehicle(vehicleId);
        return events.findByVehicle_IdOrderBySequenceNumberAsc(vehicleId).stream()
                .map(this::toView)
                .toList();
    }

    private VehicleEntity findVehicle(UUID vehicleId) {
        return vehicles.findById(vehicleId)
                .orElseThrow(() -> new VehicleNotFoundException(vehicleId));
    }

    private VehicleView toView(VehicleEntity vehicle) {
        return new VehicleView(
                vehicle.getId(),
                vehicle.getVin(),
                vehicle.getMake(),
                vehicle.getModel(),
                vehicle.getYear(),
                vehicle.getCreatedAt()
        );
    }

    private VehicleEventView toView(VehicleEventEntity event) {
        return new VehicleEventView(
                event.getId(),
                event.getVehicle().getId(),
                event.getSequenceNumber(),
                event.getEventType(),
                event.getOccurredAt(),
                event.getDescription(),
                event.getPreviousHash(),
                event.getHash(),
                event.getCreatedAt()
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
