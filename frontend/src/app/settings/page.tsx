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
          <div className="flex h-11 w-11 shrink-0 items-center justify-center rounded-xl border border-blue-400/30 bg-blue-500/15 text-neon-cyan">
            <Globe2 className="h-5 w-5" />
          </div>
          <div className="min-w-0 flex-1">
            <h2 className="text-xl font-bold text-white">{t("settings.languageTitle")}</h2>
            <p className="mt-2 text-sm leading-6 text-slate-400">{t("settings.languageDescription")}</p>
            <div className="mt-5 grid gap-3 sm:grid-cols-2">
              {languageOptions.map((option) => {
                const active = language === option.value;
                return (
                  <button
                    key={option.value}
                    type="button"
                    onClick={() => setLanguage(option.value)}
                    className={cn(
                      "flex items-center justify-between rounded-xl border px-4 py-3 text-left text-sm font-semibold transition",
                      active
                        ? "border-blue-400/70 bg-blue-500/15 text-white shadow-glow"
                        : "border-slate-800 bg-surface-900 text-slate-300 hover:border-blue-400/50 hover:text-white"
                    )}
                  >
                    <span>{t(option.labelKey)}</span>
                    {active ? (
                      <Badge className="border-emerald-400/30 bg-emerald-400/10 text-emerald-200">
                        <Check className="h-3.5 w-3.5" />
                      </Badge>
                    ) : null}
                  </button>
                );
              })}
            </div>
            <p className="mt-4 text-xs leading-5 text-slate-500">{t("settings.savedLocally")}</p>
          </div>
        </div>
      </Card>
    </div>
  );
}
