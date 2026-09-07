"use client";

import { ArrowRight, CalendarDays, CarFront } from "lucide-react";
import Link from "next/link";
import { RoleBadge } from "@/components/ui/badge";
import { Card } from "@/components/ui/card";
import { CarIllustration } from "@/components/vehicles/car-illustration";
import type { VehicleDto } from "@/lib/api/types";
import { useLanguage } from "@/lib/i18n";

export function VehicleCard({ vehicle }: { vehicle: VehicleDto }) {
  const { language, t } = useLanguage();
  const title = [vehicle.make, vehicle.model].filter(Boolean).join(" ") || (language === "ru" ? "Автомобиль" : "Vehicle");

  return (
    <Card className="group overflow-hidden p-0 transition-shadow hover:shadow-lift sm:p-0">
      <div className="relative bg-canvas px-5 pt-4">
        <div className="flex items-center justify-between gap-2">
          <span className="flex items-center gap-1.5 text-xs text-muted"><CarFront className="h-3.5 w-3.5" />
            {language === "ru" ? "Мой автомобиль" : "My vehicle"}
          </span>
          <RoleBadge role={vehicle.role} />
        </div>
        <CarIllustration className="mx-auto h-36 max-w-72" />
      </div>
      <div className="p-5">
        <div className="flex flex-wrap items-center justify-between gap-2">
          <h3 className="min-w-0 break-words text-xl font-bold tracking-tight">{title}</h3>
          {vehicle.year && <span className="inline-flex items-center gap-1.5 text-xs text-muted"><CalendarDays className="h-3.5 w-3.5" />{vehicle.year}</span>}
        </div>
        <p className="mt-2 break-all font-mono text-[11px] tracking-wide text-muted">VIN {vehicle.vin}</p>
        <div className="my-5 grid grid-cols-2 gap-3 border-y border-line py-4">
          <Spec label={t("label.engine")} value={vehicle.engine} />
          <Spec label={t("label.transmission")} value={vehicle.transmission} />
        </div>
        <Link href={`/vehicles/${vehicle.id}`} aria-label={`${t("vehicles.open")}: ${title}`}
          className="flex min-h-11 items-center justify-between gap-2 rounded-xl bg-canvas px-4 py-3 text-sm font-semibold transition-colors hover:bg-brand-yellow">
          {t("vehicles.open")}<ArrowRight className="h-4 w-4" />
        </Link>
      </div>
    </Card>
  );
}

function Spec({ label, value }: { label: string; value?: string | number | null }) {
  return <div className="min-w-0"><p className="text-xs text-muted">{label}</p><p className="mt-1 truncate text-sm font-semibold">{value || "—"}</p></div>;
}
