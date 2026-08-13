package com.autoblog.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class DomainMetrics {

    private final Counter attachmentsUploaded;
    private final Counter eventsCreated;
    private final Counter publicReportsDisabled;
    private final Counter publicReportsRotated;

    public DomainMetrics(MeterRegistry registry) {
        attachmentsUploaded = registry.counter("autoblog.attachments.uploaded");
        eventsCreated = registry.counter("autoblog.vehicle.events.created");
        publicReportsDisabled = registry.counter("autoblog.public.reports.disabled");
        publicReportsRotated = registry.counter("autoblog.public.reports.rotated");
    }

    public void attachmentUploaded() {
        attachmentsUploaded.increment();
    }

    public void eventCreated() {
        eventsCreated.increment();
    }

    public void publicReportDisabled() {
        publicReportsDisabled.increment();
    }

    public void publicReportRotated() {
        publicReportsRotated.increment();
    }
}
