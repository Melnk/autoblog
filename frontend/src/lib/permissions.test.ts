import { describe, expect, it } from "vitest";
import { canEditVehicle, canManageVehicleAccess } from "@/lib/permissions";

describe("vehicle permissions", () => {
  it("allows owners to edit and manage access", () => {
    expect(canEditVehicle("OWNER")).toBe(true);
    expect(canManageVehicleAccess("OWNER")).toBe(true);
  });

  it("allows editors to edit without managing access", () => {
    expect(canEditVehicle("EDITOR")).toBe(true);
    expect(canManageVehicleAccess("EDITOR")).toBe(false);
  });

  it("keeps viewers read-only", () => {
    expect(canEditVehicle("VIEWER")).toBe(false);
    expect(canManageVehicleAccess("VIEWER")).toBe(false);
  });
});
