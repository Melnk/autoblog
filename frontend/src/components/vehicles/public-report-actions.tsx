"use client";

import { Copy, ExternalLink, FileText, RefreshCw, Unlink } from "lucide-react";
import { useState } from "react";
import { Button, ButtonLink } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { ErrorMessage } from "@/components/ui/error-message";
import { createPublicReport, disablePublicReport, rotatePublicReport } from "@/lib/api/vehicles";
import type { PublicReportMetadataDto } from "@/lib/api/types";
import { readableApiError } from "@/lib/api/client";
import { useLanguage } from "@/lib/i18n";

export function PublicReportActions({ vehicleId }: { vehicleId: string }) {
  const [report, setReport] = useState<PublicReportMetadataDto | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [copied, setCopied] = useState(false);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState<string | null>(null);
  const { language, t } = useLanguage();
  const frontendUrl = report ? `/reports/${report.publicToken}` : null;
  const absoluteFrontendUrl = frontendUrl && typeof window !== "undefined" ? `${window.location.origin}${frontendUrl}` : frontendUrl;

  async function generate() {
    setLoading(true);
    setCopied(false);
    setError(null);
    setMessage(null);
    try {
      setReport(await createPublicReport(vehicleId));
    } catch (requestError) {
      setError(readableApiError(requestError, language));
    } finally {
      setLoading(false);
    }
  }

  async function disable() {
    await runMutation(async () => {
      await disablePublicReport(vehicleId);
      setReport(null);
      setMessage(t("publicReport.disabled"));
    });
  }

  async function rotate() {
    await runMutation(async () => {
      setReport(await rotatePublicReport(vehicleId));
      setCopied(false);
      setMessage(t("publicReport.rotated"));
    });
  }

  async function runMutation(action: () => Promise<void>) {
    setLoading(true);
    setError(null);
    setMessage(null);
    try {
      await action();
    } catch (requestError) {
      setError(readableApiError(requestError, language));
    } finally {
      setLoading(false);
    }
  }

  async function copy() {
    if (absoluteFrontendUrl) {
      await navigator.clipboard.writeText(absoluteFrontendUrl);
      setCopied(true);
    }
  }

  return (
    <Card>
      <div className="flex items-start gap-3">
        <div className="flex h-11 w-11 shrink-0 items-center justify-center rounded-2xl bg-brand-soft text-ink">
          <FileText className="h-5 w-5" />
        </div>
        <div className="min-w-0 flex-1">
          <h3 className="text-lg font-bold text-ink">{t("publicReport.title")}</h3>
          <p className="mt-1 text-sm text-muted">{t("publicReport.description")}</p>
          <ErrorMessage message={error} />
          {message ? <p className="mt-3 text-sm text-emerald-700">{message}</p> : null}
          {report ? (
            <div className="mt-4 space-y-3">
              <div className="rounded-2xl border border-line bg-canvas p-3 text-sm text-ink">
                <div className="mb-2 text-xs font-medium text-muted">{language === "ru" ? "Ссылка для покупателя" : "Link for a buyer"}</div>
                <div className="break-all">{absoluteFrontendUrl}</div>
              </div>
              <div className="flex flex-wrap gap-2">
                <ButtonLink href={frontendUrl ?? "#"} target="_blank" variant="secondary">
                  <ExternalLink className="h-4 w-4" />
                  {t("common.open")}
                </ButtonLink>
                <Button type="button" variant="secondary" onClick={() => void copy()}>
                  <Copy className="h-4 w-4" />
                  {copied ? t("common.copied") : t("common.copy")}
                </Button>
                <Button type="button" variant="secondary" disabled={loading} onClick={() => void rotate()}>
                  <RefreshCw className="h-4 w-4" />
                  {t("publicReport.rotate")}
                </Button>
                <Button type="button" variant="ghost" disabled={loading} onClick={() => void disable()}>
                  <Unlink className="h-4 w-4" />
                  {t("publicReport.disable")}
                </Button>
              </div>
            </div>
          ) : (
            <Button type="button" className="mt-4" onClick={() => void generate()} disabled={loading}>
              {loading ? t("publicReport.creating") : t("publicReport.create")}
            </Button>
          )}
        </div>
      </div>
    </Card>
  );
}
