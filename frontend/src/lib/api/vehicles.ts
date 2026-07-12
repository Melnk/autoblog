import { apiRequest } from "@/lib/api/client";
import type { PublicReportMetadataDto, TrustScoreResponse, VehicleAccessDto, VehicleAccessRole, VehicleDto } from "@/lib/api/types";

export type CreateVehiclePayload = {
  vin: string;
  make?: string;
  model?: string;
  generation?: string;
  year?: number;
  engine?: string;
  transmission?: string;
  trim?: string;
  market?: string;
};

export function listVehicles() {
  return apiRequest<VehicleDto[]>("/api/v1/vehicles");
}

export function getVehicle(vehicleId: string) {
  return apiRequest<VehicleDto>(`/api/v1/vehicles/${vehicleId}`);
}

export function getVehicleTrustScore(vehicleId: string) {
  return apiRequest<TrustScoreResponse>(`/api/v1/vehicles/${vehicleId}/trust-score`);
}

export function createVehicle(payload: CreateVehiclePayload) {
  return apiRequest<VehicleDto>("/api/v1/vehicles", {
    method: "POST",
    body: payload
  });
}

export function createPublicReport(vehicleId: string) {
  return apiRequest<PublicReportMetadataDto>(`/api/v1/vehicles/${vehicleId}/public-report`, {
    method: "POST"
  });
}

export function listVehicleAccess(vehicleId: string) {
  return apiRequest<VehicleAccessDto[]>(`/api/v1/vehicles/${vehicleId}/access`);
}

export function grantVehicleAccess(vehicleId: string, email: string, role: Exclude<VehicleAccessRole, "OWNER">) {
  return apiRequest<VehicleAccessDto>(`/api/v1/vehicles/${vehicleId}/access`, {
    method: "POST",
    body: { email, role }
  });
}
