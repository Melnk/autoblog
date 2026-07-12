package com.autoblog.trust.application;

import com.autoblog.access.application.VehicleAccessService;
import com.autoblog.application.VehicleNotFoundException;
import com.autoblog.attachment.domain.AttachmentVisibility;
import com.autoblog.attachment.infrastructure.EventAttachmentEntity;
import com.autoblog.attachment.infrastructure.EventAttachmentJpaRepository;
import com.autoblog.infrastructure.persistence.VehicleEventEntity;
import com.autoblog.infrastructure.persistence.VehicleEventJpaRepository;
import com.autoblog.infrastructure.persistence.VehicleJpaRepository;
import com.autoblog.publicreport.application.VehicleHashChainVerifier;
import com.autoblog.reminder.domain.ReminderStatus;
import com.autoblog.reminder.infrastructure.MaintenanceReminderEntity;
import com.autoblog.reminder.infrastructure.MaintenanceReminderJpaRepository;
import com.autoblog.trust.domain.TrustScoreLevel;
import com.autoblog.trust.domain.TrustSignalImpact;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TrustScoreService {

    private static final int BASE_SCORE = 50;

    private final VehicleJpaRepository vehicles;
    private final VehicleEventJpaRepository events;
    private final EventAttachmentJpaRepository attachments;
    private final MaintenanceReminderJpaRepository reminders;
    private final VehicleHashChainVerifier hashChainVerifier;
    private final VehicleAccessService vehicleAccess;

    public TrustScoreService(
            VehicleJpaRepository vehicles,
            VehicleEventJpaRepository events,
            EventAttachmentJpaRepository attachments,
            MaintenanceReminderJpaRepository reminders,
            VehicleHashChainVerifier hashChainVerifier,
            VehicleAccessService vehicleAccess
    ) {
        this.vehicles = vehicles;
        this.events = events;
        this.attachments = attachments;
        this.reminders = reminders;
        this.hashChainVerifier = hashChainVerifier;
        this.vehicleAccess = vehicleAccess;
    }

    @Transactional(readOnly = true)
    public TrustScoreView calculateForPrivateVehicle(UUID vehicleId) {
        vehicleAccess.requireViewAccess(vehicleId);
        return calculate(vehicleId);
    }

    @Transactional(readOnly = true)
    public TrustScoreView calculateForPublicVehicle(UUID vehicleId) {
        return calculate(vehicleId);
    }

    private TrustScoreView calculate(UUID vehicleId) {
        vehicles.findById(vehicleId).orElseThrow(() -> new VehicleNotFoundException(vehicleId));

        List<VehicleEventEntity> vehicleEvents = events.findByVehicle_IdOrderBySequenceNumberAsc(vehicleId);
        List<EventAttachmentEntity> vehicleAttachments = attachments.findByVehicle_Id(vehicleId);
        List<MaintenanceReminderEntity> vehicleReminders = reminders.findByVehicle_Id(vehicleId);
        LocalDate today = LocalDate.now();

        TrustScoreMetricsView metrics = metrics(vehicleEvents, vehicleAttachments, vehicleReminders, today);
        List<TrustScoreSignalView> signals = signals(metrics, vehicleReminders);
        int score = clamp(signals.stream().mapToInt(TrustScoreSignalView::points).sum());
        TrustScoreLevel level = level(score, metrics.eventsCount());

        return new TrustScoreView(
                vehicleId,
                score,
                level,
                Instant.now(),
                summary(level),
                signals,
                metrics
        );
    }

    private TrustScoreMetricsView metrics(
            List<VehicleEventEntity> events,
            List<EventAttachmentEntity> attachments,
            List<MaintenanceReminderEntity> reminders,
            LocalDate today
    ) {
        int eventsCount = events.size();
        Set<UUID> eventIdsWithAttachments = new HashSet<>();
        int publicAttachmentsCount = 0;
        int privateAttachmentsCount = 0;
        for (EventAttachmentEntity attachment : attachments) {
            eventIdsWithAttachments.add(attachment.getEvent().getId());
            if (attachment.getVisibility() == AttachmentVisibility.PUBLIC) {
                publicAttachmentsCount++;
            } else {
                privateAttachmentsCount++;
            }
        }

        List<Integer> odometerReadings = events.stream()
                .map(VehicleEventEntity::getOdometerKm)
                .filter(value -> value != null)
                .toList();
        boolean odometerConsistent = odometerConsistent(odometerReadings);
        Integer latestOdometerKm = odometerReadings.isEmpty() ? null : odometerReadings.getLast();
        LocalDate firstEventDate = events.stream()
                .map(VehicleEventEntity::getEventDate)
                .min(Comparator.naturalOrder())
                .orElse(null);
        LocalDate lastEventDate = events.stream()
                .map(VehicleEventEntity::getEventDate)
                .max(Comparator.naturalOrder())
                .orElse(null);
        BigDecimal totalKnownCostAmount = events.stream()
                .map(VehicleEventEntity::getCostAmount)
                .filter(value -> value != null)
                .reduce(BigDecimal::add)
                .orElse(null);
        int activeRemindersCount = (int) reminders.stream()
                .filter(reminder -> reminder.getStatus() == ReminderStatus.ACTIVE)
                .count();
        int overdueRemindersCount = (int) reminders.stream()
                .filter(reminder -> isOverdue(reminder, latestOdometerKm, today))
                .count();

        return new TrustScoreMetricsView(
                eventsCount,
                eventIdsWithAttachments.size(),
                publicAttachmentsCount,
                privateAttachmentsCount,
                odometerReadings.size(),
                latestOdometerKm,
                totalKnownCostAmount,
                hashChainVerifier.verify(events),
                odometerConsistent,
                firstEventDate,
                lastEventDate,
                activeRemindersCount,
                overdueRemindersCount
        );
    }

    private List<TrustScoreSignalView> signals(
            TrustScoreMetricsView metrics,
            List<MaintenanceReminderEntity> reminders
    ) {
        SignalBuilder builder = new SignalBuilder();
        builder.add("BASE_SCORE", TrustSignalImpact.NEUTRAL, BASE_SCORE, "Base trust score");

        if (metrics.eventsCount() == 0) {
            builder.add("NO_EVENTS", TrustSignalImpact.NEGATIVE, -25, "No vehicle history events are recorded");
        }
        if (metrics.eventsCount() > 0 && metrics.hashChainValid()) {
            builder.add("HASH_CHAIN_VALID", TrustSignalImpact.POSITIVE, 15, "Event hash-chain is valid");
        }
        if (metrics.eventsCount() > 0 && !metrics.hashChainValid()) {
            builder.add("HASH_CHAIN_INVALID", TrustSignalImpact.NEGATIVE, -30, "Event hash-chain is invalid");
        }
        if (metrics.eventsCount() >= 3) {
            builder.add("HAS_3_EVENTS", TrustSignalImpact.POSITIVE, 10, "Vehicle has at least 3 history events");
        }
        if (metrics.eventsCount() >= 5) {
            builder.add("HAS_5_EVENTS", TrustSignalImpact.POSITIVE, 10, "Vehicle has at least 5 history events");
        }
        if (metrics.eventsWithAttachmentsCount() > 0) {
            builder.add("HAS_EVENT_ATTACHMENTS", TrustSignalImpact.POSITIVE, 10, "History includes evidence attachments");
        } else {
            builder.add("NO_ATTACHMENTS", TrustSignalImpact.NEGATIVE, -10, "No evidence attachments are recorded");
        }
        if (metrics.publicAttachmentsCount() > 0) {
            builder.add("HAS_PUBLIC_ATTACHMENTS", TrustSignalImpact.POSITIVE, 5, "Public evidence is available for buyers");
        }
        if (!metrics.odometerConsistent()) {
            builder.add("ODOMETER_INCONSISTENCY", TrustSignalImpact.NEGATIVE, -25, "Odometer readings decrease in the timeline");
        } else if (metrics.odometerEventsCount() >= 2) {
            builder.add("ODOMETER_CONSISTENT", TrustSignalImpact.POSITIVE, 10, "Odometer readings are non-decreasing");
        }
        if (metrics.odometerEventsCount() == 0) {
            builder.add("NO_ODOMETER_DATA", TrustSignalImpact.NEGATIVE, -10, "No odometer data is recorded");
        }
        if (hasRecentEvent(metrics.lastEventDate())) {
            builder.add("HAS_RECENT_EVENT", TrustSignalImpact.POSITIVE, 10, "Vehicle has a recent history event");
        }
        if (lastEventIsOld(metrics.lastEventDate())) {
            builder.add("LAST_EVENT_OLD", TrustSignalImpact.NEGATIVE, -10, "Last history event is older than 24 months");
        }
        if (!reminders.isEmpty()) {
            builder.add("HAS_REMINDERS", TrustSignalImpact.POSITIVE, 5, "Vehicle has maintenance reminders configured");
        }
        if (metrics.overdueRemindersCount() > 0) {
            builder.add("HAS_OVERDUE_REMINDERS", TrustSignalImpact.NEGATIVE, -5, "Vehicle has overdue maintenance reminders");
        }

        return builder.signals();
    }

    private boolean odometerConsistent(List<Integer> odometerReadings) {
        for (int index = 1; index < odometerReadings.size(); index++) {
            if (odometerReadings.get(index) < odometerReadings.get(index - 1)) {
                return false;
            }
        }
        return true;
    }

    private boolean isOverdue(MaintenanceReminderEntity reminder, Integer latestOdometerKm, LocalDate today) {
        if (reminder.getStatus() != ReminderStatus.ACTIVE) {
            return false;
        }
        if (reminder.getDueDate() != null && reminder.getDueDate().isBefore(today)) {
            return true;
        }
        return latestOdometerKm != null
                && reminder.getDueOdometerKm() != null
                && reminder.getDueOdometerKm() <= latestOdometerKm;
    }

    private boolean hasRecentEvent(LocalDate lastEventDate) {
        return lastEventDate != null && !lastEventDate.isBefore(LocalDate.now().minusMonths(12));
    }

    private boolean lastEventIsOld(LocalDate lastEventDate) {
        return lastEventDate != null && lastEventDate.isBefore(LocalDate.now().minusMonths(24));
    }

    private TrustScoreLevel level(int score, int eventsCount) {
        if (eventsCount == 0) {
            return TrustScoreLevel.UNKNOWN;
        }
        if (score >= 80) {
            return TrustScoreLevel.HIGH;
        }
        if (score >= 50) {
            return TrustScoreLevel.MEDIUM;
        }
        return TrustScoreLevel.LOW;
    }

    private String summary(TrustScoreLevel level) {
        return switch (level) {
            case HIGH -> "История автомобиля выглядит хорошо подтвержденной.";
            case MEDIUM -> "История автомобиля частично подтверждена, но есть пробелы.";
            case LOW -> "История автомобиля требует дополнительной проверки.";
            case UNKNOWN -> "Недостаточно данных для оценки истории автомобиля.";
        };
    }

    private int clamp(int score) {
        return Math.max(0, Math.min(100, score));
    }

    private static class SignalBuilder {
        private final List<TrustScoreSignalView> signals = new java.util.ArrayList<>();

        void add(String code, TrustSignalImpact impact, int points, String message) {
            signals.add(new TrustScoreSignalView(code, impact, points, message));
        }

        List<TrustScoreSignalView> signals() {
            return List.copyOf(signals);
        }
    }
}
