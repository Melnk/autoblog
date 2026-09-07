"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import { ArrowLeft } from "lucide-react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { ProtectedRoute } from "@/components/auth/protected-route";
import { AppShell } from "@/components/layout/app-shell";
import { Button } from "@/components/ui/button";
import { Card, SectionHeader } from "@/components/ui/card";
import { ErrorMessage } from "@/components/ui/error-message";
import { Field, inputClassName, textareaClassName } from "@/components/ui/form";
import { ApiError, readableApiError } from "@/lib/api/client";
import { createEvent, type CreateEventPayload } from "@/lib/api/events";
import { getVehicle } from "@/lib/api/vehicles";
import { VEHICLE_EVENT_TYPE_OPTIONS, getVehicleEventTypeOptions, useLanguage } from "@/lib/i18n";
import { canEditVehicle } from "@/lib/permissions";

const optionalPositiveInteger = z.preprocess(
  (value) => value === "" || value === null || value === undefined ? undefined : Number(value),
  z.number().int().positive("Значение должно быть положительным").optional()
);

const optionalPositiveNumber = z.preprocess(
  (value) => value === "" || value === null || value === undefined ? undefined : Number(value),
  z.number().positive("Значение должно быть положительным").optional()
);

const schema = z.object({
  type: z.enum(VEHICLE_EVENT_TYPE_OPTIONS),
  eventDate: z.string().min(1, "Дата обязательна"),
  odometerKm: optionalPositiveInteger,
  title: z.string().min(1, "Название обязательно"),
  description: z.string().optional(),
  costAmount: optionalPositiveNumber,
  costCurrency: z.string().default("RUB"),
  serviceName: z.string().optional(),
  payload: z.string().optional()
}).superRefine((value, context) => {
  if (value.payload?.trim()) {
    try {
      JSON.parse(value.payload);
    } catch {
      context.addIssue({
        code: z.ZodIssueCode.custom,
        path: ["payload"],
        message: "Проверьте формат дополнительных данных: нужен корректный JSON"
      });
    }
  }
});

type FormValues = z.infer<typeof schema>;

export default function NewEventPage({ params }: { params: { vehicleId: string } }) {
  return (
    <ProtectedRoute>
      <AppShell>
        <NewEventContent vehicleId={params.vehicleId} />
      </AppShell>
    </ProtectedRoute>
  );
}

function NewEventContent({ vehicleId }: { vehicleId: string }) {
  const router = useRouter();
  const { language, t } = useLanguage();
  const [apiError, setApiError] = useState<unknown>(null);
  const [permissionLoading, setPermissionLoading] = useState(true);
  const [canEdit, setCanEdit] = useState(false);
  const eventTypeOptions = getVehicleEventTypeOptions(language);
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: {
      type: "MAINTENANCE",
      eventDate: new Date().toISOString().slice(0, 10),
      title: "",
      description: "",
      costCurrency: "RUB",
      serviceName: "",
      payload: ""
    }
  });

  useEffect(() => {
    async function loadPermission() {
      try {
        const vehicle = await getVehicle(vehicleId);
        const allowed = canEditVehicle(vehicle.role);
        setCanEdit(allowed);
        if (!allowed) {
          router.replace(`/vehicles/${vehicleId}`);
        }
      } catch (error) {
        setApiError(error);
      } finally {
        setPermissionLoading(false);
      }
    }
    void loadPermission();
  }, [router, vehicleId]);

  async function onSubmit(values: FormValues) {
    setApiError(null);
    try {
      await createEvent(vehicleId, cleanEventPayload(values));
      router.push(`/vehicles/${vehicleId}?eventCreated=1`);
    } catch (error) {
      setApiError(error);
    }
  }

  return (
    <div>
      <Link href={`/vehicles/${vehicleId}`} className="mb-6 inline-flex items-center gap-2 text-sm text-muted hover:text-ink">
        <ArrowLeft className="h-4 w-4" />
        {t("events.backToVehicle")}
      </Link>
      <SectionHeader title={t("events.newTitle")} description={t("events.newDescription")} />
      {apiError && !canEdit ? (
        <ErrorMessage
          message={readableApiError(apiError, language)}
          details={apiError instanceof ApiError ? apiError.details : []}
        />
      ) : permissionLoading || !canEdit ? (
        <Card className="text-muted">{t("common.loading")}</Card>
      ) : (
      <Card className="max-w-4xl">
        <form className="grid gap-5 md:grid-cols-2" onSubmit={handleSubmit(onSubmit)}>
          <div className="md:col-span-2">
            <ErrorMessage
              message={apiError ? readableApiError(apiError, language) : null}
              details={apiError instanceof ApiError ? apiError.details : []}
            />
          </div>
          <Field label={t("label.type")} error={errors.type?.message}>
            <select className={inputClassName()} {...register("type")}>
              {eventTypeOptions.map((option) => <option key={option.value} value={option.value}>{option.label}</option>)}
            </select>
          </Field>
          <Field label={t("label.date")} error={errors.eventDate?.message}>
            <input className={inputClassName()} type="date" {...register("eventDate")} />
          </Field>
          <Field label={t("label.title")} error={errors.title?.message}>
            <input className={inputClassName()} placeholder={language === "ru" ? "Например, замена масла" : "For example, oil change"} {...register("title")} />
          </Field>
          <Field label={t("label.odometerKm")} error={errors.odometerKm?.message}>
            <input className={inputClassName()} inputMode="numeric" placeholder="120000" {...register("odometerKm")} />
          </Field>
          <Field label={t("label.cost")} error={errors.costAmount?.message}>
            <input className={inputClassName()} inputMode="decimal" placeholder="5000" {...register("costAmount")} />
          </Field>
          <Field label={t("label.currency")} error={errors.costCurrency?.message}>
            <input className={inputClassName()} {...register("costCurrency")} />
          </Field>
          <Field label={t("label.service")} error={errors.serviceName?.message}>
            <input className={inputClassName()} placeholder={language === "ru" ? "Где обслуживали автомобиль" : "Where the car was serviced"} {...register("serviceName")} />
          </Field>
          <div className="md:col-span-2">
            <Field label={t("label.description")} error={errors.description?.message}>
              <textarea className={textareaClassName()} {...register("description")} />
            </Field>
          </div>
          <details className="rounded-2xl border border-line bg-canvas p-4 md:col-span-2" open={errors.payload ? true : undefined}>
            <summary className="cursor-pointer text-sm font-medium text-muted focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-4 focus-visible:outline-ink">{language === "ru" ? "Технические сведения · необязательно" : "Technical details · optional"}</summary>
            <div className="mt-4">
              <Field
                label={language === "ru" ? "Дополнительные данные (JSON)" : "Additional data (JSON)"}
                hint={language === "ru" ? "Например, марка масла или список запчастей. Основные сведения укажите в полях выше." : "For example, an oil grade or parts list. Use the fields above for the main event details."}
                error={errors.payload?.message}
              >
                <textarea className={textareaClassName()} placeholder='{"oil":"5W-40","parts":["oil_filter"]}' {...register("payload")} />
              </Field>
            </div>
          </details>
          <div className="pt-2 md:col-span-2">
            <Button type="submit" disabled={isSubmitting}>
              {isSubmitting ? t("events.adding") : t("events.add")}
            </Button>
          </div>
        </form>
      </Card>
      )}
    </div>
  );
}

function cleanEventPayload(values: FormValues): CreateEventPayload {
  return {
    type: values.type,
    eventDate: values.eventDate,
    odometerKm: values.odometerKm ?? undefined,
    title: values.title,
    description: values.description || undefined,
    costAmount: values.costAmount ?? undefined,
    costCurrency: values.costCurrency || "RUB",
    serviceName: values.serviceName || undefined,
    payload: values.payload?.trim() ? JSON.parse(values.payload) : undefined
  };
}
