"use client";

import { Download, Upload } from "lucide-react";
import { useCallback, useEffect, useState } from "react";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { ErrorMessage } from "@/components/ui/error-message";
import { Field, inputClassName } from "@/components/ui/form";
import { PaginationControls } from "@/components/ui/pagination-controls";
import { downloadAttachment, listAttachments, uploadAttachment } from "@/lib/api/attachments";
import { readableApiError } from "@/lib/api/client";
import type { AttachmentType, AttachmentVisibility, EventAttachmentDto } from "@/lib/api/types";
import { formatFileSize } from "@/lib/format";
import {
  getAttachmentTypeOptions,
  getAttachmentVisibilityOptions,
  getEnumLabel,
  useLanguage
} from "@/lib/i18n";

export function AttachmentPanel({
  vehicleId,
  eventId,
  canUpload
}: {
  vehicleId: string;
  eventId: string;
  canUpload: boolean;
}) {
  const [attachments, setAttachments] = useState<EventAttachmentDto[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [file, setFile] = useState<File | null>(null);
  const [type, setType] = useState<AttachmentType>("RECEIPT");
  const [visibility, setVisibility] = useState<AttachmentVisibility>("PRIVATE");
  const [description, setDescription] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [uploading, setUploading] = useState(false);
  const { language, t } = useLanguage();
  const attachmentTypeOptions = getAttachmentTypeOptions(language);
  const visibilityOptions = getAttachmentVisibilityOptions(language);
  const selectedVisibility = visibilityOptions.find((option) => option.value === visibility);

  const refresh = useCallback(async () => {
    try {
      const response = await listAttachments(vehicleId, eventId, page, 20);
      setAttachments(response.items);
      setTotalPages(response.totalPages);
    } catch (requestError) {
      setError(readableApiError(requestError, language));
    }
  }, [eventId, language, page, vehicleId]);

  useEffect(() => {
    void refresh();
  }, [refresh]);

  async function onUpload() {
    if (!file) {
      setError(t("attachments.chooseFile"));
      return;
    }
    setError(null);
    setUploading(true);
    try {
      await uploadAttachment(vehicleId, eventId, { file, type, visibility, description });
      setFile(null);
      setDescription("");
      if (page === 0) {
        await refresh();
      } else {
        setPage(0);
      }
    } catch (requestError) {
      setError(readableApiError(requestError, language));
    } finally {
      setUploading(false);
    }
  }

  async function onDownload(attachment: EventAttachmentDto) {
    try {
      const blob = await downloadAttachment(vehicleId, eventId, attachment.id);
      const url = URL.createObjectURL(blob);
      const link = document.createElement("a");
      link.href = url;
      link.download = attachment.originalFilename;
      link.click();
      URL.revokeObjectURL(url);
    } catch (requestError) {
      setError(readableApiError(requestError, language));
    }
  }

  return (
    <div className="mt-4 min-w-0 rounded-2xl border border-line bg-canvas p-3 sm:p-4">
      <div className="mb-4 flex items-center justify-between">
        <h4 className="text-sm font-semibold text-ink">{t("attachments.title")}</h4>
        <Badge>{attachments.length}</Badge>
      </div>
      <ErrorMessage message={error} />
      {attachments.length > 0 ? (
        <div className="mt-3 space-y-2">
          {attachments.map((attachment) => (
            <div key={attachment.id} className="flex flex-col gap-3 rounded-2xl border border-line bg-white p-3 sm:flex-row sm:items-center sm:justify-between">
              <div className="min-w-0">
                <div className="break-all text-sm font-semibold text-ink">{attachment.originalFilename}</div>
                <div className="mt-1 flex flex-wrap gap-2 text-xs text-muted">
                  <span>{getEnumLabel(language, "attachmentType", attachment.type)}</span>
                  <span>{getEnumLabel(language, "attachmentVisibility", attachment.visibility)}</span>
                  <span>{formatFileSize(attachment.sizeBytes, language)}</span>
                </div>
                {attachment.description ? <p className="mt-1 break-words text-xs text-muted">{attachment.description}</p> : null}
                <details className="mt-2 text-xs text-muted">
                  <summary className="cursor-pointer focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-4 focus-visible:outline-ink">{language === "ru" ? "Технические сведения" : "Technical details"}</summary>
                  <p className="mt-2 break-all font-mono">SHA-256: {attachment.checksumSha256}</p>
                </details>
              </div>
              <Button type="button" variant="secondary" className="h-9 shrink-0" onClick={() => void onDownload(attachment)}>
                <Download className="h-4 w-4" />
                {t("common.download")}
              </Button>
            </div>
          ))}
        </div>
      ) : (
        <p className="mt-3 text-sm text-muted">{t("attachments.empty")}</p>
      )}
      <PaginationControls page={page} totalPages={totalPages} onPageChange={setPage} />

      {canUpload ? (
        <details className="mt-4 border-t border-line pt-4">
          <summary className="cursor-pointer text-sm font-semibold text-ink focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-4 focus-visible:outline-ink">{language === "ru" ? "Добавить чек или документ" : "Add a receipt or document"}</summary>
          <div className="mt-4 grid min-w-0 gap-3 md:grid-cols-2">
            <Field label={t("label.file")}>
              <input className={inputClassName("pt-2")} type="file" onChange={(event) => setFile(event.target.files?.[0] ?? null)} />
            </Field>
            <Field label={t("label.type")}>
              <select className={inputClassName()} value={type} onChange={(event) => setType(event.target.value as AttachmentType)}>
                {attachmentTypeOptions.map((option) => <option key={option.value} value={option.value}>{option.label}</option>)}
              </select>
            </Field>
            <Field label={t("label.visibility")}>
              <select className={inputClassName()} value={visibility} onChange={(event) => setVisibility(event.target.value as AttachmentVisibility)}>
                {visibilityOptions.map((option) => <option key={option.value} value={option.value}>{option.label}</option>)}
              </select>
              <span className="mt-1 block text-xs text-muted">
                {selectedVisibility?.description}
              </span>
            </Field>
            <Field label={t("label.description")}>
              <input className={inputClassName()} value={description} onChange={(event) => setDescription(event.target.value)} />
            </Field>
          </div>
          <Button type="button" variant="secondary" className="mt-3" onClick={() => void onUpload()} disabled={uploading}>
            <Upload className="h-4 w-4" />
            {uploading ? t("attachments.uploading") : t("attachments.upload")}
          </Button>
        </details>
      ) : null}
    </div>
  );
}
