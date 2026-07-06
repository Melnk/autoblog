"use client";

import { Copy, ExternalLink, FileText } from "lucide-react";
import { useState } from "react";
import { Button, ButtonLink } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { ErrorMessage } from "@/components/ui/error-message";
import { createPublicReport } from "@/lib/api/vehicles";
import type { PublicReportMetadataDto } from "@/lib/api/types";
import { API_BASE_URL, readableApiError } from "@/lib/api/client";

export function PublicReportActions({ vehicleId }: { vehicleId: string }) {
  const [report, setReport] = useState<PublicReportMetadataDto | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [copied, setCopied] = useState(false);
  const [loading, setLoading] = useState(false);
  const frontendUrl = report ? `/reports/${report.publicToken}` : null;
  const absoluteFrontendUrl = frontendUrl && typeof window !== "undefined" ? `${window.location.origin}${frontendUrl}` : frontendUrl;

  async function generate() {
    setLoading(true);
    setCopied(false);
    setError(null);
    try {
      setReport(await createPublicReport(vehicleId));
    } catch (requestError) {
      setError(readableApiError(requestError));
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
        <div className="flex h-11 w-11 shrink-0 items-center justify-center rounded-xl border border-emerald-400/30 bg-emerald-400/10 text-emerald-200">
          <FileText className="h-5 w-5" />
        </div>
        <div className="min-w-0 flex-1">
          <h3 className="text-lg font-bold text-white">Публичный отчет</h3>
          <p className="mt-1 text-sm text-slate-400">Ссылка для покупателя. Открывается без авторизации.</p>
          <ErrorMessage message={error} />
          {report ? (
            <div className="mt-4 space-y-3">
              <div className="rounded-lg border border-slate-800 bg-black/20 p-3 text-sm text-slate-300">
                <div className="mb-2 text-xs uppercase tracking-wide text-slate-500">Token {shortToken(report.publicToken)}</div>
                <div className="truncate">Frontend: {absoluteFrontendUrl}</div>
                <div className="mt-1 truncate text-slate-500">Backend: {API_BASE_URL}{report.publicUrl}</div>
              </div>
              <div className="flex flex-wrap gap-2">
                <ButtonLink href={frontendUrl ?? "#"} target="_blank" variant="secondary">
                  <ExternalLink className="h-4 w-4" />
                  Открыть
                </ButtonLink>
                <Button type="button" variant="secondary" onClick={() => void copy()}>
                  <Copy className="h-4 w-4" />
                  {copied ? "Скопировано" : "Скопировать"}
                </Button>
              </div>
            </div>
          ) : (
            <Button type="button" className="mt-4" onClick={() => void generate()} disabled={loading}>
              {loading ? "Генерируем…" : "Создать публичный отчет"}
            </Button>
          )}
        </div>
      </div>
    </Card>
  );
}

function shortToken(token: string) {
  return token.length > 16 ? `${token.slice(0, 8)}…${token.slice(-6)}` : token;
}
