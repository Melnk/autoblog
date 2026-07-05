import { apiRequest } from "@/lib/api/client";
import type { VehicleEventDto, VehicleEventType } from "@/lib/api/types";

export type CreateEventPayload = {
  type: VehicleEventType;
  eventDate: string;
  odometerKm?: number;
  title: string;
  description?: string;
  costAmount?: number;
  costCurrency?: string;
  serviceName?: string;
  payload?: unknown;
};

export function listEvents(vehicleId: string) {
  return apiRequest<VehicleEventDto[]>(`/api/v1/vehicles/${vehicleId}/events`);
}

export function createEvent(vehicleId: string, payload: CreateEventPayload) {
  return apiRequest<VehicleEventDto>(`/api/v1/vehicles/${vehicleId}/events`, {
    method: "POST",
    body: payload
  });
}
