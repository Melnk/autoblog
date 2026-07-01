package com.autoblog.application;

import com.autoblog.infrastructure.persistence.VehicleEntity;
import com.autoblog.infrastructure.persistence.VehicleEventEntity;
import com.autoblog.infrastructure.persistence.VehicleEventJpaRepository;
import com.autoblog.infrastructure.persistence.VehicleJpaRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VehicleApplicationService {

    private static final String DEFAULT_MARKET = "RU";
    private static final String DEFAULT_CURRENCY = "RUB";

    private final VehicleJpaRepository vehicles;
    private final VehicleEventJpaRepository events;
    private final VinNormalizer vinNormalizer;
    private final EventHashService eventHashService;
    private final CanonicalJsonService canonicalJsonService;

    public VehicleApplicationService(
            VehicleJpaRepository vehicles,
            VehicleEventJpaRepository events,
            VinNormalizer vinNormalizer,
            EventHashService eventHashService,
            CanonicalJsonService canonicalJsonService
    ) {
        this.vehicles = vehicles;
        this.events = events;
        this.vinNormalizer = vinNormalizer;
        this.eventHashService = eventHashService;
        this.canonicalJsonService = canonicalJsonService;
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
                trimToNull(command.generation()),
                command.year(),
                trimToNull(command.engine()),
                trimToNull(command.transmission()),
                trimToNull(command.trim()),
                defaultIfBlank(command.market(), DEFAULT_MARKET)
        );

        return toView(vehicles.save(vehicle));
    }

    @Transactional(readOnly = true)
    public VehicleView getVehicle(UUID vehicleId) {
        return toView(findVehicle(vehicleId));
    }

    @Transactional(readOnly = true)
    public VehicleView getVehicleByVin(String rawVin) {
        String vin = vinNormalizer.normalizeAndValidate(rawVin);
        return vehicles.findByVin(vin)
                .map(this::toView)
                .orElseThrow(() -> new VehicleNotFoundException("Vehicle with VIN " + vin + " was not found"));
    }

    @Transactional
    public VehicleEventView addEvent(AddVehicleEventCommand command) {
        VehicleEntity vehicle = findVehicle(command.vehicleId());
        VehicleEventEntity previous = events.findTopByVehicle_IdOrderBySequenceNumberDesc(vehicle.getId())
                .orElse(null);
        long sequenceNumber = previous == null ? 1L : previous.getSequenceNumber() + 1L;
        String payload = canonicalJsonService.canonicalize(command.payload());
        String previousEventHash = previous == null ? null : previous.getEventHash();
        String costCurrency = defaultIfBlank(command.costCurrency(), DEFAULT_CURRENCY);

        String eventHash = eventHashService.hash(new EventHashInput(
                vehicle.getId(),
                sequenceNumber,
                command.type(),
                command.eventDate(),
                command.odometerKm(),
                trimToNull(command.title()),
                trimToNull(command.description()),
                command.costAmount(),
                costCurrency,
                trimToNull(command.serviceName()),
                payload,
                previousEventHash
        ));

        VehicleEventEntity event = new VehicleEventEntity(
                UUID.randomUUID(),
                vehicle,
                sequenceNumber,
                command.type(),
                command.eventDate(),
                command.odometerKm(),
                trimToNull(command.title()),
                trimToNull(command.description()),
                command.costAmount(),
                costCurrency,
                trimToNull(command.serviceName()),
                payload,
                previousEventHash,
                eventHash
        );

        return toView(events.save(event));
    }

    @Transactional(readOnly = true)
    public List<VehicleEventView> getEvents(UUID vehicleId) {
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
                vehicle.getGeneration(),
                vehicle.getYear(),
                vehicle.getEngine(),
                vehicle.getTransmission(),
                vehicle.getTrim(),
                vehicle.getMarket(),
                vehicle.getCreatedAt(),
                vehicle.getUpdatedAt()
        );
    }

    private VehicleEventView toView(VehicleEventEntity event) {
        return new VehicleEventView(
                event.getId(),
                event.getVehicle().getId(),
                event.getSequenceNumber(),
                event.getType(),
                event.getEventDate(),
                event.getOdometerKm(),
                event.getTitle(),
                event.getDescription(),
                event.getCostAmount(),
                event.getCostCurrency(),
                event.getServiceName(),
                canonicalJsonService.parse(event.getPayload()),
                event.getPreviousEventHash(),
                event.getEventHash(),
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

    private String defaultIfBlank(String value, String defaultValue) {
        String trimmed = trimToNull(value);
        return trimmed == null ? defaultValue : trimmed;
    }
}
