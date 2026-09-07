"use client";

import { CalendarDays, CarFront, CheckCircle2, Coins, Download, Gauge, MapPin, XCircle } from "lucide-react";
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
    return <PublicShell><Card className="text-muted">{t("common.loading")}</Card></PublicShell>;
  }

  if (error || !report) {
    return (
      <PublicShell>
        <ErrorMessage message={error || (language === "ru" ? "Отчёт не найден" : "Report not found")} />
      </PublicShell>
    );
  }

  const title = [report.vehicle.make, report.vehicle.model].filter(Boolean).join(" ") || (language === "ru" ? "Автомобиль" : "Vehicle");

  return (
    <PublicShell>
      <div className="mb-8 overflow-hidden rounded-[28px] bg-brand-yellow p-6 sm:p-8">
        <div className="flex flex-col gap-5 lg:flex-row lg:items-end lg:justify-between">
          <div className="min-w-0">
            <Badge className="border-transparent bg-white/60 text-ink">{t("publicReport.title")}</Badge>
            <h1 className="mt-4 break-words text-3xl font-bold tracking-tight text-ink md:text-4xl">{report.vehicle.year ? `${report.vehicle.year} ` : ""}{title}</h1>
            <p className="mt-3 break-all text-sm font-medium tracking-wide text-ink/75">VIN {report.vehicle.vin}</p>
            <p className="mt-3 max-w-xl text-sm leading-6 text-ink/80">{language === "ru" ? "Познакомьтесь с историей автомобиля: обслуживание, пробег и документы — всё в одном месте." : "Get to know this car: servicing, mileage and documents, all in one place."}</p>
          </div>
          <div className="flex items-center gap-2 self-start rounded-2xl bg-white px-4 py-3 lg:self-end">
            {report.summary.hashChainValid ? (
              <CheckCircle2 className="h-5 w-5 shrink-0 text-emerald-700" />
            ) : (
              <XCircle className="h-5 w-5 shrink-0 text-red-700" />
            )}
            <span className="text-sm font-semibold text-ink">
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

      <div className="grid items-start gap-6 xl:grid-cols-[minmax(0,300px)_minmax(0,1fr)]">
        <Card className="min-w-0">
          <h2 className="text-lg font-bold text-ink">{t("publicReport.vehicleData")}</h2>
          <div className="mt-4 space-y-3 text-sm">
            <Spec label={t("label.generation")} value={report.vehicle.generation} />
            <Spec label={t("label.engine")} value={report.vehicle.engine} />
            <Spec label={t("label.transmission")} value={report.vehicle.transmission} />
            <Spec label={t("label.trim")} value={report.vehicle.trim} />
            <Spec label={t("label.market")} value={report.vehicle.market} />
          </div>
          <p className="mt-5 text-xs leading-5 text-muted">
            {t("publicReport.noOwnerData")} {t("attachments.publicOnly")}
          </p>
        </Card>

        <div className="min-w-0 space-y-5">
          {report.events.length === 0 ? (
            <Card className="text-muted">{t("publicReport.empty")}</Card>
          ) : report.events.map((event) => (
            <Card key={event.sequenceNumber}>
              <div className="space-y-4">
                <div className="min-w-0">
                  <div className="flex flex-wrap gap-2">
                    <Badge>#{event.sequenceNumber}</Badge>
                    <Badge>{getEnumLabel(language, "vehicleEventType", event.type)}</Badge>
                  </div>
                  <h3 className="mt-3 break-words text-xl font-bold text-ink">{event.title}</h3>
                  {event.description ? <p className="mt-2 break-words text-sm leading-6 text-muted">{event.description}</p> : null}
                </div>
                <div className="grid min-w-0 gap-2 text-sm sm:grid-cols-2">
                  <PublicInfo icon={<CalendarDays className="h-4 w-4" />} label={t("label.date")} value={formatDate(event.eventDate, language)} />
                  <PublicInfo icon={<Gauge className="h-4 w-4" />} label={t("label.odometer")} value={formatKm(event.odometerKm, language)} />
                  <PublicInfo icon={<Coins className="h-4 w-4" />} label={t("label.cost")} value={formatMoney(event.costAmount, event.costCurrency, language)} />
                  <PublicInfo icon={<MapPin className="h-4 w-4" />} label={t("label.service")} value={event.serviceName || "—"} />
                </div>
              </div>

              <details className="mt-5 rounded-2xl border border-line p-3 text-xs text-muted">
                <summary className="cursor-pointer font-medium focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-4 focus-visible:outline-ink">{language === "ru" ? "Технические сведения" : "Technical details"}</summary>
                <div className="mt-3 grid gap-2 break-all md:grid-cols-2">
                  <div>{language === "ru" ? "Предыдущая запись" : "Previous record"}: <span className="font-mono text-ink">{shortHash(event.previousEventHash)}</span></div>
                  <div>{language === "ru" ? "Эта запись" : "This record"}: <span className="font-mono text-ink">{shortHash(event.eventHash)}</span></div>
                </div>
              </details>

              {event.attachments.length > 0 ? (
                <div className="mt-5 rounded-2xl border border-line bg-canvas p-4">
                  <h4 className="text-sm font-semibold text-ink">{t("publicReport.publicAttachments")}</h4>
                  <div className="mt-3 space-y-2">
                    {event.attachments.map((attachment) => (
                      <a
                        key={attachment.id}
                        href={`${API_BASE_URL}${attachment.downloadUrl}`}
                        className="flex flex-col gap-2 rounded-2xl border border-line bg-white p-3 text-sm transition hover:border-brand-yellow sm:flex-row sm:items-center sm:justify-between"
                      >
                        <span className="min-w-0">
                          <span className="block break-all font-semibold text-ink">{attachment.originalFilename}</span>
                          <span className="text-xs text-muted">
                            {getEnumLabel(language, "attachmentType", attachment.type)} · {formatFileSize(attachment.sizeBytes, language)}
                          </span>
                        </span>
                        <span className="inline-flex shrink-0 items-center gap-2 font-semibold text-ink">
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
    <main className="min-h-screen bg-canvas px-4 py-6 sm:px-6 sm:py-8 lg:px-10">
      <div className="mx-auto max-w-7xl">
        <header className="mb-8 flex flex-wrap items-center justify-between gap-4">
          <Link href="/" className="inline-flex items-center gap-2.5 text-xl font-bold tracking-tight text-ink">
            <span className="flex h-10 w-10 items-center justify-center rounded-2xl bg-brand-yellow"><CarFront className="h-5 w-5" /></span>
            <span>auto<span className="font-medium">blog</span>.</span>
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
    <Card className="min-w-0">
      <div className="text-xs uppercase tracking-wide text-muted">{label}</div>
      <div className="mt-2 break-words text-xl font-bold leading-7 text-ink">{value}</div>
    </Card>
  );
}

function Spec({ label, value }: { label: string; value?: string | number | null }) {
  return (
    <div className="grid min-w-0 grid-cols-2 gap-3 border-b border-line pb-3">
      <span className="text-muted">{label}</span>
      <span className="break-words text-right font-semibold text-ink">{value || "—"}</span>
    </div>
  );
}

function PublicInfo({ icon, label, value }: { icon: ReactNode; label: string; value: string }) {
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
