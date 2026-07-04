package com.autoblog.publicreport.application;

import com.autoblog.application.EventHashInput;
import com.autoblog.application.EventHashService;
import com.autoblog.infrastructure.persistence.VehicleEventEntity;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;

@Service
public class VehicleHashChainVerifier {

    private final EventHashService eventHashService;

    public VehicleHashChainVerifier(EventHashService eventHashService) {
        this.eventHashService = eventHashService;
    }

    public boolean verify(List<VehicleEventEntity> events) {
        String previousHash = null;
        for (VehicleEventEntity event : events) {
            if (!Objects.equals(previousHash, event.getPreviousEventHash())) {
                return false;
            }

            String recomputedHash = eventHashService.hash(new EventHashInput(
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
                    event.getPayload(),
                    event.getPreviousEventHash()
            ));
            if (!Objects.equals(recomputedHash, event.getEventHash())) {
                return false;
            }

            previousHash = event.getEventHash();
        }
        return true;
    }
}
