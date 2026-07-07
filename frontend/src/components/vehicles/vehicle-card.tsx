"use client";

import { ArrowRight, Calendar, Gauge, ShieldCheck } from "lucide-react";
import Link from "next/link";
import { Badge, RoleBadge } from "@/components/ui/badge";
import { Card } from "@/components/ui/card";
import type { VehicleDto } from "@/lib/api/types";
import { useLanguage } from "@/lib/i18n";

export function VehicleCard({ vehicle }: { vehicle: VehicleDto }) {
  const title = [vehicle.make, vehicle.model].filter(Boolean).join(" ") || "Автомобиль";
  const { t } = useLanguage();

  return (
    <Card className="group overflow-hidden p-0 transition hover:border-blue-400/50 hover:shadow-glow">
      <div className="relative h-36 border-b border-slate-800 bg-gradient-to-br from-blue-500/20 via-surface-900 to-emerald-500/10">
        <div className="absolute inset-x-6 bottom-5">
          <div className="flex items-center justify-between gap-3">
            <div>
              <h2 className="text-xl font-bold text-white">{vehicle.year ? `${vehicle.year} ` : ""}{title}</h2>
              <p className="mt-1 text-xs uppercase tracking-wide text-slate-400">VIN {vehicle.vin}</p>
            </div>
            {vehicle.role ? <RoleBadge role={vehicle.role} /> : null}
          </div>
        </div>
      </div>
      <div className="space-y-5 p-5">
        <div className="grid grid-cols-2 gap-3 text-sm">
          <Spec label={t("label.engine")} value={vehicle.engine} />
          <Spec label={t("label.transmission")} value={vehicle.transmission} />
          <Spec label={t("label.trim")} value={vehicle.trim} />
          <Spec label={t("label.market")} value={vehicle.market} />
        </div>
        <div className="flex flex-wrap gap-2">
          {vehicle.generation ? <Badge>{vehicle.generation}</Badge> : null}
          <Badge className="gap-1">
            <ShieldCheck className="h-3.5 w-3.5 text-emerald-300" />
            История
          </Badge>
          {vehicle.year ? (
            <Badge className="gap-1">
              <Calendar className="h-3.5 w-3.5 text-blue-300" />
              {vehicle.year}
            </Badge>
          ) : null}
        </div>
        <Link
          href={`/vehicles/${vehicle.id}`}
          className="flex items-center justify-between rounded-lg border border-slate-800 bg-surface-900 px-4 py-3 text-sm font-semibold text-slate-200 transition group-hover:border-blue-400/60 group-hover:text-white"
        >
          {t("vehicles.open")}
          <ArrowRight className="h-4 w-4" />
        </Link>
      </div>
    </Card>
  );
}

function Spec({ label, value }: { label: string; value?: string | number | null }) {
  return (
    <div className="min-w-0 rounded-lg border border-slate-800 bg-black/20 p-3">
      <div className="flex items-center gap-2 text-[11px] uppercase tracking-wide text-slate-500">
        <Gauge className="h-3 w-3" />
        {label}
      </div>
      <div className="mt-1 truncate text-slate-200">{value || "—"}</div>
    </div>
  );
}
