import { CalendarDays, Coins, Gauge, MapPin, Wrench } from "lucide-react";
import type { ReactNode } from "react";
import { AttachmentPanel } from "@/components/vehicles/attachment-panel";
import { Badge } from "@/components/ui/badge";
import { Card } from "@/components/ui/card";
import type { VehicleEventDto } from "@/lib/api/types";
import { formatDate, formatKm, formatMoney, shortHash } from "@/lib/format";
import { getEnumLabel, useLanguage } from "@/lib/i18n";

export function EventTimeline({
  vehicleId,
  events,
  canEdit
}: {
  vehicleId: string;
  events: VehicleEventDto[];
  canEdit: boolean;
}) {
  const { language, t } = useLanguage();

  if (events.length === 0) {
    return (
      <Card>
        <h3 className="text-lg font-bold text-ink">{t("events.emptyTitle")}</h3>
        <p className="mt-2 text-sm text-muted">{t("events.emptyDescription")}</p>
      </Card>
    );
  }

  return (
    <div className="relative min-w-0 space-y-5">
      <div className="timeline-line absolute bottom-8 left-4 top-8 w-px sm:left-5" />
      {events.map((event) => (
        <div key={event.id} className="relative min-w-0 pl-10 sm:pl-14">
          <div className="absolute left-0 top-5 flex h-8 w-8 items-center justify-center rounded-full border border-brand-yellow bg-brand-yellow text-ink sm:h-10 sm:w-10">
            <Wrench className="h-5 w-5" />
          </div>
          <Card>
            <div className="space-y-4">
              <div className="min-w-0">
                <div className="flex flex-wrap items-center gap-2">
                  <Badge>#{event.sequenceNumber}</Badge>
                  <Badge>{getEnumLabel(language, "vehicleEventType", event.type)}</Badge>
                </div>
                <h3 className="mt-3 break-words text-xl font-bold text-ink">{event.title}</h3>
                {event.description ? <p className="mt-2 break-words text-sm leading-6 text-muted">{event.description}</p> : null}
              </div>
              <div className="grid min-w-0 gap-2 text-sm sm:grid-cols-2">
                <Info icon={<CalendarDays className="h-4 w-4" />} label={t("label.date")} value={formatDate(event.eventDate, language)} />
                <Info icon={<Gauge className="h-4 w-4" />} label={t("label.odometer")} value={formatKm(event.odometerKm, language)} />
                <Info icon={<Coins className="h-4 w-4" />} label={t("label.cost")} value={formatMoney(event.costAmount, event.costCurrency, language)} />
                <Info icon={<MapPin className="h-4 w-4" />} label={t("label.service")} value={event.serviceName || "—"} />
              </div>
            </div>
            <details className="mt-4 rounded-2xl border border-line p-3 text-xs text-muted">
              <summary className="cursor-pointer font-medium text-muted focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-4 focus-visible:outline-ink">{language === "ru" ? "Технические сведения" : "Technical details"}</summary>
              <div className="mt-3 grid gap-2 break-all md:grid-cols-2">
                <div>{language === "ru" ? "Предыдущая запись" : "Previous record"}: <span className="font-mono text-ink">{shortHash(event.previousEventHash)}</span></div>
                <div>{language === "ru" ? "Эта запись" : "This record"}: <span className="font-mono text-ink">{shortHash(event.eventHash)}</span></div>
              </div>
              {event.payload ? (
                <pre className="mt-3 max-h-40 overflow-auto rounded bg-canvas p-3 text-[11px] text-muted">
                  {JSON.stringify(event.payload, null, 2)}
                </pre>
              ) : null}
            </details>
            <AttachmentPanel
              vehicleId={vehicleId}
              eventId={event.id}
              canUpload={canEdit}
            />
          </Card>
        </div>
      ))}
    </div>
  );
}

function Info({ icon, label, value }: { icon: ReactNode; label: string; value: string }) {
  return (
    <div className="min-w-0 rounded-2xl bg-canvas p-3">
      <div className="flex items-center gap-2 text-xs uppercase tracking-wide text-muted">
        {icon}
        {label}
      </div>
      <div className="mt-1 break-words font-semibold text-ink">{value}</div>
    </div>
  );
}
