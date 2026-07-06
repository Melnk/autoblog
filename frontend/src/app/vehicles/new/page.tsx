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
import { Field, inputClassName } from "@/components/ui/form";
import { ApiError, readableApiError } from "@/lib/api/client";
import { createVehicle, type CreateVehiclePayload } from "@/lib/api/vehicles";

const vinPattern = /^[A-Z0-9]+$/;
const forbiddenVinCharacters = /[IOQ]/;

const optionalYear = z.preprocess(
  (value) => value === "" || value === null || value === undefined ? undefined : Number(value),
  z.number().int().min(1886, "Проверьте год").max(2100, "Проверьте год").optional()
);

const schema = z.object({
  vin: z.string()
    .trim()
    .min(1, "VIN обязателен")
    .transform((value) => value.toUpperCase())
    .refine((value) => value.length === 17, "VIN должен состоять из 17 символов")
    .refine((value) => vinPattern.test(value), "VIN может содержать только A-Z и 0-9")
    .refine((value) => !forbiddenVinCharacters.test(value), "VIN не должен содержать I, O или Q"),
  make: z.string().optional(),
  model: z.string().optional(),
  generation: z.string().optional(),
  year: optionalYear,
  engine: z.string().optional(),
  transmission: z.string().optional(),
  trim: z.string().optional(),
  market: z.string().default("RU")
});

type FormValues = z.infer<typeof schema>;

export default function NewVehiclePage() {
  return (
    <ProtectedRoute>
      <AppShell>
        <NewVehicleContent />
      </AppShell>
    </ProtectedRoute>
  );
}

function NewVehicleContent() {
  const router = useRouter();
  const [apiError, setApiError] = useState<unknown>(null);
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: {
      vin: "",
      make: "",
      model: "",
      generation: "",
      engine: "",
      transmission: "",
      trim: "",
      market: "RU"
    }
  });

  async function onSubmit(values: FormValues) {
    setApiError(null);
    try {
      const vehicle = await createVehicle(cleanPayload(values));
      router.push(`/vehicles/${vehicle.id}`);
    } catch (error) {
      setApiError(error);
    }
  }

  const vinField = register("vin");

  return (
    <div>
      <Link href="/vehicles" className="mb-6 inline-flex items-center gap-2 text-sm text-slate-400 hover:text-white">
        <ArrowLeft className="h-4 w-4" />
        К автомобилям
      </Link>
      <SectionHeader title="Новый автомобиль" description="Создайте карточку машины. Вы автоматически станете OWNER." />
      <Card className="max-w-4xl">
        <form className="grid gap-4 md:grid-cols-2" onSubmit={handleSubmit(onSubmit)}>
          <div className="md:col-span-2">
            <ErrorMessage
              message={apiError ? readableApiError(apiError) : null}
              details={apiError instanceof ApiError ? apiError.details : []}
            />
          </div>
          <Field label="VIN" error={errors.vin?.message}>
            <input
              className={inputClassName()}
              maxLength={17}
              placeholder="XTA217030C0000000"
              {...vinField}
              onChange={(event) => {
                event.currentTarget.value = event.currentTarget.value.toUpperCase();
                void vinField.onChange(event);
              }}
            />
          </Field>
          <Field label="Рынок" error={errors.market?.message}>
            <input className={inputClassName()} {...register("market")} />
          </Field>
          <Field label="Марка" error={errors.make?.message}>
            <input className={inputClassName()} placeholder="Lada" {...register("make")} />
          </Field>
          <Field label="Модель" error={errors.model?.message}>
            <input className={inputClassName()} placeholder="Priora" {...register("model")} />
          </Field>
          <Field label="Поколение" error={errors.generation?.message}>
            <input className={inputClassName()} placeholder="2170" {...register("generation")} />
          </Field>
          <Field label="Год" error={errors.year?.message}>
            <input className={inputClassName()} inputMode="numeric" placeholder="2012" {...register("year")} />
          </Field>
          <Field label="Двигатель" error={errors.engine?.message}>
            <input className={inputClassName()} placeholder="1.6" {...register("engine")} />
          </Field>
          <Field label="КПП" error={errors.transmission?.message}>
            <input className={inputClassName()} placeholder="MT" {...register("transmission")} />
          </Field>
          <Field label="Комплектация" error={errors.trim?.message}>
            <input className={inputClassName()} placeholder="Norma" {...register("trim")} />
          </Field>
          <div className="flex items-end md:col-span-2">
            <Button type="submit" disabled={isSubmitting}>
              {isSubmitting ? "Создаем…" : "Создать автомобиль"}
            </Button>
          </div>
        </form>
      </Card>
    </div>
  );
}

function cleanPayload(values: FormValues): CreateVehiclePayload {
  return Object.fromEntries(
    Object.entries(values).filter(([, value]) => value !== "" && value !== undefined)
  ) as CreateVehiclePayload;
}
