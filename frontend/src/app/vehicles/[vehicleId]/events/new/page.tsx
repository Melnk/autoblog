"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import { ArrowLeft } from "lucide-react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useState } from "react";
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
import { VEHICLE_EVENT_TYPE_OPTIONS, getVehicleEventTypeOptions, useLanguage } from "@/lib/i18n";

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
        message: "Payload должен быть валидным JSON"
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
      <Link href={`/vehicles/${vehicleId}`} className="mb-6 inline-flex items-center gap-2 text-sm text-slate-400 hover:text-white">
        <ArrowLeft className="h-4 w-4" />
        {t("events.backToVehicle")}
      </Link>
      <SectionHeader title={t("events.newTitle")} description={t("events.newDescription")} />
      <Card className="max-w-5xl">
        <form className="grid gap-4 md:grid-cols-2" onSubmit={handleSubmit(onSubmit)}>
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
            <input className={inputClassName()} placeholder="Замена масла" {...register("title")} />
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
            <input className={inputClassName()} placeholder="Гаражный сервис" {...register("serviceName")} />
          </Field>
          <div className="md:col-span-2">
            <Field label={t("label.description")} error={errors.description?.message}>
              <textarea className={textareaClassName()} {...register("description")} />
            </Field>
          </div>
          <div className="md:col-span-2">
            <Field
              label="Payload JSON"
              hint="Только extension details. Пробег, стоимость, валюта и сервис должны быть top-level полями."
              error={errors.payload?.message}
            >
              <textarea className={textareaClassName()} placeholder='{"oil":"5W-40","parts":["oil_filter"]}' {...register("payload")} />
            </Field>
          </div>
          <div className="md:col-span-2">
            <Button type="submit" disabled={isSubmitting}>
              {isSubmitting ? t("events.adding") : t("events.add")}
            </Button>
          </div>
        </form>
      </Card>
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
