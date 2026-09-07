"use client";

import { AlertTriangle, CheckCircle2, Info, ShieldCheck, XCircle } from "lucide-react";
import type { ReactNode } from "react";
import { Badge } from "@/components/ui/badge";
import { Card } from "@/components/ui/card";
import { ErrorMessage } from "@/components/ui/error-message";
import type { TrustScoreLevel, TrustScoreResponse, TrustSignalImpact } from "@/lib/api/types";
import { formatKm, formatMoney } from "@/lib/format";
import {
  getEnumLabel,
  getTrustScoreSummary,
  getTrustSignalLabel,
  getTrustSignalMessage,
  useLanguage
} from "@/lib/i18n";
import { cn } from "@/lib/utils";

const levelStyles: Record<TrustScoreLevel, string> = {
  HIGH: "border-emerald-200 bg-emerald-50 text-emerald-700",
  MEDIUM: "border-amber-200 bg-amber-50 text-amber-800",
  LOW: "border-red-200 bg-red-50 text-red-700",
  UNKNOWN: "border-line bg-canvas text-ink"
};

const impactStyles: Record<TrustSignalImpact, string> = {
  POSITIVE: "border-emerald-200 bg-emerald-50 text-emerald-700",
  NEGATIVE: "border-red-200 bg-red-50 text-red-700",
  NEUTRAL: "border-line bg-canvas text-ink"
};

export function TrustScoreCard({
  trustScore,
  loading = false,
  error,
  publicMode = false
}: {
  trustScore?: TrustScoreResponse | null;
  loading?: boolean;
  error?: string | null;
  publicMode?: boolean;
}) {
  const { language, t } = useLanguage();

  if (loading) {
    return <Card className="text-muted">{t("common.loading")}</Card>;
  }

  if (error) {
    return (
      <Card>
        <h3 className="text-lg font-bold text-ink">{t("trust.title")}</h3>
        <ErrorMessage message={error} />
      </Card>
    );
  }

  if (!trustScore) {
    return null;
  }

  const metrics = trustScore.metrics;
  const buyerSignals = publicMode
    ? trustScore.signals.filter((signal) => signal.impact !== "NEUTRAL").slice(0, 6)
    : trustScore.signals;

  return (
    <Card className={cn("min-w-0", publicMode ? "border-brand-yellow" : undefined)}>
      <div className="flex flex-col gap-5 sm:flex-row sm:items-start sm:justify-between">
        <div className="min-w-0">
          <div className="flex items-center gap-2">
            <ShieldCheck className="h-5 w-5 shrink-0 text-ink" />
            <h3 className="text-lg font-bold text-ink">{t("trust.title")}</h3>
          </div>
          <p className="mt-1 text-sm text-muted">
            {publicMode ? t("trust.publicHint") : t("trust.description")}
          </p>
        </div>
        <Badge className={cn("w-fit", levelStyles[trustScore.level])}>
          {getEnumLabel(language, "trustScoreLevel", trustScore.level)}
        </Badge>
      </div>

      <div className="mt-5 grid gap-4 lg:grid-cols-[180px_minmax(0,1fr)]">
        <div className="rounded-2xl bg-brand-soft p-5 text-center">
          <div className="text-5xl font-black tracking-normal text-ink">{trustScore.score}</div>
          <div className="mt-1 text-xs uppercase tracking-wide text-muted">{t("trust.scoreOutOf")}</div>
          <div className="mt-4 h-2 overflow-hidden rounded-full bg-white">
            <div
              className={cn(
                "h-full rounded-full",
                trustScore.level === "HIGH" ? "bg-emerald-400" : null,
                trustScore.level === "MEDIUM" ? "bg-amber-300" : null,
                trustScore.level === "LOW" ? "bg-red-400" : null,
                trustScore.level === "UNKNOWN" ? "bg-slate-500" : null
              )}
              style={{ width: `${Math.max(0, Math.min(100, trustScore.score))}%` }}
            />
          </div>
        </div>

        <div className="min-w-0">
          <p className="text-sm leading-6 text-ink">
            {getTrustScoreSummary(language, trustScore.level, trustScore.summary)}
          </p>
          <div className="mt-4 grid gap-2 sm:grid-cols-2">
            <Metric label={t("label.eventsCount")} value={String(metrics.eventsCount)} />
            <Metric label={t("label.latestOdometer")} value={formatKm(metrics.latestOdometerKm, language)} />
            <Metric label={t("trust.publicEvidence")} value={String(metrics.publicAttachmentsCount)} />
            <Metric label={t("trust.overdueReminders")} value={String(metrics.overdueRemindersCount)} />
          </div>
        </div>
      </div>

      <details className="mt-5 border-t border-line pt-4">
        <summary className="cursor-pointer text-sm font-semibold text-ink focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-4 focus-visible:outline-ink">{language === "ru" ? "Из чего складывается оценка" : "How this score is calculated"}</summary>
        <div className="mt-4 grid gap-4 lg:grid-cols-2">
          <div className="min-w-0">
            <h4 className="text-sm font-semibold text-ink">{t("trust.affects")}</h4>
            <div className="mt-3 space-y-2">
              {buyerSignals.map((signal) => (
                <div key={`${signal.code}:${signal.points}`} className="rounded-2xl border border-line bg-white p-3">
                  <div className="flex flex-wrap items-center justify-between gap-2">
                    <div className="flex min-w-0 items-center gap-2">
                      <ImpactIcon impact={signal.impact} />
                      <span className="break-words text-sm font-semibold text-ink">
                        {getTrustSignalLabel(language, signal.code)}
                      </span>
                    </div>
                    <Badge className={impactStyles[signal.impact]}>
                      {signal.points > 0 ? `+${signal.points}` : signal.points}
                    </Badge>
                  </div>
                  <p className="mt-2 text-xs leading-5 text-muted">
                    {getTrustSignalMessage(language, signal.code, signal.message)}
                  </p>
                </div>
              ))}
            </div>
          </div>

          <div className="min-w-0">
            <h4 className="text-sm font-semibold text-ink">{t("trust.metrics")}</h4>
            <div className="mt-3 grid gap-2">
              <Metric label={t("trust.eventsWithEvidence")} value={String(metrics.eventsWithAttachmentsCount)} />
              {!publicMode ? <Metric label={t("trust.privateEvidence")} value={String(metrics.privateAttachmentsCount)} /> : null}
              <Metric label={t("trust.odometerReadings")} value={String(metrics.odometerEventsCount)} />
              <Metric label={t("label.knownCosts")} value={formatMoney(metrics.totalKnownCostAmount, "RUB", language)} />
              <Metric
                label={t("publicReport.hashValid")}
                value={metrics.hashChainValid ? t("publicReport.hashValid") : t("publicReport.hashInvalid")}
                icon={metrics.hashChainValid ? <CheckCircle2 className="h-4 w-4 text-emerald-700" /> : <XCircle className="h-4 w-4 text-red-700" />}
              />
              <Metric
                label={t("label.odometer")}
                value={metrics.odometerConsistent ? t("trust.odometerConsistent") : t("trust.odometerIssue")}
                icon={metrics.odometerConsistent ? <CheckCircle2 className="h-4 w-4 text-emerald-700" /> : <AlertTriangle className="h-4 w-4 text-red-700" />}
              />
              {!publicMode ? <Metric label={t("trust.activeReminders")} value={String(metrics.activeRemindersCount)} /> : null}
            </div>
          </div>
        </div>
      </details>
    </Card>
  );
}

function ImpactIcon({ impact }: { impact: TrustSignalImpact }) {
  if (impact === "POSITIVE") {
    return <CheckCircle2 className="h-4 w-4 text-emerald-700" />;
  }
  if (impact === "NEGATIVE") {
    return <AlertTriangle className="h-4 w-4 text-red-700" />;
  }
  return <Info className="h-4 w-4 text-muted" />;
}

function Metric({ label, value, icon }: { label: string; value: string; icon?: ReactNode }) {
  return (
    <div className="min-w-0 rounded-2xl bg-canvas p-3">
      <div className="flex items-center gap-2 text-xs uppercase tracking-wide text-muted">
        {icon}
        {label}
      </div>
      <div className="mt-1 break-words font-semibold text-ink">{value || "—"}</div>
    </div>
  );
}
