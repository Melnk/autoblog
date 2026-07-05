"use client";

import { ArrowLeft, Plus } from "lucide-react";
import Link from "next/link";
import { useEffect, useState } from "react";
import { useAuth } from "@/components/auth/auth-provider";
import { ProtectedRoute } from "@/components/auth/protected-route";
import { AppShell } from "@/components/layout/app-shell";
import { EventTimeline } from "@/components/vehicles/event-timeline";
import { PublicReportActions } from "@/components/vehicles/public-report-actions";
import { RoleBadge } from "@/components/ui/badge";
import { ButtonLink } from "@/components/ui/button";
import { Card, SectionHeader } from "@/components/ui/card";
import { ErrorMessage } from "@/components/ui/error-message";
import { listAttachments } from "@/lib/api/attachments";
import { readableApiError } from "@/lib/api/client";
import { listEvents } from "@/lib/api/events";
import type { EventAttachmentDto, VehicleAccessRole, VehicleDto, VehicleEventDto } from "@/lib/api/types";
import { getVehicle, listVehicleAccess } from "@/lib/api/vehicles";

export default function VehicleDetailPage({ params }: { params: { vehicleId: string } }) {
  return (
    <ProtectedRoute>
      <AppShell>
        <VehicleDetailContent vehicleId={params.vehicleId} />
      </AppShell>
    </ProtectedRoute>
  );
}

function VehicleDetailContent({ vehicleId }: { vehicleId: string }) {
  const { user } = useAuth();
  const [vehicle, setVehicle] = useState<VehicleDto | null>(null);
  const [events, setEvents] = useState<VehicleEventDto[]>([]);
  const [attachmentsByEvent, setAttachmentsByEvent] = useState<Record<string, EventAttachmentDto[]>>({});
  const [role, setRole] = useState<VehicleAccessRole | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    async function load() {
      try {
        const [vehicleResponse, eventResponse] = await Promise.all([
          getVehicle(vehicleId),
          listEvents(vehicleId)
        ]);
        setVehicle(vehicleResponse);
        setEvents(eventResponse);
        const attachmentEntries = await Promise.all(
          eventResponse.map(async (event) => [event.id, await listAttachments(vehicleId, event.id)] as const)
        );
        setAttachmentsByEvent(Object.fromEntries(attachmentEntries));

        try {
          const access = await listVehicleAccess(vehicleId);
          setRole(access.find((item) => item.userId === user?.id)?.role ?? null);
        } catch {
          setRole(null);
        }
      } catch (requestError) {
        setError(readableApiError(requestError));
      } finally {
        setLoading(false);
      }
    }

    void load();
  }, [user?.id, vehicleId]);

  const title = vehicle ? [vehicle.make, vehicle.model].filter(Boolean).join(" ") || "Автомобиль" : "Автомобиль";

  return (
    <div>
      <Link href="/vehicles" className="mb-6 inline-flex items-center gap-2 text-sm text-slate-400 hover:text-white">
        <ArrowLeft className="h-4 w-4" />
        К автомобилям
      </Link>
      <ErrorMessage message={error} />
      {loading ? (
        <Card className="text-slate-400">Загружаем историю…</Card>
      ) : vehicle ? (
        <div className="grid gap-6 xl:grid-cols-[minmax(0,1fr)_380px]">
          <div>
            <SectionHeader
              title={vehicle.year ? `${vehicle.year} ${title}` : title}
              description={`VIN ${vehicle.vin}`}
              action={<ButtonLink href={`/vehicles/${vehicleId}/events/new`}><Plus className="h-4 w-4" />Добавить событие</ButtonLink>}
            />
            <Card className="mb-6">
              <div className="grid gap-4 md:grid-cols-4">
                <Spec label="Двигатель" value={vehicle.engine} />
                <Spec label="КПП" value={vehicle.transmission} />
                <Spec label="Комплектация" value={vehicle.trim} />
                <Spec label="Рынок" value={vehicle.market} />
              </div>
              {role ? <div className="mt-4"><RoleBadge role={role} /></div> : null}
            </Card>
            <EventTimeline vehicleId={vehicleId} events={events} attachmentsByEvent={attachmentsByEvent} />
          </div>
          <div className="space-y-6">
            <PublicReportActions vehicleId={vehicleId} />
            <Card>
              <h3 className="text-lg font-bold text-white">Hash-chain</h3>
              <p className="mt-2 text-sm leading-6 text-slate-400">
                Каждое событие хранит ссылку на предыдущий hash. Технические поля доступны в карточках timeline.
              </p>
            </Card>
          </div>
        </div>
      ) : null}
    </div>
  );
}

function Spec({ label, value }: { label: string; value?: string | number | null }) {
  return (
    <div>
      <div className="text-xs uppercase tracking-wide text-slate-500">{label}</div>
      <div className="mt-1 font-semibold text-slate-100">{value || "—"}</div>
    </div>
  );
}
