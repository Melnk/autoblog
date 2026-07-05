import { apiRequest } from "@/lib/api/client";
import type { PublicReportDto } from "@/lib/api/types";

export function getPublicReport(publicToken: string) {
  return apiRequest<PublicReportDto>(`/api/v1/public/reports/${publicToken}`, {
    auth: false
  });
}
