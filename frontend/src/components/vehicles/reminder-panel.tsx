"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import { Bell, CalendarDays, Check, Gauge, Plus, X } from "lucide-react";
import { useCallback, useEffect, useState } from "react";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { ErrorMessage } from "@/components/ui/error-message";
import { Field, inputClassName, textareaClassName } from "@/components/ui/form";
import { PaginationControls } from "@/components/ui/pagination-controls";
import { ApiError, readableApiError } from "@/lib/api/client";
import {
  cancelVehicleReminder,
  completeVehicleReminder,
  createVehicleReminder,
  listVehicleReminders,
  type CreateMaintenanceReminderRequest
} from "@/lib/api/reminders";
import type { MaintenanceReminder, ReminderDueState } from "@/lib/api/types";
import { formatDate, formatKm } from "@/lib/format";
import { getEnumLabel, getReminderTypeOptions, REMINDER_TYPE_OPTIONS, useLanguage } from "@/lib/i18n";
import { cn } from "@/lib/utils";

const optionalPositiveInteger = z.preprocess(
  (value) => value === "" || value === null || value === undefined ? undefined : Number(value),
  z.number().int().positive("Значение должно быть положительным").optional()
);

const schema = z.object({
  title: z.string().trim().min(1, "Название обязательно"),
  type: z.enum(REMINDER_TYPE_OPTIONS),
  dueDate: z.string().optional(),
  dueOdometerKm: optionalPositiveInteger,
  description: z.string().optional()
}).superRefine((value, context) => {
  if (!value.dueDate?.trim() && value.dueOdometerKm === undefined) {
    context.addIssue({
      code: z.ZodIssueCode.custom,
      path: ["dueDate"],
      message: "Укажите срок по дате или пробегу"
    });
  }
});

type FormValues = z.infer<typeof schema>;

const dueStateStyles: Record<ReminderDueState, string> = {
  OVERDUE: "border-red-200 bg-red-50 text-red-700",
  DUE_SOON: "border-amber-200 bg-amber-50 text-amber-800",
  UPCOMING: "border-brand-yellow bg-brand-soft text-ink",
  COMPLETED: "border-emerald-200 bg-emerald-50 text-emerald-700",
  CANCELLED: "border-line bg-canvas text-ink"
};

export function ReminderPanel({ vehicleId, canEdit }: { vehicleId: string; canEdit: boolean }) {
  const { language, t } = useLanguage();
  const [reminders, setReminders] = useState<MaintenanceReminder[]>([]);
  const [loading, setLoading] = useState(true);
  const [formOpen, setFormOpen] = useState(false);
  const [message, setMessage] = useState<string | null>(null);
  const [error, setError] = useState<unknown>(null);
  const [actionLoading, setActionLoading] = useState<string | null>(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const reminderTypeOptions = getReminderTypeOptions(language);

  const { register, handleSubmit, reset, formState: { errors, isSubmitting } } = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: {
      title: "",
      type: "OIL_CHANGE",
      dueDate: "",
      dueOdometerKm: undefined,
      description: ""
    }
  });

  const refresh = useCallback(async () => {
    const response = await listVehicleReminders(vehicleId, page);
    setReminders(response.items);
    setTotalPages(response.totalPages);
  }, [page, vehicleId]);

  useEffect(() => {
    async function load() {
      setLoading(true);
      setError(null);
      try {
        await refresh();
      } catch (requestError) {
        setError(requestError);
      } finally {
        setLoading(false);
      }
    }

    void load();
  }, [refresh]);

  async function onSubmit(values: FormValues) {
    setError(null);
    setMessage(null);
    try {
      await createVehicleReminder(vehicleId, cleanPayload(values));
      reset();
      setFormOpen(false);
      setMessage(t("reminders.created"));
      await refresh();
    } catch (requestError) {
      setError(requestError);
    }
  }

  async function onComplete(reminderId: string) {
    await runAction(`${reminderId}:complete`, () => completeVehicleReminder(vehicleId, reminderId));
  }

  async function onCancel(reminderId: string) {
    await runAction(`${reminderId}:cancel`, () => cancelVehicleReminder(vehicleId, reminderId));
  }

  async function runAction(actionKey: string, action: () => Promise<MaintenanceReminder>) {
    setError(null);
    setMessage(null);
    setActionLoading(actionKey);
    try {
      await action();
      await refresh();
    } catch (requestError) {
      setError(requestError);
    } finally {
      setActionLoading(null);
    }
  }

  const activeCount = reminders.filter((reminder) => reminder.status === "ACTIVE").length;
  const overdueCount = reminders.filter((reminder) => reminder.dueState === "OVERDUE").length;
  const dueSoonCount = reminders.filter((reminder) => reminder.dueState === "DUE_SOON").length;

  return (
    <Card>
      <div className="mb-4 flex flex-wrap items-start justify-between gap-3">
        <div className="min-w-0 flex-1 basis-40">
          <div className="flex items-center gap-2">
            <Bell className="h-5 w-5 text-ink" />
            <h3 className="text-lg font-bold text-ink">{t("reminders.title")}</h3>
          </div>
          <p className="mt-1 text-sm text-muted">{t("reminders.emptyDescription")}</p>
        </div>
        {canEdit ? (
          <Button type="button" className="h-9 shrink-0 px-3" onClick={() => setFormOpen((value) => !value)}>
            <Plus className="h-4 w-4" />
            {t("common.add")}
          </Button>
        ) : null}
      </div>

      <div className="mb-4 grid grid-cols-3 gap-2">
        <SummaryPill label={t("reminders.activeCount")} value={activeCount} />
        <SummaryPill label={t("reminders.overdue")} value={overdueCount} />
        <SummaryPill label={t("reminders.dueSoon")} value={dueSoonCount} />
      </div>

      <ErrorMessage
        message={error ? readableApiError(error, language) : null}
        details={error instanceof ApiError ? error.details : []}
      />
      {message ? (
        <div className="mb-4 rounded-2xl border border-emerald-200 bg-emerald-50 px-3 py-2 text-sm text-emerald-700">
          {message}
        </div>
      ) : null}

      {canEdit && formOpen ? (
        <form className="mb-5 rounded-2xl border border-line bg-canvas p-4" onSubmit={handleSubmit(onSubmit)}>
          <h4 className="mb-3 text-sm font-semibold text-ink">{t("reminders.new")}</h4>
          <div className="grid gap-3">
            <Field label={t("label.title")} error={errors.title?.message}>
              <input className={inputClassName()} placeholder={language === "ru" ? "Заменить масло" : "Change oil"} {...register("title")} />
            </Field>
            <Field label={t("label.type")} error={errors.type?.message}>
              <select className={inputClassName()} {...register("type")}>
                {reminderTypeOptions.map((option) => <option key={option.value} value={option.value}>{option.label}</option>)}
              </select>
            </Field>
            <div className="grid gap-3 sm:grid-cols-2">
              <Field label={t("reminders.dueDate")} error={errors.dueDate?.message}>
                <input className={inputClassName()} type="date" {...register("dueDate")} />
              </Field>
              <Field label={t("reminders.dueOdometer")} error={errors.dueOdometerKm?.message}>
                <input className={inputClassName()} inputMode="numeric" placeholder="135000" {...register("dueOdometerKm")} />
              </Field>
            </div>
            <Field label={t("label.description")} hint={t("reminders.formRequirement")} error={errors.description?.message}>
              <textarea className={textareaClassName("min-h-20")} {...register("description")} />
            </Field>
            <div className="flex flex-wrap gap-2">
              <Button type="submit" disabled={isSubmitting}>
                {isSubmitting ? t("reminders.creating") : t("reminders.add")}
              </Button>
              <Button type="button" variant="secondary" onClick={() => setFormOpen(false)}>
                {t("common.cancel")}
              </Button>
            </div>
          </div>
        </form>
      ) : null}

      {loading ? (
        <p className="text-sm text-muted">{t("common.loading")}</p>
      ) : reminders.length === 0 ? (
        <div className="rounded-2xl border border-dashed border-line p-4">
          <h4 className="text-sm font-semibold text-ink">{t("reminders.emptyTitle")}</h4>
          <p className="mt-1 text-sm text-muted">{t("reminders.emptyDescription")}</p>
        </div>
      ) : (
        <div className="space-y-3">
          {reminders.map((reminder) => (
            <ReminderCard
              key={reminder.id}
              reminder={reminder}
              actionLoading={actionLoading}
              onComplete={onComplete}
              onCancel={onCancel}
              canEdit={canEdit}
            />
          ))}
        </div>
      )}
      <PaginationControls page={page} totalPages={totalPages} onPageChange={setPage} />
    </Card>
  );
}

function ReminderCard({
  reminder,
  actionLoading,
  onComplete,
  onCancel,
  canEdit
}: {
  reminder: MaintenanceReminder;
  actionLoading: string | null;
  onComplete: (reminderId: string) => Promise<void>;
  onCancel: (reminderId: string) => Promise<void>;
  canEdit: boolean;
}) {
  const { language, t } = useLanguage();
  const isActive = reminder.status === "ACTIVE";

  return (
    <div className="rounded-2xl border border-line bg-white p-4">
      <div className="flex flex-wrap items-start justify-between gap-3">
        <div className="min-w-0">
          <div className="flex flex-wrap items-center gap-2">
            <Badge className={cn(dueStateStyles[reminder.dueState])}>
              {getEnumLabel(language, "reminderDueState", reminder.dueState)}
            </Badge>
            <Badge>{getEnumLabel(language, "reminderType", reminder.type)}</Badge>
          </div>
          <h4 className="mt-3 break-words text-sm font-semibold text-ink">{reminder.title}</h4>
          {reminder.description ? <p className="mt-1 break-words text-sm leading-5 text-muted">{reminder.description}</p> : null}
        </div>
      </div>

      <div className="mt-3 grid gap-2 text-xs sm:grid-cols-2">
        <Metric icon={<CalendarDays className="h-3.5 w-3.5" />} label={t("reminders.dueDate")} value={formatDate(reminder.dueDate, language)} />
        <Metric icon={<Gauge className="h-3.5 w-3.5" />} label={t("reminders.dueOdometer")} value={formatKm(reminder.dueOdometerKm, language)} />
        <Metric icon={<Gauge className="h-3.5 w-3.5" />} label={t("reminders.latestOdometer")} value={formatKm(reminder.latestOdometerKm, language)} />
        <Metric label={t("label.status")} value={getEnumLabel(language, "reminderStatus", reminder.status)} />
      </div>

      {isActive && canEdit ? (
        <div className="mt-3 flex flex-wrap gap-2">
          <Button
            type="button"
            variant="secondary"
            className="h-9 px-3"
            disabled={actionLoading === `${reminder.id}:complete`}
            onClick={() => void onComplete(reminder.id)}
          >
            <Check className="h-4 w-4" />
            {t("reminders.complete")}
          </Button>
          <Button
            type="button"
            variant="ghost"
            className="h-9 px-3"
            disabled={actionLoading === `${reminder.id}:cancel`}
            onClick={() => void onCancel(reminder.id)}
          >
            <X className="h-4 w-4" />
            {t("reminders.cancel")}
          </Button>
        </div>
      ) : null}
    </div>
  );
}

function SummaryPill({ label, value }: { label: string; value: number }) {
  return (
    <div className="min-w-0 rounded-2xl bg-canvas p-3">
      <div className="text-lg font-bold text-ink">{value}</div>
      <div className="mt-1 break-words text-[11px] leading-4 text-muted">{label}</div>
    </div>
  );
}

function Metric({ icon, label, value }: { icon?: React.ReactNode; label: string; value: string }) {
  return (
    <div className="min-w-0 rounded-2xl bg-canvas p-3">
      <div className="flex items-center gap-1.5 text-[11px] uppercase tracking-wide text-muted">
        {icon}
        {label}
      </div>
      <div className="mt-1 break-words font-semibold text-ink">{value}</div>
    </div>
  );
}

function cleanPayload(values: FormValues): CreateMaintenanceReminderRequest {
  return {
    title: values.title,
    type: values.type,
    dueDate: values.dueDate || undefined,
    dueOdometerKm: values.dueOdometerKm ?? undefined,
    description: values.description || undefined
  };
}
