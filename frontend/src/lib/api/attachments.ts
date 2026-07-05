import { API_BASE_URL, apiRequest } from "@/lib/api/client";
import type { AttachmentType, AttachmentVisibility, EventAttachmentDto } from "@/lib/api/types";

export type UploadAttachmentPayload = {
  file: File;
  type: AttachmentType;
  visibility?: AttachmentVisibility;
  description?: string;
};

export function listAttachments(vehicleId: string, eventId: string) {
  return apiRequest<EventAttachmentDto[]>(`/api/v1/vehicles/${vehicleId}/events/${eventId}/attachments`);
}

export function uploadAttachment(vehicleId: string, eventId: string, payload: UploadAttachmentPayload) {
  const formData = new FormData();
  formData.append("file", payload.file);
  formData.append("type", payload.type);
  if (payload.visibility) {
    formData.append("visibility", payload.visibility);
  }
  if (payload.description) {
    formData.append("description", payload.description);
  }

  return apiRequest<EventAttachmentDto>(`/api/v1/vehicles/${vehicleId}/events/${eventId}/attachments`, {
    method: "POST",
    formData
  });
}

export function downloadAttachment(vehicleId: string, eventId: string, attachmentId: string) {
  return apiRequest<Blob>(`/api/v1/vehicles/${vehicleId}/events/${eventId}/attachments/${attachmentId}/download`);
}

export function publicAttachmentUrl(downloadUrl: string) {
  return `${API_BASE_URL}${downloadUrl}`;
}
