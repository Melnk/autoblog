package com.autoblog.publicreport.application;

import com.autoblog.application.CanonicalJsonService;
import com.autoblog.application.VehicleNotFoundException;
import com.autoblog.attachment.application.AttachmentContentView;
import com.autoblog.attachment.application.EventAttachmentService;
import com.autoblog.attachment.application.PublicAttachmentView;
import com.autoblog.infrastructure.persistence.VehicleEntity;
import com.autoblog.infrastructure.persistence.VehicleEventEntity;
import com.autoblog.infrastructure.persistence.VehicleEventJpaRepository;
import com.autoblog.infrastructure.persistence.VehicleJpaRepository;
import com.autoblog.publicreport.domain.PublicReportNotFoundException;
import com.autoblog.publicreport.domain.PublicReportStatus;
import com.autoblog.publicreport.infrastructure.PublicVehicleReportEntity;
import com.autoblog.publicreport.infrastructure.PublicVehicleReportJpaRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PublicVehicleReportService {

    private static final String DEFAULT_CURRENCY = "RUB";

    private final PublicVehicleReportJpaRepository reports;
    private final VehicleJpaRepository vehicles;
    private final VehicleEventJpaRepository events;
    private final PublicReportTokenGenerator tokenGenerator;
    private final PublicReportUrlService urlService;
    private final VehicleHashChainVerifier hashChainVerifier;
    private final CanonicalJsonService canonicalJsonService;
    private final QrCodeSvgService qrCodeSvgService;
    private final EventAttachmentService attachments;

    public PublicVehicleReportService(
            PublicVehicleReportJpaRepository reports,
            VehicleJpaRepository vehicles,
            VehicleEventJpaRepository events,
            PublicReportTokenGenerator tokenGenerator,
            PublicReportUrlService urlService,
            VehicleHashChainVerifier hashChainVerifier,
            CanonicalJsonService canonicalJsonService,
            QrCodeSvgService qrCodeSvgService,
            EventAttachmentService attachments
    ) {
        this.reports = reports;
        this.vehicles = vehicles;
        this.events = events;
        this.tokenGenerator = tokenGenerator;
        this.urlService = urlService;
        this.hashChainVerifier = hashChainVerifier;
        this.canonicalJsonService = canonicalJsonService;
        this.qrCodeSvgService = qrCodeSvgService;
        this.attachments = attachments;
    }

    @Transactional
    public PublicReportMetadataView createOrGetActiveReport(UUID vehicleId) {
        VehicleEntity vehicle = findVehicle(vehicleId);
        return reports.findByVehicle_IdAndStatus(vehicleId, PublicReportStatus.ACTIVE)
                .map(this::toMetadataView)
                .orElseGet(() -> toMetadataView(reports.save(new PublicVehicleReportEntity(
                        UUID.randomUUID(),
                        vehicle,
                        generateUniqueToken(),
                        PublicReportStatus.ACTIVE
                ))));
    }

    @Transactional(readOnly = true)
    public PublicVehicleReportView getPublicReport(String publicToken) {
        PublicVehicleReportEntity report = findActiveReport(publicToken);
        VehicleEntity vehicle = report.getVehicle();
        List<VehicleEventEntity> eventEntities = events.findByVehicle_IdOrderBySequenceNumberAsc(vehicle.getId());
        Map<UUID, List<PublicAttachmentView>> attachmentsByEventId = attachments
                .publicAttachments(eventEntities.stream().map(VehicleEventEntity::getId).toList(), publicToken)
                .stream()
                .collect(Collectors.groupingBy(PublicAttachmentView::eventId));

        return new PublicVehicleReportView(
                toInfoView(report),
                toPublicVehicleView(vehicle),
                summary(eventEntities),
                eventEntities.stream()
                        .map(event -> toPublicEventView(
                                event,
                                attachmentsByEventId.getOrDefault(event.getId(), List.of())
                        ))
                        .toList()
        );
    }

    @Transactional(readOnly = true)
    public String getQrSvg(String publicToken) {
        findActiveReport(publicToken);
        return qrCodeSvgService.createSvg(urlService.publicReportUrl(publicToken));
    }

    @Transactional(readOnly = true)
    public AttachmentContentView downloadPublicAttachment(String publicToken, UUID attachmentId) {
        PublicVehicleReportEntity report = findActiveReport(publicToken);
        return attachments.downloadPublic(report.getVehicle().getId(), attachmentId);
    }

    private VehicleEntity findVehicle(UUID vehicleId) {
        return vehicles.findById(vehicleId)
                .orElseThrow(() -> new VehicleNotFoundException(vehicleId));
    }

    private PublicVehicleReportEntity findActiveReport(String publicToken) {
        return reports.findByPublicTokenAndStatus(publicToken, PublicReportStatus.ACTIVE)
                .orElseThrow(() -> new PublicReportNotFoundException(publicToken));
    }

    private String generateUniqueToken() {
        String token;
        do {
            token = tokenGenerator.generate();
        } while (reports.existsByPublicToken(token));
        return token;
    }

    private PublicReportMetadataView toMetadataView(PublicVehicleReportEntity report) {
        return new PublicReportMetadataView(
                report.getId(),
                report.getVehicle().getId(),
                report.getPublicToken(),
                urlService.publicReportUrl(report.getPublicToken()),
                report.getStatus(),
                report.getCreatedAt(),
                report.getUpdatedAt()
        );
    }

    private PublicReportInfoView toInfoView(PublicVehicleReportEntity report) {
        return new PublicReportInfoView(
                report.getId(),
                report.getPublicToken(),
                report.getStatus(),
                report.getCreatedAt(),
                report.getUpdatedAt()
        );
    }

    private PublicVehicleView toPublicVehicleView(VehicleEntity vehicle) {
        return new PublicVehicleView(
                vehicle.getVin(),
                vehicle.getMake(),
                vehicle.getModel(),
                vehicle.getGeneration(),
                vehicle.getYear(),
                vehicle.getEngine(),
                vehicle.getTransmission(),
                vehicle.getTrim(),
                vehicle.getMarket()
        );
    }

    private PublicReportEventView toPublicEventView(
            VehicleEventEntity event,
            List<PublicAttachmentView> attachments
    ) {
        return new PublicReportEventView(
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
                attachments
        );
    }

    private PublicReportSummaryView summary(List<VehicleEventEntity> events) {
        LocalDate firstEventDate = events.stream()
                .map(VehicleEventEntity::getEventDate)
                .min(Comparator.naturalOrder())
                .orElse(null);
        LocalDate lastEventDate = events.stream()
                .map(VehicleEventEntity::getEventDate)
                .max(Comparator.naturalOrder())
                .orElse(null);
        Integer latestOdometerKm = events.reversed().stream()
                .map(VehicleEventEntity::getOdometerKm)
                .filter(value -> value != null)
                .findFirst()
                .orElse(null);
        BigDecimal totalKnownCostAmount = events.stream()
                .map(VehicleEventEntity::getCostAmount)
                .filter(value -> value != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new PublicReportSummaryView(
                events.size(),
                firstEventDate,
                lastEventDate,
                latestOdometerKm,
                totalKnownCostAmount,
                DEFAULT_CURRENCY,
                hashChainVerifier.verify(events)
        );
    }
}
