import type { TrustScoreLevel } from "@/lib/api/types";
import type { Language } from "@/lib/i18n/translations";

const trustScoreSummaries: Record<Language, Record<TrustScoreLevel, string>> = {
  ru: {
    HIGH: "История автомобиля выглядит хорошо подтвержденной.",
    MEDIUM: "История автомобиля частично подтверждена, но есть пробелы.",
    LOW: "История автомобиля требует дополнительной проверки.",
    UNKNOWN: "Недостаточно данных для оценки истории автомобиля."
  },
  en: {
    HIGH: "Vehicle history looks well verified.",
    MEDIUM: "Vehicle history is partially verified, but has gaps.",
    LOW: "Vehicle history requires additional review.",
    UNKNOWN: "Not enough data to score this vehicle history."
  }
};

const signalLabels: Record<Language, Record<string, string>> = {
  ru: {
    BASE_SCORE: "Базовая оценка",
    NO_EVENTS: "Нет событий истории",
    HASH_CHAIN_VALID: "Цепочка hash подтверждена",
    HASH_CHAIN_INVALID: "Цепочка hash нарушена",
    HAS_3_EVENTS: "Есть минимум 3 события",
    HAS_5_EVENTS: "Есть минимум 5 событий",
    HAS_EVENT_ATTACHMENTS: "Есть доказательства",
    HAS_PUBLIC_ATTACHMENTS: "Есть публичные доказательства",
    NO_ATTACHMENTS: "Нет вложений-доказательств",
    ODOMETER_CONSISTENT: "Пробег выглядит последовательным",
    ODOMETER_INCONSISTENCY: "Есть признаки проблемы с пробегом",
    NO_ODOMETER_DATA: "Нет данных о пробеге",
    HAS_RECENT_EVENT: "Есть недавнее событие",
    LAST_EVENT_OLD: "История давно не обновлялась",
    HAS_REMINDERS: "Настроены напоминания",
    HAS_OVERDUE_REMINDERS: "Есть просроченные напоминания"
  },
  en: {
    BASE_SCORE: "Base score",
    NO_EVENTS: "No history events",
    HASH_CHAIN_VALID: "Hash chain verified",
    HASH_CHAIN_INVALID: "Hash chain is broken",
    HAS_3_EVENTS: "At least 3 events",
    HAS_5_EVENTS: "At least 5 events",
    HAS_EVENT_ATTACHMENTS: "Evidence is attached",
    HAS_PUBLIC_ATTACHMENTS: "Public evidence is available",
    NO_ATTACHMENTS: "No evidence attachments",
    ODOMETER_CONSISTENT: "Odometer looks consistent",
    ODOMETER_INCONSISTENCY: "Possible odometer issue detected",
    NO_ODOMETER_DATA: "No odometer data",
    HAS_RECENT_EVENT: "Recent event is present",
    LAST_EVENT_OLD: "History has not been updated recently",
    HAS_REMINDERS: "Reminders are configured",
    HAS_OVERDUE_REMINDERS: "Overdue reminders exist"
  }
};

const signalMessages: Record<Language, Record<string, string>> = {
  ru: {
    BASE_SCORE: "Каждая оценка начинается с нейтральной базы.",
    NO_EVENTS: "Для автомобиля пока нет записей истории.",
    HASH_CHAIN_VALID: "События связаны проверяемой цепочкой hash.",
    HASH_CHAIN_INVALID: "Цепочка событий не проходит проверку.",
    HAS_3_EVENTS: "История содержит несколько подтвержденных записей.",
    HAS_5_EVENTS: "История достаточно насыщена событиями.",
    HAS_EVENT_ATTACHMENTS: "К событиям приложены файлы-доказательства.",
    HAS_PUBLIC_ATTACHMENTS: "В публичном отчете есть доказательства для покупателя.",
    NO_ATTACHMENTS: "Файлы-доказательства пока не приложены.",
    ODOMETER_CONSISTENT: "Показания пробега не уменьшаются по timeline.",
    ODOMETER_INCONSISTENCY: "В timeline найдено уменьшение пробега.",
    NO_ODOMETER_DATA: "В событиях нет показаний пробега.",
    HAS_RECENT_EVENT: "История обновлялась за последние 12 месяцев.",
    LAST_EVENT_OLD: "Последнее событие старше 24 месяцев.",
    HAS_REMINDERS: "Запланированы будущие сервисные действия.",
    HAS_OVERDUE_REMINDERS: "Есть обслуживание, которое уже просрочено."
  },
  en: {
    BASE_SCORE: "Every score starts from a neutral base.",
    NO_EVENTS: "No history records have been added yet.",
    HASH_CHAIN_VALID: "Events are linked by a verifiable hash chain.",
    HASH_CHAIN_INVALID: "The event chain does not pass verification.",
    HAS_3_EVENTS: "The history contains several records.",
    HAS_5_EVENTS: "The history has a stronger event trail.",
    HAS_EVENT_ATTACHMENTS: "Events include evidence files.",
    HAS_PUBLIC_ATTACHMENTS: "Buyer-visible evidence is available.",
    NO_ATTACHMENTS: "No evidence files have been attached yet.",
    ODOMETER_CONSISTENT: "Odometer readings do not decrease in the timeline.",
    ODOMETER_INCONSISTENCY: "A decreasing odometer reading was found.",
    NO_ODOMETER_DATA: "Events do not include odometer readings.",
    HAS_RECENT_EVENT: "The history was updated within the last 12 months.",
    LAST_EVENT_OLD: "The last event is older than 24 months.",
    HAS_REMINDERS: "Future service actions are planned.",
    HAS_OVERDUE_REMINDERS: "Some planned service is overdue."
  }
};

export function getTrustScoreSummary(language: Language, level: TrustScoreLevel, fallback?: string) {
  return trustScoreSummaries[language]?.[level] ?? fallback ?? level;
}

export function getTrustSignalLabel(language: Language, code: string) {
  return signalLabels[language]?.[code] ?? code;
}

export function getTrustSignalMessage(language: Language, code: string, fallback?: string) {
  return signalMessages[language]?.[code] ?? fallback ?? code;
}
