"use client";

import { Shield, Trash2, UserPlus } from "lucide-react";
import { useCallback, useEffect, useState } from "react";
import { useAuth } from "@/components/auth/auth-provider";
import { RoleBadge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { ErrorMessage } from "@/components/ui/error-message";
import { Field, inputClassName } from "@/components/ui/form";
import { PaginationControls } from "@/components/ui/pagination-controls";
import { readableApiError } from "@/lib/api/client";
import type { VehicleAccessDto, VehicleAccessRole } from "@/lib/api/types";
import {
  grantVehicleAccess,
  listVehicleAccess,
  revokeVehicleAccess
} from "@/lib/api/vehicles";
import { getEnumLabel, useLanguage } from "@/lib/i18n";

type GrantableRole = Exclude<VehicleAccessRole, "OWNER">;

export function AccessManagementPanel({ vehicleId }: { vehicleId: string }) {
  const { user } = useAuth();
  const { language, t } = useLanguage();
  const [entries, setEntries] = useState<VehicleAccessDto[]>([]);
  const [email, setEmail] = useState("");
  const [role, setRole] = useState<GrantableRole>("VIEWER");
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const refresh = useCallback(async () => {
    const response = await listVehicleAccess(vehicleId, page);
    setEntries(response.items);
    setTotalPages(response.totalPages);
  }, [page, vehicleId]);

  useEffect(() => {
    async function load() {
      setLoading(true);
      setError(null);
      try {
        await refresh();
      } catch (requestError) {
        setError(readableApiError(requestError, language));
      } finally {
        setLoading(false);
      }
    }
    void load();
  }, [language, refresh]);

  async function grant() {
    if (!email.trim()) {
      setError(t("access.emailRequired"));
      return;
    }
    await mutate(async () => {
      await grantVehicleAccess(vehicleId, email.trim(), role);
      setEmail("");
    });
  }

  async function revoke(entry: VehicleAccessDto) {
    await mutate(() => revokeVehicleAccess(vehicleId, entry.userId));
  }

  async function mutate(action: () => Promise<unknown>) {
    setSaving(true);
    setError(null);
    try {
      await action();
      await refresh();
    } catch (requestError) {
      setError(readableApiError(requestError, language));
    } finally {
      setSaving(false);
    }
  }

  return (
    <Card>
      <div className="mb-4 flex items-center gap-2">
        <Shield className="h-5 w-5 text-ink" />
        <h3 className="text-lg font-bold text-ink">{t("access.title")}</h3>
      </div>
      <p className="mb-4 text-sm text-muted">{t("access.description")}</p>
      <ErrorMessage message={error} />

      <div className="grid min-w-0 gap-3">
        <Field label="Email">
          <input
            className={inputClassName()}
            type="email"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
          />
        </Field>
        <Field label={t("access.role")}>
          <select
            className={inputClassName()}
            value={role}
            onChange={(event) => setRole(event.target.value as GrantableRole)}
          >
            <option value="EDITOR">{getEnumLabel(language, "vehicleAccessRole", "EDITOR")}</option>
            <option value="VIEWER">{getEnumLabel(language, "vehicleAccessRole", "VIEWER")}</option>
          </select>
        </Field>
        <Button type="button" className="self-end" disabled={saving} onClick={() => void grant()}>
          <UserPlus className="h-4 w-4" />
          {t("access.grant")}
        </Button>
      </div>

      {loading ? (
        <p className="mt-4 text-sm text-muted">{t("common.loading")}</p>
      ) : (
        <div className="mt-4 space-y-2">
          {entries.map((entry) => (
            <div
              key={entry.id}
              className="flex flex-wrap items-center justify-between gap-3 rounded-2xl border border-line p-3"
            >
              <div className="min-w-0">
                <div className="break-all text-sm font-semibold text-ink">{entry.email}</div>
                <div className="mt-1"><RoleBadge role={entry.role} /></div>
              </div>
              {entry.userId !== user?.id && entry.role !== "OWNER" ? (
                <Button
                  type="button"
                  variant="ghost"
                  className="h-9 px-3 text-red-700"
                  disabled={saving}
                  onClick={() => void revoke(entry)}
                >
                  <Trash2 className="h-4 w-4" />
                  {t("access.revoke")}
                </Button>
              ) : null}
            </div>
          ))}
        </div>
      )}
      <PaginationControls page={page} totalPages={totalPages} onPageChange={setPage} />
    </Card>
  );
}
