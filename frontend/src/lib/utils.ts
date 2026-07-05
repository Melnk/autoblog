import { clsx, type ClassValue } from "clsx";
import { twMerge } from "tailwind-merge";

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

export function formatDate(value?: string | null) {
  if (!value) {
    return "—";
  }
  return new Intl.DateTimeFormat("ru-RU", {
    year: "numeric",
    month: "short",
    day: "numeric"
  }).format(new Date(value));
}

export function formatMoney(value?: number | string | null, currency = "RUB") {
  if (value === null || value === undefined || value === "") {
    return "—";
  }
  const numericValue = typeof value === "string" ? Number(value) : value;
  return new Intl.NumberFormat("ru-RU", {
    style: "currency",
    currency,
    maximumFractionDigits: 0
  }).format(numericValue);
}

export function formatBytes(value?: number | null) {
  if (!value) {
    return "0 Б";
  }
  const units = ["Б", "КБ", "МБ", "ГБ"];
  let size = value;
  let unit = 0;
  while (size >= 1024 && unit < units.length - 1) {
    size /= 1024;
    unit += 1;
  }
  return `${size.toFixed(unit === 0 ? 0 : 1)} ${units[unit]}`;
}

export function shortHash(value?: string | null) {
  if (!value) {
    return "—";
  }
  return `${value.slice(0, 10)}…${value.slice(-6)}`;
}
