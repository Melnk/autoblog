"use client";

import { Plus, Search } from "lucide-react";
import { useEffect, useState } from "react";
import { ProtectedRoute } from "@/components/auth/protected-route";
import { AppShell } from "@/components/layout/app-shell";
import { VehicleCard } from "@/components/vehicles/vehicle-card";
import { ButtonLink } from "@/components/ui/button";
import { Card, SectionHeader } from "@/components/ui/card";
import { ErrorMessage } from "@/components/ui/error-message";
import { inputClassName } from "@/components/ui/form";
import { listVehicles } from "@/lib/api/vehicles";
import type { VehicleDto } from "@/lib/api/types";
import { readableApiError } from "@/lib/api/client";

export default function VehiclesPage() {
  return (
    <ProtectedRoute>
      <AppShell>
        <VehiclesContent />
      </AppShell>
    </ProtectedRoute>
  );
}

function VehiclesContent() {
  const [vehicles, setVehicles] = useState<VehicleDto[]>([]);
  const [query, setQuery] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    async function load() {
      try {
        setVehicles(await listVehicles());
      } catch (requestError) {
        setError(readableApiError(requestError));
      } finally {
        setLoading(false);
      }
    }

    void load();
  }, []);

  const filteredVehicles = vehicles.filter((vehicle) => matchesVehicle(vehicle, query));

  return (
    <div>
      <SectionHeader
        title="Автомобили"
        description="Ваши машины и их цифровая история."
        action={<ButtonLink href="/vehicles/new"><Plus className="h-4 w-4" />Добавить автомобиль</ButtonLink>}
      />
      <ErrorMessage message={error} />
      {loading ? (
        <Card className="text-slate-400">Загружаем автомобили…</Card>
      ) : vehicles.length === 0 ? (
        <Card className="flex flex-col items-start gap-4">
          <div>
            <h2 className="text-xl font-bold text-white">Пока нет автомобилей</h2>
            <p className="mt-2 max-w-xl text-sm text-slate-400">
              Добавьте первый автомобиль, чтобы вести append-only историю обслуживания, ремонта и доказательств.
            </p>
          </div>
          <ButtonLink href="/vehicles/new"><Plus className="h-4 w-4" />Добавить автомобиль</ButtonLink>
        </Card>
      ) : (
        <>
          <Card className="mb-5">
            <label className="relative block">
              <Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-500" />
              <input
                className={inputClassName("pl-10")}
                value={query}
                onChange={(event) => setQuery(event.target.value)}
                placeholder="Поиск по VIN, марке или модели"
              />
            </label>
          </Card>
          {filteredVehicles.length === 0 ? (
            <Card className="text-slate-400">По этому запросу автомобилей не найдено.</Card>
          ) : (
            <div className="grid gap-5 md:grid-cols-2 xl:grid-cols-3">
              {filteredVehicles.map((vehicle) => <VehicleCard key={vehicle.id} vehicle={vehicle} />)}
            </div>
          )}
        </>
      )}
    </div>
  );
}

function matchesVehicle(vehicle: VehicleDto, query: string) {
  const normalizedQuery = query.trim().toLowerCase();
  if (!normalizedQuery) {
    return true;
  }
  return [vehicle.vin, vehicle.make, vehicle.model]
    .filter(Boolean)
    .some((value) => String(value).toLowerCase().includes(normalizedQuery));
}
