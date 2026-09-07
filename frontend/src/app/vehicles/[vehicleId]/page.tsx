"use client";

import { ArrowLeft, Plus, ShieldCheck } from "lucide-react";
import Link from "next/link";
import { useEffect, useState } from "react";
import { ProtectedRoute } from "@/components/auth/protected-route";
import { AppShell } from "@/components/layout/app-shell";
import { TrustScoreCard } from "@/components/trust/trust-score-card";
import { PaginationControls } from "@/components/ui/pagination-controls";
import { AccessManagementPanel } from "@/components/vehicles/access-management-panel";
import { EventTimeline } from "@/components/vehicles/event-timeline";
import { PublicReportActions } from "@/components/vehicles/public-report-actions";
import { ReminderPanel } from "@/components/vehicles/reminder-panel";
import { RoleBadge } from "@/components/ui/badge";
import { ButtonLink } from "@/components/ui/button";
import { Card, SectionHeader } from "@/components/ui/card";
import { ErrorMessage } from "@/components/ui/error-message";
import { readableApiError } from "@/lib/api/client";
import { listEvents } from "@/lib/api/events";
import type { TrustScoreResponse, VehicleDto, VehicleEventDto } from "@/lib/api/types";
import { getVehicle, getVehicleTrustScore } from "@/lib/api/vehicles";
import { useLanguage } from "@/lib/i18n";
import { canEditVehicle, canManageVehicleAccess } from "@/lib/permissions";

export default function VehicleDetailPage({
  params,
  searchParams
}: {
  params: { vehicleId: string };
  searchParams?: { eventCreated?: string };
}) {
  return (
    <ProtectedRoute>
      <AppShell>
        <VehicleDetailContent vehicleId={params.vehicleId} eventCreated={searchParams?.eventCreated === "1"} />
      </AppShell>
    </ProtectedRoute>
  );
}

function VehicleDetailContent({ vehicleId, eventCreated }: { vehicleId: string; eventCreated: boolean }) {
  const { language, t } = useLanguage();
  const [vehicle, setVehicle] = useState<VehicleDto | null>(null);
  const [events, setEvents] = useState<VehicleEventDto[]>([]);
  const [trustScore, setTrustScore] = useState<TrustScoreResponse | null>(null);
  const [trustScoreError, setTrustScoreError] = useState<string | null>(null);
  const [trustScoreLoading, setTrustScoreLoading] = useState(true);
  const [eventsPage, setEventsPage] = useState(0);
  const [eventsTotalPages, setEventsTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    async function load() {
      try {
        setTrustScoreLoading(true);
        setTrustScoreError(null);
        const trustScoreRequest = getVehicleTrustScore(vehicleId)
          .then((response) => ({ response }))
          .catch((requestError) => ({ requestError }));
        const [vehicleResponse, eventPageResponse, trustScoreResult] = await Promise.all([
          getVehicle(vehicleId),
          listEvents(vehicleId, eventsPage),
          trustScoreRequest
        ]);
        const sortedEvents = [...eventPageResponse.items]
          .sort((left, right) => left.sequenceNumber - right.sequenceNumber);
        setVehicle(vehicleResponse);
        setEvents(sortedEvents);
        setEventsTotalPages(eventPageResponse.totalPages);
        if ("response" in trustScoreResult) {
          setTrustScore(trustScoreResult.response);
        } else {
          setTrustScore(null);
          setTrustScoreError(readableApiError(trustScoreResult.requestError, language));
        }
        setTrustScoreLoading(false);
      } catch (requestError) {
        setError(readableApiError(requestError, language));
        setTrustScoreLoading(false);
      } finally {
        setLoading(false);
      }
    }

    void load();
  }, [eventsPage, language, vehicleId]);

  const fallbackTitle = language === "ru" ? "Автомобиль" : "Vehicle";
  const title = vehicle ? [vehicle.make, vehicle.model].filter(Boolean).join(" ") || fallbackTitle : fallbackTitle;
  const canEdit = vehicle ? canEditVehicle(vehicle.role) : false;
  const canManageAccess = vehicle ? canManageVehicleAccess(vehicle.role) : false;

  return (
    <div>
      <Link href="/vehicles" className="mb-6 inline-flex items-center gap-2 text-sm text-muted hover:text-ink">
        <ArrowLeft className="h-4 w-4" />
        {t("vehicle.backToVehicles")}
      </Link>
      <ErrorMessage message={error} />
      {eventCreated ? (
        <div className="mb-6 rounded-2xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-700">
          {t("events.created")}
        </div>
      ) : null}
      {loading ? (
        <Card className="text-muted">{t("common.loading")}</Card>
      ) : vehicle ? (
        <div className="grid gap-6 xl:grid-cols-[minmax(0,1fr)_minmax(0,360px)]">
          <div className="min-w-0">
            <SectionHeader
              title={vehicle.year ? `${vehicle.year} ${title}` : title}
              description={`VIN ${vehicle.vin}`}
              action={canEdit ? (
                <ButtonLink href={`/vehicles/${vehicleId}/events/new`}>
                  <Plus className="h-4 w-4" />{t("events.add")}
                </ButtonLink>
              ) : undefined}
            />
            <Card className="mb-6">
              <div className="grid grid-cols-2 gap-5 sm:grid-cols-3 2xl:grid-cols-5">
                <Spec label={t("label.generation")} value={vehicle.generation} />
                <Spec label={t("label.engine")} value={vehicle.engine} />
                <Spec label={t("label.transmission")} value={vehicle.transmission} />
                <Spec label={t("label.trim")} value={vehicle.trim} />
                <Spec label={t("label.market")} value={vehicle.market} />
              </div>
              <div className="mt-4"><RoleBadge role={vehicle.role} /></div>
            </Card>
            <div className="mb-6">
              <TrustScoreCard trustScore={trustScore} loading={trustScoreLoading} error={trustScoreError} />
            </div>
            <EventTimeline
              vehicleId={vehicleId}
              events={events}
              canEdit={canEdit}
            />
            <PaginationControls
              page={eventsPage}
              totalPages={eventsTotalPages}
              onPageChange={setEventsPage}
            />
          </div>
          <div className="min-w-0 space-y-6">
            <ReminderPanel vehicleId={vehicleId} canEdit={canEdit} />
            {canEdit ? <PublicReportActions vehicleId={vehicleId} /> : null}
            {canManageAccess ? <AccessManagementPanel vehicleId={vehicleId} /> : null}
            <Card className="bg-brand-soft/60">
              <ShieldCheck className="mb-3 h-6 w-6 text-ink" />
              <h3 className="text-lg font-bold text-ink">{language === "ru" ? "История под защитой" : "A protected history"}</h3>
              <p className="mt-2 text-sm leading-6 text-muted">
                {language === "ru"
                  ? "Записи связаны между собой: проверка помогает заметить изменения в сохранённой истории. Чеки и документы дополняют каждое событие."
                  : "Records are linked together, making changes to the saved history detectable. Receipts and documents add context to each event."}
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
    <div className="min-w-0">
      <div className="text-xs uppercase tracking-wide text-muted">{label}</div>
      <div className="mt-1 break-words font-semibold text-ink">{value || "—"}</div>
    </div>
  );
}
