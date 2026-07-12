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
  HIGH: "border-emerald-400/40 bg-emerald-400/10 text-emerald-100",
  MEDIUM: "border-amber-300/40 bg-amber-400/10 text-amber-100",
  LOW: "border-red-400/40 bg-red-500/10 text-red-100",
  UNKNOWN: "border-slate-600 bg-slate-800 text-slate-200"
};

const impactStyles: Record<TrustSignalImpact, string> = {
  POSITIVE: "border-emerald-400/30 bg-emerald-400/10 text-emerald-100",
  NEGATIVE: "border-red-400/30 bg-red-500/10 text-red-100",
  NEUTRAL: "border-slate-600 bg-slate-800 text-slate-200"
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
    return <Card className="text-slate-400">{t("common.loading")}</Card>;
  }

  if (error) {
    return (
      <Card>
        <h3 className="text-lg font-bold text-white">{t("trust.title")}</h3>
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
    <Card className={publicMode ? "border-blue-400/20" : undefined}>
      <div className="flex flex-col gap-5 sm:flex-row sm:items-start sm:justify-between">
        <div>
          <div className="flex items-center gap-2">
            <ShieldCheck className="h-5 w-5 text-neon-cyan" />
            <h3 className="text-lg font-bold text-white">{t("trust.title")}</h3>
          </div>
          <p className="mt-1 text-sm text-slate-400">
            {publicMode ? t("trust.publicHint") : t("trust.description")}
          </p>
        </div>
        <Badge className={cn("w-fit", levelStyles[trustScore.level])}>
          {getEnumLabel(language, "trustScoreLevel", trustScore.level)}
        </Badge>
      </div>

      <div className="mt-5 grid gap-4 lg:grid-cols-[180px_minmax(0,1fr)]">
        <div className="rounded-xl border border-slate-800 bg-black/20 p-4 text-center">
          <div className="text-5xl font-black tracking-normal text-white">{trustScore.score}</div>
          <div className="mt-1 text-xs uppercase tracking-wide text-slate-500">{t("trust.scoreOutOf")}</div>
          <div className="mt-4 h-2 overflow-hidden rounded-full bg-slate-800">
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

        <div>
          <p className="text-sm leading-6 text-slate-300">
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

      <div className="mt-5 grid gap-4 lg:grid-cols-2">
        <div>
          <h4 className="text-sm font-semibold text-white">{t("trust.affects")}</h4>
          <div className="mt-3 space-y-2">
            {buyerSignals.map((signal) => (
              <div key={`${signal.code}:${signal.points}`} className="rounded-lg border border-slate-800 bg-surface-900 p-3">
                <div className="flex flex-wrap items-center justify-between gap-2">
                  <div className="flex items-center gap-2">
                    <ImpactIcon impact={signal.impact} />
                    <span className="text-sm font-semibold text-slate-100">
                      {getTrustSignalLabel(language, signal.code)}
                    </span>
                  </div>
                  <Badge className={impactStyles[signal.impact]}>
                    {signal.points > 0 ? `+${signal.points}` : signal.points}
                  </Badge>
                </div>
                <p className="mt-2 text-xs leading-5 text-slate-500">
                  {getTrustSignalMessage(language, signal.code, signal.message)}
                </p>
              </div>
            ))}
          </div>
        </div>

        <div>
          <h4 className="text-sm font-semibold text-white">{t("trust.metrics")}</h4>
          <div className="mt-3 grid gap-2">
            <Metric label={t("trust.eventsWithEvidence")} value={String(metrics.eventsWithAttachmentsCount)} />
            {!publicMode ? <Metric label={t("trust.privateEvidence")} value={String(metrics.privateAttachmentsCount)} /> : null}
            <Metric label={t("trust.odometerReadings")} value={String(metrics.odometerEventsCount)} />
            <Metric label={t("label.knownCosts")} value={formatMoney(metrics.totalKnownCostAmount, "RUB", language)} />
            <Metric
              label={t("publicReport.hashValid")}
              value={metrics.hashChainValid ? t("publicReport.hashValid") : t("publicReport.hashInvalid")}
              icon={metrics.hashChainValid ? <CheckCircle2 className="h-4 w-4 text-emerald-300" /> : <XCircle className="h-4 w-4 text-red-300" />}
            />
            <Metric
              label={t("label.odometer")}
              value={metrics.odometerConsistent ? t("trust.odometerConsistent") : t("trust.odometerIssue")}
              icon={metrics.odometerConsistent ? <CheckCircle2 className="h-4 w-4 text-emerald-300" /> : <AlertTriangle className="h-4 w-4 text-red-300" />}
            />
            {!publicMode ? <Metric label={t("trust.activeReminders")} value={String(metrics.activeRemindersCount)} /> : null}
          </div>
        </div>
      </div>
    </Card>
  );
}

function ImpactIcon({ impact }: { impact: TrustSignalImpact }) {
  if (impact === "POSITIVE") {
    return <CheckCircle2 className="h-4 w-4 text-emerald-300" />;
  }
  if (impact === "NEGATIVE") {
    return <AlertTriangle className="h-4 w-4 text-red-300" />;
  }
  return <Info className="h-4 w-4 text-slate-400" />;
}

function Metric({ label, value, icon }: { label: string; value: string; icon?: ReactNode }) {
  return (
    <div className="rounded-lg border border-slate-800 bg-black/20 p-3">
      <div className="flex items-center gap-2 text-xs uppercase tracking-wide text-slate-500">
        {icon}
        {label}
      </div>
      <div className="mt-1 font-semibold text-slate-200">{value || "—"}</div>
    </div>
  );
}
