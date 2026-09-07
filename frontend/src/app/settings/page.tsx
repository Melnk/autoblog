"use client";

import { Check, Globe2 } from "lucide-react";
import { ProtectedRoute } from "@/components/auth/protected-route";
import { AppShell } from "@/components/layout/app-shell";
import { Badge } from "@/components/ui/badge";
import { Card, SectionHeader } from "@/components/ui/card";
import { cn } from "@/lib/utils";
import { useLanguage, type Language } from "@/lib/i18n";

const languageOptions: Array<{ value: Language; labelKey: string }> = [
  { value: "ru", labelKey: "settings.russian" },
  { value: "en", labelKey: "settings.english" }
];

export default function SettingsPage() {
  return (
    <ProtectedRoute>
      <AppShell>
        <SettingsContent />
      </AppShell>
    </ProtectedRoute>
  );
}

function SettingsContent() {
  const { language, setLanguage, t } = useLanguage();

  return (
    <div>
      <SectionHeader title={t("settings.title")} />
      <Card className="max-w-3xl">
        <div className="flex items-start gap-4">
          <div className="flex h-11 w-11 shrink-0 items-center justify-center rounded-2xl border border-brand-yellow bg-brand-soft text-ink">
            <Globe2 className="h-5 w-5" />
          </div>
          <div className="min-w-0 flex-1">
            <h2 className="text-xl font-bold text-ink">{t("settings.languageTitle")}</h2>
            <p className="mt-2 text-sm leading-6 text-muted">{t("settings.languageDescription")}</p>
            <div className="mt-5 grid gap-3 sm:grid-cols-2">
              {languageOptions.map((option) => {
                const active = language === option.value;
                return (
                  <button
                    key={option.value}
                    type="button"
                    aria-pressed={active}
                    onClick={() => setLanguage(option.value)}
                    className={cn(
                      "flex items-center justify-between gap-3 rounded-2xl border px-4 py-4 text-left text-sm font-semibold transition focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-4 focus-visible:outline-ink",
                      active
                        ? "border-brand-yellow bg-brand-soft text-ink"
                        : "border-line bg-white text-ink hover:border-brand-yellow hover:bg-canvas"
                    )}
                  >
                    <span>{t(option.labelKey)}</span>
                    {active ? (
                      <Badge className="border-emerald-200 bg-emerald-50 text-emerald-700">
                        <Check className="h-3.5 w-3.5" />
                      </Badge>
                    ) : null}
                  </button>
                );
              })}
            </div>
            <p className="mt-4 text-xs leading-5 text-muted">{t("settings.savedLocally")}</p>
          </div>
        </div>
      </Card>
    </div>
  );
}
