"use client";

import { ArrowRight, BookOpen, CarFront, Check, FileCheck2, Plus, Search, ShieldCheck, Wrench, X } from "lucide-react";
import { useEffect, useState } from "react";
import { useAuth } from "@/components/auth/auth-provider";
import { ProtectedRoute } from "@/components/auth/protected-route";
import { AppShell } from "@/components/layout/app-shell";
import { VehicleCard } from "@/components/vehicles/vehicle-card";
import { CarIllustration } from "@/components/vehicles/car-illustration";
import { Button, ButtonLink } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { ErrorMessage } from "@/components/ui/error-message";
import { inputClassName } from "@/components/ui/form";
import { PaginationControls } from "@/components/ui/pagination-controls";
import { listVehicles } from "@/lib/api/vehicles";
import type { VehicleDto } from "@/lib/api/types";
import { readableApiError } from "@/lib/api/client";
import { useLanguage } from "@/lib/i18n";

export default function VehiclesPage() {
  return <ProtectedRoute><AppShell><VehiclesContent /></AppShell></ProtectedRoute>;
}

function VehiclesContent() {
  const [vehicles, setVehicles] = useState<VehicleDto[]>([]);
  const [query, setQuery] = useState("");
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [reloadKey, setReloadKey] = useState(0);
  const { language, t } = useLanguage();
  const { user } = useAuth();
  const ru = language === "ru";

  useEffect(() => {
    let cancelled = false;
    async function load() {
      setLoading(true);
      setError(null);
      try {
        const response = await listVehicles(page);
        if (cancelled) return;
        setVehicles(response.items);
        setTotalPages(response.totalPages);
        setTotalElements(response.totalElements);
      } catch (requestError) {
        if (!cancelled) setError(readableApiError(requestError, language));
      } finally {
        if (!cancelled) setLoading(false);
      }
    }
    void load();
    return () => { cancelled = true; };
  }, [language, page, reloadKey]);

  const filteredVehicles = vehicles.filter((vehicle) => matchesVehicle(vehicle, query));
  const firstName = user?.displayName?.trim().split(/\s+/)[0];
  const steps = [
    { icon: CarFront, title: ru ? "Добавьте автомобиль" : "Add your vehicle", text: ru ? "Марка, модель и VIN — начало вашей истории." : "Make, model and VIN: the start of your story." },
    { icon: Wrench, title: ru ? "Записывайте важное" : "Keep the essentials", text: ru ? "ТО, ремонт, пробег. Чеки и фото тоже сохранятся." : "Service, repairs and mileage, with receipts and photos." },
    { icon: FileCheck2, title: ru ? "Делитесь историей" : "Share the story", text: ru ? "Отправьте покупателю отчёт по одной ссылке." : "Send a buyer your report with a single link." }
  ];

  return (
    <div>
      <div className="mb-7 flex flex-wrap items-start justify-between gap-4">
        <div className="min-w-0 max-w-full">
          <p className="mb-1.5 text-sm text-muted [overflow-wrap:anywhere]">{ru ? "Хорошего дня" : "Have a good day"}{firstName ? `, ${firstName}` : ""}!</p>
          <h1 className="text-[32px] font-bold leading-tight tracking-tight sm:text-4xl">{t("vehicles.title")}</h1>
        </div>
        <ButtonLink href="/vehicles/new"><Plus className="h-4 w-4" />{t("nav.addVehicle")}</ButtonLink>
      </div>

      <section aria-label={ru ? "Добро пожаловать в AutoBlog" : "Welcome to AutoBlog"}
        className="relative mb-8 grid overflow-hidden rounded-[28px] bg-brand-soft md:grid-cols-[1.1fr_1fr]">
        <div className="relative z-10 p-6 sm:p-8 lg:p-9">
          <span className="inline-flex items-center gap-1.5 rounded-full bg-white/70 px-3 py-1 text-[11px] font-semibold">
            <span className="h-1.5 w-1.5 rounded-full bg-ink" />{ru ? "Меньше забот. Больше приятных поездок." : "Less worry. More happy journeys."}
          </span>
          <h2 className="mt-5 max-w-md text-[28px] font-bold leading-[1.2] tracking-tight sm:text-[34px]">
            {ru ? <>Ваш автомобиль.<br />Всё под рукой.</> : <>Your car.<br />All in one place.</>}
          </h2>
          <p className="mt-3 max-w-sm text-sm leading-6 text-ink/75">
            {ru ? "Обслуживание, расходы и документы — соберите историю, к которой легко вернуться." : "Service, expenses and documents. Keep a history you can always come back to."}
          </p>
          <a href="#garage" className="mt-5 inline-flex min-h-11 items-center gap-2 text-sm font-semibold underline decoration-ink/25 underline-offset-4 hover:decoration-ink">
            {ru ? "К моим автомобилям" : "Go to my vehicles"}<ArrowRight className="h-4 w-4" />
          </a>
        </div>
        <div className="relative flex items-center justify-center px-6 pb-5 md:p-3">
          <CarIllustration className="max-w-[440px]" />
          <span className="absolute bottom-6 right-5 inline-flex items-center gap-2 rounded-2xl bg-white px-4 py-3 text-xs font-semibold shadow-card md:bottom-7 md:right-7">
            <ShieldCheck className="h-4 w-4" />{ru ? "У каждой машины своя история" : "Every car has a story"}
          </span>
        </div>
      </section>

      <div className="grid items-start gap-6 xl:grid-cols-[minmax(0,1fr)_280px]">
        <section id="garage" className="min-w-0 scroll-mt-5" aria-labelledby="garage-title">
          <div className="mb-5 flex flex-wrap items-center justify-between gap-3">
            <h2 id="garage-title" className="flex items-center gap-2.5 text-xl font-bold">
              {ru ? "Мои автомобили" : "My vehicles"}
              {!loading && !error && <span className="rounded-lg bg-white px-2.5 py-1 text-xs text-muted">{totalElements}</span>}
            </h2>
            <span className="text-xs text-muted">{ru ? "Вся история начинается здесь" : "Every story starts here"}</span>
          </div>

          {error ? (
            <Card>
              <ErrorMessage message={error} />
              <Button type="button" variant="secondary" className="mt-4" onClick={() => setReloadKey((value) => value + 1)}>
                {ru ? "Попробовать снова" : "Try again"}
              </Button>
            </Card>
          ) : loading ? (
            <div role="status" aria-label={t("vehicles.loading")} className="grid gap-5 sm:grid-cols-2">
              {[0, 1].map((key) => <Card key={key} className="animate-pulse"><div className="h-40 rounded-2xl bg-canvas" /><div className="mt-5 h-5 w-2/3 rounded bg-canvas" /><div className="mt-3 h-3 w-1/2 rounded bg-canvas" /><div className="mt-7 h-11 rounded-xl bg-canvas" /></Card>)}
              <span className="sr-only">{t("vehicles.loading")}</span>
            </div>
          ) : vehicles.length === 0 ? (
            <Card className="flex min-h-[340px] flex-col items-center justify-center border-dashed px-6 py-10 text-center sm:py-10">
              <span className="mb-5 flex h-16 w-16 items-center justify-center rounded-3xl bg-brand-soft"><CarFront className="h-8 w-8" strokeWidth={1.5} /></span>
              <h3 className="text-xl font-bold">{t("vehicles.emptyTitle")}</h3>
              <p className="mb-6 mt-2 max-w-sm text-sm leading-6 text-muted">{t("vehicles.emptyDescription")}</p>
              <ButtonLink href="/vehicles/new"><Plus className="h-4 w-4" />{t("vehicles.add")}</ButtonLink>
            </Card>
          ) : (
            <>
              <div className="relative mb-5">
                <Search className="pointer-events-none absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-muted" />
                <input type="search" className={inputClassName("bg-white pl-11 pr-11")} value={query}
                  onChange={(event) => setQuery(event.target.value)} aria-label={t("vehicles.searchPlaceholder")}
                  placeholder={t("vehicles.searchPlaceholder")} />
                {query && <button type="button" onClick={() => setQuery("")}
                  aria-label={ru ? "Очистить поиск" : "Clear search"} className="absolute right-1 top-1 flex h-10 w-10 items-center justify-center rounded-xl text-muted hover:bg-canvas"><X className="h-4 w-4" /></button>}
              </div>
              {totalPages > 1 && <p className="mb-4 text-xs text-muted">{ru ? "Поиск по автомобилям на этой странице" : "Search vehicles on this page"}</p>}
              {filteredVehicles.length === 0 ? (
                <Card className="py-10 text-center sm:py-10">
                  <Search className="mx-auto mb-3 h-6 w-6 text-muted" />
                  <p className="font-semibold">{t("vehicles.noSearchResults")}</p>
                  <Button type="button" variant="ghost" className="mt-3" onClick={() => setQuery("")}>{ru ? "Сбросить поиск" : "Clear search"}</Button>
                </Card>
              ) : (
                <div className="grid gap-5 sm:grid-cols-2 2xl:grid-cols-3">
                  {filteredVehicles.map((vehicle) => <VehicleCard key={vehicle.id} vehicle={vehicle} />)}
                </div>
              )}
              <PaginationControls page={page} totalPages={totalPages} onPageChange={(nextPage) => { setPage(nextPage); setQuery(""); }} />
            </>
          )}
        </section>

        <aside className="grid gap-5 sm:grid-cols-2 xl:grid-cols-1">
          <Card>
            <span className="mb-4 inline-flex h-10 w-10 items-center justify-center rounded-2xl bg-canvas"><BookOpen className="h-5 w-5" strokeWidth={1.6} /></span>
            <h2 className="text-lg font-bold">{ru ? "Бортжурнал без хлопот" : "A simple car journal"}</h2>
            <p className="mb-5 mt-1.5 text-xs leading-5 text-muted">{ru ? "Три простых шага для спокойствия за рулём." : "Three small steps for peace of mind."}</p>
            <ol className="space-y-5">
              {steps.map((step, index) => <li key={step.title} className="flex gap-3">
                <span className="flex h-6 w-6 shrink-0 items-center justify-center rounded-full bg-brand-soft text-[11px] font-semibold">
                  {index === 0 && totalElements > 0 && !error ? <Check className="h-3.5 w-3.5" /> : index + 1}
                </span>
                <div><h3 className="text-xs font-bold">{step.title}</h3><p className="mt-1 text-xs leading-5 text-muted">{step.text}</p></div>
              </li>)}
            </ol>
          </Card>
          <div className="rounded-3xl bg-ink p-6 text-white">
            <FileCheck2 className="mb-4 h-7 w-7 text-brand-yellow" strokeWidth={1.5} />
            <h2 className="text-lg font-semibold">{ru ? "История говорит за вас" : "Let your history speak"}</h2>
            <p className="mt-2 text-xs leading-5 text-white/75">{ru ? "Когда придёт время продавать авто, поделитесь отчётом с покупателем. Данные аккаунта не показываются. Перед публикацией проверьте записи и документы." : "When it is time to sell, share a report with the buyer. Account details are hidden. Review your records and documents before sharing."}</p>
            <p className="mt-5 flex items-center gap-2 text-xs font-medium text-brand-yellow"><ShieldCheck className="h-4 w-4" />{ru ? "Вы решаете, чем поделиться" : "You choose what to share"}</p>
          </div>
        </aside>
      </div>
    </div>
  );
}

function matchesVehicle(vehicle: VehicleDto, query: string) {
  const normalizedQuery = query.trim().toLowerCase();
  if (!normalizedQuery) return true;
  return [vehicle.vin, vehicle.make, vehicle.model].filter(Boolean)
    .some((value) => String(value).toLowerCase().includes(normalizedQuery));
}
