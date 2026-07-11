import { apiRequest } from "@/lib/api/client";
import type {
  MaintenanceReminder,
  ReminderDueState,
  ReminderStatus,
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
  filters: { status?: ReminderStatus; dueState?: ReminderDueState } = {}
) {
  const params = new URLSearchParams();
  if (filters.status) {
    params.set("status", filters.status);
  }
  if (filters.dueState) {
    params.set("dueState", filters.dueState);
  }
  const query = params.toString();
  return apiRequest<MaintenanceReminder[]>(`/api/v1/vehicles/${vehicleId}/reminders${query ? `?${query}` : ""}`);
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
