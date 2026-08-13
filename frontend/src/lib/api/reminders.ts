import { apiRequest } from "@/lib/api/client";
import type {
  MaintenanceReminder,
  PagedResponse,
  ReminderType
} from "@/lib/api/types";

export type CreateMaintenanceReminderRequest = {
  title: string;
  description?: string;
  type: ReminderType;
  dueDate?: string;
  dueOdometerKm?: number;
};

export function listVehicleReminders(
  vehicleId: string,
  page = 0,
  size = 20
) {
  return apiRequest<PagedResponse<MaintenanceReminder>>(
    `/api/v2/vehicles/${vehicleId}/reminders?page=${page}&size=${size}`
  );
}

export function createVehicleReminder(vehicleId: string, request: CreateMaintenanceReminderRequest) {
  return apiRequest<MaintenanceReminder>(`/api/v1/vehicles/${vehicleId}/reminders`, {
    method: "POST",
    body: request
  });
}

export function completeVehicleReminder(vehicleId: string, reminderId: string) {
  return apiRequest<MaintenanceReminder>(`/api/v1/vehicles/${vehicleId}/reminders/${reminderId}/complete`, {
    method: "PATCH"
  });
}

export function cancelVehicleReminder(vehicleId: string, reminderId: string) {
  return apiRequest<MaintenanceReminder>(`/api/v1/vehicles/${vehicleId}/reminders/${reminderId}/cancel`, {
    method: "PATCH"
  });
}
