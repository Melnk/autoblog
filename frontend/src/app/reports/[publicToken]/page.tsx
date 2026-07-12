"use client";

import { CalendarDays, CheckCircle2, Coins, Download, Gauge, MapPin, XCircle } from "lucide-react";
import Link from "next/link";
import type { ReactNode } from "react";
import { useEffect, useState } from "react";
import { TrustScoreCard } from "@/components/trust/trust-score-card";
import { Badge } from "@/components/ui/badge";
import { Card } from "@/components/ui/card";
import { ErrorMessage } from "@/components/ui/error-message";
import { API_BASE_URL, readableApiError } from "@/lib/api/client";
import { getPublicReport } from "@/lib/api/publicReports";
import type { PublicReportDto } from "@/lib/api/types";
import { formatDate, formatFileSize, formatKm, formatMoney, shortHash } from "@/lib/format";
import { getEnumLabel, useLanguage } from "@/lib/i18n";

export default function PublicReportPage({ params }: { params: { publicToken: string } }) {
  const [report, setReport] = useState<PublicReportDto | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const { language, t } = useLanguage();

  useEffect(() => {
    async function load() {
      try {
        setReport(await getPublicReport(params.publicToken));
      } catch (requestError) {
        setError(readableApiError(requestError, language));
      } finally {
        setLoading(false);
      }
    }

    void load();
  }, [language, params.publicToken]);

  if (loading) {
    return <PublicShell><Card className="text-slate-400">{t("common.loading")}</Card></PublicShell>;
  }

  if (error || !report) {
    return (
      <PublicShell>
        <ErrorMessage message={error || "Отчет не найден"} />
      </PublicShell>
    );
  }

  const title = [report.vehicle.make, report.vehicle.model].filter(Boolean).join(" ") || "Автомобиль";

  return (
    <PublicShell>
      <div className="mb-8 overflow-hidden rounded-2xl border border-blue-400/30 bg-gradient-to-br from-blue-500/18 via-surface-900 to-emerald-500/10 p-6 shadow-glow">
        <div className="flex flex-col gap-5 lg:flex-row lg:items-end lg:justify-between">
          <div>
            <Badge className="border-emerald-400/30 bg-emerald-400/10 text-emerald-200">{t("publicReport.title")}</Badge>
            <h1 className="mt-4 text-3xl font-bold text-white md:text-4xl">{report.vehicle.year ? `${report.vehicle.year} ` : ""}{title}</h1>
            <p className="mt-2 text-sm uppercase tracking-wide text-slate-400">VIN {report.vehicle.vin}</p>
          </div>
          <div className="flex items-center gap-2 rounded-xl border border-slate-700 bg-black/20 px-4 py-3">
            {report.summary.hashChainValid ? (
              <CheckCircle2 className="h-5 w-5 text-emerald-300" />
            ) : (
              <XCircle className="h-5 w-5 text-red-300" />
            )}
            <span className="text-sm font-semibold text-slate-100">
              {report.summary.hashChainValid ? t("publicReport.hashValid") : t("publicReport.hashInvalid")}
            </span>
          </div>
        </div>
      </div>

      <div className="mb-8 grid gap-4 md:grid-cols-2 xl:grid-cols-4">
        <SummaryCard label={t("label.eventsCount")} value={String(report.summary.eventsCount)} />
        <SummaryCard label={t("label.period")} value={`${formatDate(report.summary.firstEventDate, language)} — ${formatDate(report.summary.lastEventDate, language)}`} />
        <SummaryCard label={t("label.latestOdometer")} value={formatKm(report.summary.latestOdometerKm, language)} />
        <SummaryCard label={t("label.knownCosts")} value={formatMoney(report.summary.totalKnownCostAmount, report.summary.costCurrency, language)} />
      </div>

      <div className="mb-8">
        <TrustScoreCard trustScore={report.trustScore} publicMode />
      </div>

      <div className="grid gap-6 xl:grid-cols-[320px_minmax(0,1fr)]">
        <Card>
          <h2 className="text-lg font-bold text-white">{t("publicReport.vehicleData")}</h2>
          <div className="mt-4 space-y-3 text-sm">
            <Spec label={t("label.generation")} value={report.vehicle.generation} />
            <Spec label={t("label.engine")} value={report.vehicle.engine} />
            <Spec label={t("label.transmission")} value={report.vehicle.transmission} />
            <Spec label={t("label.trim")} value={report.vehicle.trim} />
            <Spec label={t("label.market")} value={report.vehicle.market} />
          </div>
          <p className="mt-5 text-xs leading-5 text-slate-500">
            {t("publicReport.noOwnerData")} {t("attachments.publicOnly")}
          </p>
        </Card>

        <div className="space-y-5">
          {report.events.length === 0 ? (
            <Card className="text-slate-400">{t("publicReport.empty")}</Card>
          ) : report.events.map((event) => (
            <Card key={event.sequenceNumber}>
              <div className="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
                <div>
                  <div className="flex flex-wrap gap-2">
                    <Badge>#{event.sequenceNumber}</Badge>
                    <Badge>{getEnumLabel(language, "vehicleEventType", event.type)}</Badge>
                  </div>
                  <h3 className="mt-3 text-xl font-bold text-white">{event.title}</h3>
                  {event.description ? <p className="mt-2 text-sm leading-6 text-slate-300">{event.description}</p> : null}
                </div>
                <div className="grid gap-2 text-sm sm:grid-cols-2 lg:min-w-[420px]">
                  <PublicInfo icon={<CalendarDays className="h-4 w-4" />} label={t("label.date")} value={formatDate(event.eventDate, language)} />
                  <PublicInfo icon={<Gauge className="h-4 w-4" />} label={t("label.odometer")} value={formatKm(event.odometerKm, language)} />
                  <PublicInfo icon={<Coins className="h-4 w-4" />} label={t("label.cost")} value={formatMoney(event.costAmount, event.costCurrency, language)} />
                  <PublicInfo icon={<MapPin className="h-4 w-4" />} label={t("label.service")} value={event.serviceName || "—"} />
                </div>
              </div>

              <div className="mt-5 grid gap-2 rounded-lg border border-slate-800 bg-black/20 p-3 text-xs text-slate-500 md:grid-cols-2">
                <div>prev: <span className="text-slate-300">{shortHash(event.previousEventHash)}</span></div>
                <div>hash: <span className="text-slate-300">{shortHash(event.eventHash)}</span></div>
              </div>

              {event.attachments.length > 0 ? (
                <div className="mt-5 rounded-xl border border-slate-800 bg-black/20 p-4">
                  <h4 className="text-sm font-semibold text-white">{t("publicReport.publicAttachments")}</h4>
                  <div className="mt-3 space-y-2">
                    {event.attachments.map((attachment) => (
                      <a
                        key={attachment.id}
                        href={`${API_BASE_URL}${attachment.downloadUrl}`}
                        className="flex flex-col gap-2 rounded-lg border border-slate-800 bg-surface-900 p-3 text-sm transition hover:border-blue-400/50 sm:flex-row sm:items-center sm:justify-between"
                      >
                        <span>
                          <span className="block font-semibold text-slate-100">{attachment.originalFilename}</span>
                          <span className="text-xs text-slate-500">
                            {getEnumLabel(language, "attachmentType", attachment.type)} · {formatFileSize(attachment.sizeBytes, language)} · {shortHash(attachment.checksumSha256)}
                          </span>
                        </span>
                        <span className="inline-flex items-center gap-2 text-neon-cyan">
                          <Download className="h-4 w-4" />
                          {t("common.download")}
                        </span>
                      </a>
                    ))}
                  </div>
                </div>
              ) : null}
            </Card>
          ))}
        </div>
      </div>
    </PublicShell>
  );
}

function PublicShell({ children }: { children: ReactNode }) {
  const { t } = useLanguage();

  return (
    <main className="min-h-screen px-4 py-8 sm:px-6 lg:px-10">
      <div className="mx-auto max-w-7xl">
        <header className="mb-8 flex items-center justify-between">
          <Link href="/" className="text-2xl font-bold text-white">
            Auto<span className="text-neon-cyan">Blog</span>
          </Link>
          <Badge>{t("publicReport.noAuth")}</Badge>
        </header>
        {children}
      </div>
    </main>
  );
}

function SummaryCard({ label, value }: { label: string; value: string }) {
  return (
    <Card>
      <div className="text-xs uppercase tracking-wide text-slate-500">{label}</div>
      <div className="mt-2 text-2xl font-bold text-white">{value}</div>
    </Card>
  );
}

function Spec({ label, value }: { label: string; value?: string | number | null }) {
  return (
    <div className="flex justify-between gap-3 border-b border-slate-800 pb-3">
      <span className="text-slate-500">{label}</span>
      <span className="text-right font-semibold text-slate-200">{value || "—"}</span>
    </div>
  );
}

function PublicInfo({ icon, label, value }: { icon: ReactNode; label: string; value: string }) {
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
