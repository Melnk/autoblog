"use client";

import { cn } from "@/lib/utils";
import type { VehicleAccessRole } from "@/lib/api/types";
import { getEnumLabel, useLanguage } from "@/lib/i18n";

const roleStyles: Record<VehicleAccessRole, string> = {
  OWNER: "border-emerald-400/30 bg-emerald-400/10 text-emerald-200",
  EDITOR: "border-violet-400/30 bg-violet-400/10 text-violet-200",
  VIEWER: "border-blue-400/30 bg-blue-400/10 text-blue-200"
};

export function Badge({ className, children }: { className?: string; children: React.ReactNode }) {
  return (
    <span className={cn("inline-flex items-center rounded-full border border-slate-700 bg-slate-800/70 px-2.5 py-1 text-xs font-semibold text-slate-200", className)}>
      {children}
    </span>
  );
}

export function RoleBadge({ role }: { role: VehicleAccessRole }) {
  const { language } = useLanguage();

  return <Badge className={roleStyles[role]}>{getEnumLabel(language, "vehicleAccessRole", role)}</Badge>;
}
