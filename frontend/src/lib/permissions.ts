import type { VehicleAccessRole } from "@/lib/api/types";

export function canEditVehicle(role: VehicleAccessRole) {
  return role === "OWNER" || role === "EDITOR";
}

export function canManageVehicleAccess(role: VehicleAccessRole) {
  return role === "OWNER";
}
