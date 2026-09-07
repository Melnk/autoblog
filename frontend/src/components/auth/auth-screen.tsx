import { BellRing, CarFront, Check, FileText, Wrench } from "lucide-react";
import Link from "next/link";
import type { ReactNode } from "react";
import { ApiError } from "@/lib/api/client";

export function AuthScreen({
  title,
  subtitle,
  children
}: {
  title: string;
  subtitle: string;
  children: ReactNode;
}) {
  return (
    <main className="min-h-screen bg-canvas p-4 text-ink sm:p-6 lg:flex lg:items-center lg:p-8">
      <div className="mx-auto grid w-full max-w-[1240px] gap-4 lg:min-h-[760px] lg:grid-cols-[1.08fr_1fr] lg:gap-6">
        <section className="relative flex flex-col overflow-hidden rounded-[32px] bg-brand-yellow p-5 sm:p-10 lg:p-11">
          <Link href="/" className="inline-flex w-fit items-center gap-3 rounded-lg focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-4 focus-visible:outline-ink" aria-label="AutoBlog — на главную">
            <span className="flex h-9 w-9 items-center justify-center rounded-xl bg-ink text-brand-yellow sm:h-11 sm:w-11 sm:rounded-2xl">
              <CarFront className="h-5 w-5 sm:h-6 sm:w-6" strokeWidth={1.8} />
            </span>
            <span className="text-2xl font-bold tracking-tight">auto<span className="font-medium">blog</span>.</span>
          </Link>

          <div className="mt-4 sm:mt-9 lg:mt-14">
            <p className="mb-3 hidden text-xs font-semibold uppercase tracking-[0.16em] text-ink/65 sm:block">Ваш автомобиль. Его история.</p>
            <h1 className="max-w-lg text-2xl font-bold leading-[1.12] tracking-[-0.04em] sm:text-[44px] lg:text-[48px]">
              <span className="sm:hidden">С заботой о вашем авто.</span>
              <span className="hidden sm:inline">Меньше хлопот.<br />Больше хороших<br className="hidden lg:block" /> поездок.</span>
            </h1>
            <p className="mt-5 hidden max-w-[340px] text-sm leading-6 text-ink/75 sm:block sm:text-base">
              Обслуживание, документы и напоминания — всё под рукой, чтобы спокойно ехать дальше.
            </p>
          </div>

          <div className="relative -mx-3 my-6 hidden flex-1 items-center justify-center sm:flex lg:my-7" aria-hidden="true">
            <CarIllustration />
            <div className="absolute bottom-5 right-0 flex -rotate-3 items-center gap-3 rounded-2xl border border-white/70 bg-white px-4 py-3 shadow-[0_10px_30px_rgba(100,75,0,0.08)] sm:right-3">
              <span className="flex h-9 w-9 items-center justify-center rounded-full bg-[#E8F2D8] text-[#53713E]"><Check className="h-5 w-5" /></span>
              <div>
                <p className="text-xs font-semibold text-ink">Всё важное записано</p>
                <p className="mt-0.5 text-[11px] text-muted">И всегда под рукой</p>
              </div>
            </div>
          </div>

          <div className="mt-7 hidden flex-wrap gap-x-5 gap-y-3 border-t border-ink/10 pt-5 text-xs font-medium text-ink/80 sm:flex lg:mt-auto">
            <span className="inline-flex items-center gap-2"><Wrench className="h-4 w-4" />История ТО</span>
            <span className="inline-flex items-center gap-2"><BellRing className="h-4 w-4" />Напоминания</span>
            <span className="inline-flex items-center gap-2"><FileText className="h-4 w-4" />Документы</span>
          </div>
        </section>

        <section className="flex flex-col justify-center rounded-[32px] border border-line bg-white px-5 py-6 sm:px-10 sm:py-10 lg:px-14 lg:py-12">
          <div className="mx-auto w-full max-w-[384px]">
            <div className="mb-6 sm:mb-8">
              <span className="mb-5 hidden h-12 w-12 items-center justify-center rounded-2xl bg-brand-soft text-ink sm:inline-flex"><CarFront className="h-6 w-6" strokeWidth={1.6} /></span>
              <h2 className="text-[26px] font-bold leading-tight tracking-[-0.035em] sm:text-[34px]">{title}</h2>
              <p className="mt-2 text-sm leading-6 text-muted sm:mt-3">{subtitle}</p>
            </div>
            {children}
          </div>
          <p className="mt-7 text-center text-xs text-muted sm:mt-10">С заботой о вашем автомобиле</p>
        </section>
      </div>
    </main>
  );
}

export function authErrorMessage(error: unknown, mode: "login" | "register") {
  if (!error) {
    return null;
  }
  if (error instanceof ApiError) {
    if (error.status === undefined || error.status >= 500) {
      return "Пока не удаётся связаться с сервисом. Попробуйте ещё раз чуть позже.";
    }
    if (mode === "login" && error.status === 401) {
      return "Не подошла почта или пароль. Проверьте их и попробуйте ещё раз.";
    }
    if (mode === "register" && error.status === 409) {
      return "На эту почту уже зарегистрирован аккаунт. Попробуйте войти.";
    }
    if (error.status === 429) {
      return "Слишком много попыток. Подождите немного и попробуйте снова.";
    }
    if (error.status === 400) {
      return "Проверьте данные в форме и попробуйте ещё раз.";
    }
  }
  return "Не получилось продолжить. Попробуйте ещё раз чуть позже.";
}

function CarIllustration() {
  return (
    <svg viewBox="0 0 480 260" fill="none" className="w-full max-w-[470px]" focusable="false">
      <circle cx="253" cy="117" r="112" fill="white" fillOpacity="0.22" />
      <circle cx="396" cy="67" r="9" fill="white" fillOpacity="0.7" />
      <path d="M82 65V85M72 75H92" stroke="#242424" strokeOpacity="0.55" strokeWidth="3" strokeLinecap="round" />
      <path d="M424 132V142M419 137H429" stroke="white" strokeWidth="3" strokeLinecap="round" />
      <ellipse cx="244" cy="218" rx="164" ry="13" fill="#A88417" fillOpacity="0.12" />
      <path d="M89 209H393" stroke="#242424" strokeOpacity="0.2" strokeWidth="2" strokeLinecap="round" />
      <path d="M151 115L179 78C185 70 191 67 202 67H278C290 67 296 71 304 80L338 120" fill="#363632" stroke="#242424" strokeWidth="3" strokeLinejoin="round" />
      <path d="M173 115L195 85H225V116L173 115Z" fill="#F6F6F2" />
      <path d="M235 85H275C281 85 285 88 289 93L310 117L235 116V85Z" fill="#F6F6F2" />
      <path d="M91 159C94 142 106 129 124 126L166 116L326 119L369 137C382 142 389 152 389 167V182C389 189 384 194 377 194H103C94 194 87 187 87 178L91 159Z" fill="#FAFAF7" stroke="#242424" strokeWidth="3" strokeLinejoin="round" />
      <path d="M96 169H384" stroke="#E8E8E2" strokeWidth="3" />
      <path d="M230 122V170M322 125L326 169" stroke="#242424" strokeOpacity="0.15" strokeWidth="2" />
      <path d="M243 134H258M179 134H194" stroke="#242424" strokeWidth="3" strokeLinecap="round" />
      <path d="M98 146H113C118 146 121 149 121 153V157H94" fill="#FFD337" stroke="#242424" strokeWidth="2" />
      <path d="M364 145H378L385 159H364V145Z" fill="#FFD337" stroke="#242424" strokeWidth="2" />
      <path d="M90 179H111M365 179H388" stroke="#242424" strokeWidth="6" strokeLinecap="round" />
      <circle cx="152" cy="185" r="29" fill="#242424" />
      <circle cx="152" cy="185" r="15" fill="#D6D6CF" />
      <circle cx="152" cy="185" r="6" fill="#FAFAF7" />
      <circle cx="331" cy="185" r="29" fill="#242424" />
      <circle cx="331" cy="185" r="15" fill="#D6D6CF" />
      <circle cx="331" cy="185" r="6" fill="#FAFAF7" />
      <path d="M60 172H74M45 183H71M60 194H75" stroke="#242424" strokeOpacity="0.4" strokeWidth="3" strokeLinecap="round" />
    </svg>
  );
}
