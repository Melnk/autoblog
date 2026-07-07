"use client";

import { Car, LogOut, Plus, Search, Settings, ShieldCheck, Sparkles } from "lucide-react";
import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { useAuth } from "@/components/auth/auth-provider";
import { ButtonLink } from "@/components/ui/button";
import { useLanguage } from "@/lib/i18n";
import { cn } from "@/lib/utils";

const navItems = [
  { href: "/vehicles", labelKey: "nav.vehicles", icon: Car },
  { href: "/vehicles/new", labelKey: "nav.addVehicle", icon: Plus },
  { href: "/settings", labelKey: "nav.settings", icon: Settings }
];

export function AppShell({ children }: { children: React.ReactNode }) {
  const pathname = usePathname();
  const router = useRouter();
  const { user, logout } = useAuth();
  const { t } = useLanguage();

  return (
    <div className="min-h-screen bg-surface-950 text-slate-100">
      <aside className="fixed inset-y-0 left-0 z-30 hidden w-64 border-r border-slate-800 bg-black/30 p-4 backdrop-blur-xl lg:block">
        <Link href="/vehicles" className="mb-8 flex items-center gap-3 px-2 py-4">
          <div className="flex h-10 w-10 items-center justify-center rounded-xl border border-blue-400/30 bg-blue-500/15 text-neon-cyan shadow-glow">
            <Sparkles className="h-5 w-5" />
          </div>
          <span className="text-2xl font-bold text-white">
            Auto<span className="text-neon-cyan">Blog</span>
          </span>
        </Link>

        <nav className="space-y-2">
          {navItems.map((item) => {
            const Icon = item.icon;
            const active = pathname === item.href;
            return (
              <Link
                key={item.href}
                href={item.href}
                className={cn(
                  "flex items-center gap-3 rounded-lg px-4 py-3 text-sm font-semibold text-slate-300 transition",
                  active ? "neon-border bg-blue-500/15 text-white" : "hover:bg-white/5 hover:text-white"
                )}
              >
                <Icon className="h-5 w-5" />
                {t(item.labelKey)}
              </Link>
            );
          })}
        </nav>

        <div className="absolute bottom-4 left-4 right-4 space-y-4">
          <div className="rounded-xl border border-blue-400/20 bg-blue-500/10 p-4">
            <ShieldCheck className="mb-3 h-6 w-6 text-neon-cyan" />
            <p className="text-sm font-semibold text-white">{t("dashboard.trustTitle")}</p>
            <p className="mt-2 text-xs leading-5 text-slate-400">
              {t("dashboard.trustDescription")}
            </p>
          </div>
          <button
            type="button"
            onClick={() => {
              logout();
              router.replace("/login");
            }}
            className="flex w-full items-center justify-between rounded-xl border border-slate-800 bg-surface-900 px-4 py-3 text-left text-sm text-slate-300 transition hover:border-slate-700 hover:text-white"
            title={t("nav.logout")}
          >
            <span>
              <span className="block font-semibold">{user?.displayName || user?.email || t("common.user")}</span>
              <span className="block text-xs text-slate-500">{user?.email}</span>
            </span>
            <LogOut className="h-4 w-4" />
          </button>
        </div>
      </aside>

      <div className="lg:pl-64">
        <header className="sticky top-0 z-20 border-b border-slate-800 bg-surface-950/82 backdrop-blur-xl">
          <div className="flex h-20 items-center gap-4 px-4 sm:px-6 lg:px-8">
            <Link href="/vehicles" className="flex items-center gap-2 text-lg font-bold text-white lg:hidden">
              <Sparkles className="h-5 w-5 text-neon-cyan" />
              Auto<span className="text-neon-cyan">Blog</span>
            </Link>
            <div className="hidden h-12 max-w-2xl flex-1 items-center gap-3 rounded-xl border border-slate-800 bg-surface-900 px-4 text-sm text-slate-500 md:flex">
              <Search className="h-5 w-5" />
              {t("vehicles.searchTopbar")}
            </div>
            <div className="ml-auto flex items-center gap-3">
              <ButtonLink href="/settings" variant="secondary" className="px-3 sm:px-4" aria-label={t("nav.settings")}>
                <Settings className="h-4 w-4" />
                <span className="hidden sm:inline">{t("nav.settings")}</span>
              </ButtonLink>
              <ButtonLink href="/vehicles/new">
                <Plus className="h-4 w-4" />
                {t("nav.addVehicle")}
              </ButtonLink>
            </div>
          </div>
        </header>

        <main className="px-4 py-8 sm:px-6 lg:px-8">
          {children}
        </main>
      </div>
    </div>
  );
}
