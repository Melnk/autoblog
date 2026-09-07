"use client";

import { ArrowUpRight, CarFront, ChevronRight, LogOut, Plus, Settings, ShieldCheck } from "lucide-react";
import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { useAuth } from "@/components/auth/auth-provider";
import { useLanguage } from "@/lib/i18n";
import { cn } from "@/lib/utils";

const navItems = [
  { href: "/vehicles", labelKey: "nav.vehicles", icon: CarFront },
  { href: "/vehicles/new", labelKey: "nav.addVehicle", icon: Plus },
  { href: "/settings", labelKey: "nav.settings", icon: Settings }
];

export function AppShell({ children }: { children: React.ReactNode }) {
  const pathname = usePathname();
  const router = useRouter();
  const { user, logout } = useAuth();
  const { language, t } = useLanguage();
  const currentPage = pathname === "/settings" ? t("nav.settings") : t("nav.vehicles");
  const displayName = user?.displayName || t("common.user");
  const activeItem = (href: string) => href === "/vehicles"
    ? pathname === href || (pathname.startsWith("/vehicles/") && pathname !== "/vehicles/new")
    : pathname === href;

  return (
    <div className="min-h-screen bg-canvas text-ink">
      <a href="#main-content" className="sr-only z-50 rounded-xl bg-brand-yellow px-4 py-3 focus:not-sr-only focus:fixed focus:left-4 focus:top-4">
        {language === "ru" ? "Перейти к содержимому" : "Skip to content"}
      </a>
      <aside className="fixed inset-y-0 left-0 z-30 hidden w-60 flex-col border-r border-line bg-white p-5 lg:flex">
        <Link href="/vehicles" className="mb-10 mt-3 flex items-center gap-2.5 px-2" aria-label="AutoBlog">
          <span className="flex h-10 w-10 items-center justify-center rounded-2xl bg-brand-yellow">
            <CarFront className="h-6 w-6" strokeWidth={1.8} />
          </span>
          <span className="text-[25px] font-bold tracking-tight">auto<span className="font-medium">blog</span><span className="text-[#B68A00]">.</span></span>
        </Link>
        <p className="mb-3 px-3 text-[11px] font-semibold uppercase tracking-[0.16em] text-muted">
          {language === "ru" ? "Моё пространство" : "My space"}
        </p>
        <nav aria-label={language === "ru" ? "Основная навигация" : "Main navigation"} className="space-y-2">
          {navItems.map((item) => {
            const Icon = item.icon;
            const active = activeItem(item.href);
            return (
              <Link key={item.href} href={item.href} aria-current={active ? "page" : undefined}
                className={cn("flex min-h-12 items-center gap-3 rounded-2xl px-4 py-3 text-sm font-semibold transition-colors",
                  active ? "bg-brand-yellow text-ink" : "text-muted hover:bg-canvas hover:text-ink")}>
                <Icon className="h-5 w-5" strokeWidth={1.7} />
                {t(item.labelKey)}
                {active && <ChevronRight className="ml-auto h-4 w-4" />}
              </Link>
            );
          })}
        </nav>
        <div className="mt-auto pt-12">
          <div className="rounded-3xl bg-brand-soft p-5">
            <ShieldCheck className="mb-4 h-7 w-7" strokeWidth={1.5} />
            <p className="text-sm font-bold leading-5">{t("dashboard.trustTitle")}</p>
            <p className="mt-2 text-xs leading-5 text-ink/70">{t("dashboard.trustDescription")}</p>
          </div>
          <p className="px-3 pb-1 pt-5 text-[11px] text-muted">{language === "ru" ? "С заботой о вашем автомобиле" : "A little care for every journey"}</p>
        </div>
      </aside>
      <div className="lg:pl-60">
        <header className="border-b border-line bg-white/80">
          <div className="mx-auto flex h-20 max-w-[1480px] items-center justify-between gap-3 px-5 sm:px-8 lg:px-10">
            <Link href="/vehicles" className="flex items-center gap-2 text-xl font-bold tracking-tight lg:hidden">
              <span className="rounded-xl bg-brand-yellow p-2"><CarFront className="h-5 w-5" /></span>
              autoblog.
            </Link>
            <div className="hidden items-center gap-3 text-sm lg:flex">
              <span className="text-muted">AutoBlog</span><ChevronRight className="h-3.5 w-3.5 text-muted" />
              <span className="font-medium">{currentPage}</span>
            </div>
            <div className="flex min-w-0 items-center gap-3">
              <Link href="/settings" className="flex min-w-0 items-center gap-3 rounded-xl" aria-label={t("nav.settings")}>
                <span className="hidden min-w-0 text-right sm:block">
                  <span className="block max-w-44 truncate text-sm font-semibold">{displayName}</span>
                  <span className="block text-xs text-muted">{language === "ru" ? "Личный кабинет" : "Your account"}</span>
                </span>
                <span className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-brand-soft text-sm font-bold">
                  {(user?.displayName || user?.email || "A").slice(0, 1).toLocaleUpperCase(language)}
                </span>
              </Link>
              <button type="button" aria-label={t("nav.logout")} title={t("nav.logout")}
                onClick={() => { void logout().finally(() => router.replace("/login")); }}
                className="flex h-11 w-11 shrink-0 items-center justify-center rounded-xl text-muted transition hover:bg-canvas hover:text-ink">
                <LogOut className="h-4 w-4" />
              </button>
            </div>
          </div>
        </header>
        <main id="main-content" className="mx-auto max-w-[1480px] px-5 pb-28 pt-8 sm:px-8 lg:px-10 lg:pb-10">
          {children}
          <footer className="mt-12 flex flex-wrap items-center justify-between gap-3 border-t border-line pt-5 text-xs text-muted">
            <span>AutoBlog · {language === "ru" ? "У каждой машины своя история" : "Every car has a story"}</span>
            <Link href="/vehicles/new" className="inline-flex min-h-8 items-center gap-1 hover:text-ink">
              {t("nav.addVehicle")}<ArrowUpRight className="h-3.5 w-3.5" />
            </Link>
          </footer>
        </main>
      </div>
      <nav aria-label={language === "ru" ? "Мобильная навигация" : "Mobile navigation"}
        className="mobile-nav fixed inset-x-0 bottom-0 z-40 grid grid-cols-3 gap-1 border-t border-line bg-white px-3 pt-2 lg:hidden">
        {navItems.map((item) => {
          const Icon = item.icon;
          const active = activeItem(item.href);
          return <Link key={item.href} href={item.href} aria-current={active ? "page" : undefined}
            className={cn("flex min-h-14 flex-col items-center justify-center gap-1 rounded-2xl px-1 py-2 text-[11px] font-semibold",
              active ? "bg-brand-soft text-ink" : "text-muted")}>
            <Icon className="h-5 w-5" strokeWidth={1.7} />{t(item.labelKey)}
          </Link>;
        })}
      </nav>
    </div>
  );
}
