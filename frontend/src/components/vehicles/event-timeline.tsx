import { CalendarDays, Coins, Gauge, Wrench } from "lucide-react";
import type { ReactNode } from "react";
import { AttachmentPanel } from "@/components/vehicles/attachment-panel";
import { Badge } from "@/components/ui/badge";
import { Card } from "@/components/ui/card";
import type { EventAttachmentDto, VehicleEventDto } from "@/lib/api/types";
import { formatDate, formatMoney, shortHash } from "@/lib/utils";

export function EventTimeline({
  vehicleId,
  events,
  attachmentsByEvent
}: {
  vehicleId: string;
  events: VehicleEventDto[];
  attachmentsByEvent: Record<string, EventAttachmentDto[]>;
}) {
  if (events.length === 0) {
    return (
      <Card>
        <h3 className="text-lg font-bold text-white">Событий пока нет</h3>
        <p className="mt-2 text-sm text-slate-400">Добавьте первое обслуживание, ремонт или осмотр.</p>
      </Card>
    );
  }

  return (
    <div className="relative space-y-5">
      <div className="timeline-line absolute bottom-8 left-5 top-8 w-px" />
      {events.map((event) => (
        <div key={event.id} className="relative pl-12">
          <div className="absolute left-0 top-5 flex h-10 w-10 items-center justify-center rounded-full border border-blue-400/40 bg-surface-900 text-neon-cyan shadow-glow">
            <Wrench className="h-5 w-5" />
          </div>
          <Card>
            <div className="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
              <div>
                <div className="flex flex-wrap items-center gap-2">
                  <Badge>#{event.sequenceNumber}</Badge>
                  <Badge>{event.type}</Badge>
                </div>
                <h3 className="mt-3 text-xl font-bold text-white">{event.title}</h3>
                {event.description ? <p className="mt-2 text-sm leading-6 text-slate-300">{event.description}</p> : null}
              </div>
              <div className="grid gap-2 text-sm sm:grid-cols-3 lg:min-w-[460px]">
                <Info icon={<CalendarDays className="h-4 w-4" />} label="Дата" value={formatDate(event.eventDate)} />
                <Info icon={<Gauge className="h-4 w-4" />} label="Пробег" value={event.odometerKm ? `${event.odometerKm.toLocaleString("ru-RU")} км` : "—"} />
                <Info icon={<Coins className="h-4 w-4" />} label="Стоимость" value={formatMoney(event.costAmount, event.costCurrency)} />
              </div>
            </div>
            <div className="mt-4 rounded-lg border border-slate-800 bg-black/20 p-3 text-xs text-slate-500">
              <div className="grid gap-2 md:grid-cols-2">
                <div>prev: <span className="text-slate-300">{shortHash(event.previousEventHash)}</span></div>
                <div>hash: <span className="text-slate-300">{shortHash(event.eventHash)}</span></div>
              </div>
              {event.payload ? (
                <pre className="mt-3 max-h-40 overflow-auto rounded bg-black/30 p-3 text-[11px] text-slate-400">
                  {JSON.stringify(event.payload, null, 2)}
                </pre>
              ) : null}
            </div>
            <AttachmentPanel
              vehicleId={vehicleId}
              eventId={event.id}
              initialAttachments={attachmentsByEvent[event.id] ?? []}
            />
          </Card>
        </div>
      ))}
    </div>
  );
}

function Info({ icon, label, value }: { icon: ReactNode; label: string; value: string }) {
  return (
    <div className="rounded-lg border border-slate-800 bg-black/20 p-3">
      <div className="flex items-center gap-2 text-xs uppercase tracking-wide text-slate-500">
        {icon}
        {label}
      </div>
      <div className="mt-1 font-semibold text-slate-200">{value}</div>
    </div>
  );
}
