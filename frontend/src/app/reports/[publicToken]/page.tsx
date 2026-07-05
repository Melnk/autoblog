"use client";

import { CalendarDays, CheckCircle2, Download, Gauge, ShieldCheck, XCircle } from "lucide-react";
import Link from "next/link";
import type { ReactNode } from "react";
import { useEffect, useState } from "react";
import { Badge } from "@/components/ui/badge";
import { Card } from "@/components/ui/card";
import { ErrorMessage } from "@/components/ui/error-message";
import { API_BASE_URL, readableApiError } from "@/lib/api/client";
import { getPublicReport } from "@/lib/api/publicReports";
import type { PublicReportDto } from "@/lib/api/types";
import { formatBytes, formatDate, formatMoney, shortHash } from "@/lib/utils";

export default function PublicReportPage({ params }: { params: { publicToken: string } }) {
  const [report, setReport] = useState<PublicReportDto | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function load() {
      try {
        setReport(await getPublicReport(params.publicToken));
      } catch (requestError) {
        setError(readableApiError(requestError));
      } finally {
        setLoading(false);
      }
    }

    void load();
  }, [params.publicToken]);

  if (loading) {
    return <PublicShell><Card className="text-slate-400">Загружаем публичный отчет…</Card></PublicShell>;
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
            <Badge className="border-emerald-400/30 bg-emerald-400/10 text-emerald-200">Публичный отчет</Badge>
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
              Hash-chain {report.summary.hashChainValid ? "валиден" : "требует проверки"}
            </span>
          </div>
        </div>
      </div>

      <div className="mb-8 grid gap-4 md:grid-cols-2 xl:grid-cols-4">
        <SummaryCard label="Событий" value={String(report.summary.eventsCount)} />
        <SummaryCard label="Период" value={`${formatDate(report.summary.firstEventDate)} — ${formatDate(report.summary.lastEventDate)}`} />
        <SummaryCard label="Последний пробег" value={report.summary.latestOdometerKm ? `${report.summary.latestOdometerKm.toLocaleString("ru-RU")} км` : "—"} />
        <SummaryCard label="Известные расходы" value={formatMoney(report.summary.totalKnownCostAmount, report.summary.costCurrency)} />
      </div>

      <div className="grid gap-6 xl:grid-cols-[320px_minmax(0,1fr)]">
        <Card>
          <h2 className="text-lg font-bold text-white">Данные автомобиля</h2>
          <div className="mt-4 space-y-3 text-sm">
            <Spec label="Поколение" value={report.vehicle.generation} />
            <Spec label="Двигатель" value={report.vehicle.engine} />
            <Spec label="КПП" value={report.vehicle.transmission} />
            <Spec label="Комплектация" value={report.vehicle.trim} />
            <Spec label="Рынок" value={report.vehicle.market} />
          </div>
          <p className="mt-5 text-xs leading-5 text-slate-500">
            Отчет не содержит данных владельца, внутренних UUID автомобиля или внутренних UUID событий.
          </p>
        </Card>

        <div className="space-y-5">
          {report.events.map((event) => (
            <Card key={event.sequenceNumber}>
              <div className="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
                <div>
                  <div className="flex flex-wrap gap-2">
                    <Badge>#{event.sequenceNumber}</Badge>
                    <Badge>{event.type}</Badge>
                  </div>
                  <h3 className="mt-3 text-xl font-bold text-white">{event.title}</h3>
                  {event.description ? <p className="mt-2 text-sm leading-6 text-slate-300">{event.description}</p> : null}
                </div>
                <div className="grid gap-2 text-sm sm:grid-cols-3 lg:min-w-[420px]">
                  <PublicInfo icon={<CalendarDays className="h-4 w-4" />} label="Дата" value={formatDate(event.eventDate)} />
                  <PublicInfo icon={<Gauge className="h-4 w-4" />} label="Пробег" value={event.odometerKm ? `${event.odometerKm.toLocaleString("ru-RU")} км` : "—"} />
                  <PublicInfo icon={<ShieldCheck className="h-4 w-4" />} label="Hash" value={shortHash(event.eventHash)} />
                </div>
              </div>

              {event.attachments.length > 0 ? (
                <div className="mt-5 rounded-xl border border-slate-800 bg-black/20 p-4">
                  <h4 className="text-sm font-semibold text-white">Публичные вложения</h4>
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
                            {attachment.type} · {formatBytes(attachment.sizeBytes)} · {shortHash(attachment.checksumSha256)}
                          </span>
                        </span>
                        <span className="inline-flex items-center gap-2 text-neon-cyan">
                          <Download className="h-4 w-4" />
                          Скачать
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
  return (
    <main className="min-h-screen px-4 py-8 sm:px-6 lg:px-10">
      <div className="mx-auto max-w-7xl">
        <header className="mb-8 flex items-center justify-between">
          <Link href="/" className="text-2xl font-bold text-white">
            Auto<span className="text-neon-cyan">Blog</span>
          </Link>
          <Badge>Без авторизации</Badge>
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
