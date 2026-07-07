import type { Language } from "@/lib/i18n";

function localeFor(language: Language) {
  return language === "ru" ? "ru-RU" : "en-US";
}

export function formatDate(value: string | null | undefined, language: Language) {
  if (!value) {
    return "—";
  }
  return new Intl.DateTimeFormat(localeFor(language), {
    year: "numeric",
    month: "short",
    day: "numeric"
  }).format(new Date(value));
}

export function formatMoney(value: number | string | null | undefined, currency = "RUB", language: Language = "ru") {
  if (value === null || value === undefined || value === "") {
    return "—";
  }
  const numericValue = typeof value === "string" ? Number(value) : value;
  return new Intl.NumberFormat(localeFor(language), {
    style: "currency",
    currency,
    currencyDisplay: "symbol",
    maximumFractionDigits: 0
  }).format(numericValue);
}

export function formatKm(value: number | null | undefined, language: Language) {
  if (value === null || value === undefined) {
    return "—";
  }
  const unit = language === "ru" ? "км" : "km";
  return `${new Intl.NumberFormat(localeFor(language)).format(value)} ${unit}`;
}

export function formatFileSize(value: number | null | undefined, language: Language) {
  if (!value) {
    return language === "ru" ? "0 Б" : "0 B";
  }
  const units = language === "ru" ? ["Б", "КБ", "МБ", "ГБ"] : ["B", "KB", "MB", "GB"];
  let size = value;
  let unit = 0;
  while (size >= 1024 && unit < units.length - 1) {
    size /= 1024;
    unit += 1;
  }
  return `${new Intl.NumberFormat(localeFor(language), {
    maximumFractionDigits: unit === 0 ? 0 : 1
  }).format(size)} ${units[unit]}`;
}

export function shortHash(value: string | null | undefined) {
  if (!value) {
    return "—";
  }
  return `${value.slice(0, 10)}…${value.slice(-6)}`;
}
