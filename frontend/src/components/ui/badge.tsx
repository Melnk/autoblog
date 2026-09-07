"use client";

import { cn } from "@/lib/utils";
import type { VehicleAccessRole } from "@/lib/api/types";
import { getEnumLabel, useLanguage } from "@/lib/i18n";

const roleStyles: Record<VehicleAccessRole, string> = {
  OWNER: "border-transparent bg-brand-soft text-ink",
  EDITOR: "border-line bg-white text-ink",
  VIEWER: "border-line bg-canvas text-muted"
};

export function Badge({ className, children }: { className?: string; children: React.ReactNode }) {
  return (
    <span className={cn("inline-flex items-center rounded-full border border-line bg-canvas px-3 py-1 text-xs font-medium text-muted", className)}>
      {children}
    </span>
  );
}

export function RoleBadge({ role }: { role: VehicleAccessRole }) {
  const { language } = useLanguage();

  return <Badge className={roleStyles[role]}>{getEnumLabel(language, "vehicleAccessRole", role)}</Badge>;
}
