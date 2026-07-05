"use client";

import { Download, Upload } from "lucide-react";
import { useEffect, useState } from "react";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { ErrorMessage } from "@/components/ui/error-message";
import { Field, inputClassName } from "@/components/ui/form";
import { downloadAttachment, listAttachments, uploadAttachment } from "@/lib/api/attachments";
import { readableApiError } from "@/lib/api/client";
import type { AttachmentType, AttachmentVisibility, EventAttachmentDto } from "@/lib/api/types";
import { formatBytes, shortHash } from "@/lib/utils";

const attachmentTypes: AttachmentType[] = ["PHOTO", "RECEIPT", "WORK_ORDER", "DIAGNOSTIC_REPORT", "PART_PHOTO", "OTHER"];
const visibilities: AttachmentVisibility[] = ["PRIVATE", "PUBLIC"];

export function AttachmentPanel({
  vehicleId,
  eventId,
  initialAttachments = []
}: {
  vehicleId: string;
  eventId: string;
  initialAttachments?: EventAttachmentDto[];
}) {
  const [attachments, setAttachments] = useState<EventAttachmentDto[]>(initialAttachments);
  const [file, setFile] = useState<File | null>(null);
  const [type, setType] = useState<AttachmentType>("RECEIPT");
  const [visibility, setVisibility] = useState<AttachmentVisibility>("PRIVATE");
  const [description, setDescription] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [uploading, setUploading] = useState(false);

  useEffect(() => {
    setAttachments(initialAttachments);
  }, [initialAttachments]);

  async function refresh() {
    setAttachments(await listAttachments(vehicleId, eventId));
  }

  async function onUpload() {
    if (!file) {
      setError("Выберите файл");
      return;
    }
    setError(null);
    setUploading(true);
    try {
      await uploadAttachment(vehicleId, eventId, { file, type, visibility, description });
      setFile(null);
      setDescription("");
      await refresh();
    } catch (requestError) {
      setError(readableApiError(requestError));
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
      setError(readableApiError(requestError));
    }
  }

  return (
    <div className="mt-4 rounded-xl border border-slate-800 bg-black/20 p-4">
      <div className="mb-4 flex items-center justify-between">
        <h4 className="text-sm font-semibold text-white">Доказательства</h4>
        <Badge>{attachments.length}</Badge>
      </div>
      <ErrorMessage message={error} />
      {attachments.length > 0 ? (
        <div className="mt-3 space-y-2">
          {attachments.map((attachment) => (
            <div key={attachment.id} className="flex flex-col gap-3 rounded-lg border border-slate-800 bg-surface-900 p-3 sm:flex-row sm:items-center sm:justify-between">
              <div className="min-w-0">
                <div className="truncate text-sm font-semibold text-slate-100">{attachment.originalFilename}</div>
                <div className="mt-1 flex flex-wrap gap-2 text-xs text-slate-400">
                  <span>{attachment.type}</span>
                  <span>{attachment.visibility}</span>
                  <span>{formatBytes(attachment.sizeBytes)}</span>
                  <span>{shortHash(attachment.checksumSha256)}</span>
                </div>
                {attachment.description ? <p className="mt-1 text-xs text-slate-500">{attachment.description}</p> : null}
              </div>
              <Button type="button" variant="secondary" className="h-9 shrink-0" onClick={() => void onDownload(attachment)}>
                <Download className="h-4 w-4" />
                Скачать
              </Button>
            </div>
          ))}
        </div>
      ) : (
        <p className="mt-3 text-sm text-slate-500">Вложений пока нет.</p>
      )}

      <div className="mt-4 grid gap-3 md:grid-cols-2 xl:grid-cols-4">
        <Field label="Файл">
          <input className={inputClassName("pt-2")} type="file" onChange={(event) => setFile(event.target.files?.[0] ?? null)} />
        </Field>
        <Field label="Тип">
          <select className={inputClassName()} value={type} onChange={(event) => setType(event.target.value as AttachmentType)}>
            {attachmentTypes.map((item) => <option key={item} value={item}>{item}</option>)}
          </select>
        </Field>
        <Field label="Видимость">
          <select className={inputClassName()} value={visibility} onChange={(event) => setVisibility(event.target.value as AttachmentVisibility)}>
            {visibilities.map((item) => <option key={item} value={item}>{item}</option>)}
          </select>
        </Field>
        <Field label="Описание">
          <input className={inputClassName()} value={description} onChange={(event) => setDescription(event.target.value)} />
        </Field>
      </div>
      <Button type="button" variant="secondary" className="mt-3" onClick={() => void onUpload()} disabled={uploading}>
        <Upload className="h-4 w-4" />
        {uploading ? "Загружаем…" : "Загрузить вложение"}
      </Button>
    </div>
  );
}
