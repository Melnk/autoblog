import type {
  AttachmentType,
  AttachmentVisibility,
  ReminderDueState,
  ReminderStatus,
  ReminderType,
  TrustScoreLevel,
  TrustSignalImpact,
  VehicleAccessRole,
  VehicleEventType
} from "@/lib/api/types";
import type { Language } from "@/lib/i18n/translations";

export type PublicReportStatus = "ACTIVE" | "DISABLED";
export type UserAccountStatus = "ACTIVE" | "DISABLED";

type EnumGroup =
  | "vehicleEventType"
  | "attachmentType"
  | "attachmentVisibility"
  | "attachmentVisibilityDescription"
  | "vehicleAccessRole"
  | "publicReportStatus"
  | "userAccountStatus"
  | "reminderType"
  | "reminderStatus"
  | "reminderDueState"
  | "trustScoreLevel"
  | "trustSignalImpact";

export const VEHICLE_EVENT_TYPE_OPTIONS = [
  "MAINTENANCE",
  "REPAIR",
  "DIAGNOSTIC",
  "ACCIDENT",
  "INSPECTION",
  "ODOMETER",
  "FUEL",
  "DOCUMENT",
  "OTHER"
] as const satisfies readonly VehicleEventType[];

export const ATTACHMENT_TYPE_OPTIONS = [
  "PHOTO",
  "RECEIPT",
  "WORK_ORDER",
  "DIAGNOSTIC_REPORT",
  "PART_PHOTO",
  "OTHER"
] as const satisfies readonly AttachmentType[];

export const ATTACHMENT_VISIBILITY_OPTIONS = ["PRIVATE", "PUBLIC"] as const satisfies readonly AttachmentVisibility[];
export const VEHICLE_ACCESS_ROLE_OPTIONS = ["OWNER", "EDITOR", "VIEWER"] as const satisfies readonly VehicleAccessRole[];
export const REMINDER_TYPE_OPTIONS = [
  "OIL_CHANGE",
  "INSURANCE",
  "INSPECTION",
  "TIRE_SERVICE",
  "BRAKE_SERVICE",
  "DIAGNOSTIC",
  "CUSTOM"
] as const satisfies readonly ReminderType[];
export const REMINDER_STATUS_OPTIONS = ["ACTIVE", "COMPLETED", "CANCELLED"] as const satisfies readonly ReminderStatus[];
export const REMINDER_DUE_STATE_OPTIONS = [
  "OVERDUE",
  "DUE_SOON",
  "UPCOMING",
  "COMPLETED",
  "CANCELLED"
] as const satisfies readonly ReminderDueState[];
export const TRUST_SCORE_LEVEL_OPTIONS = ["HIGH", "MEDIUM", "LOW", "UNKNOWN"] as const satisfies readonly TrustScoreLevel[];
export const TRUST_SIGNAL_IMPACT_OPTIONS = ["POSITIVE", "NEGATIVE", "NEUTRAL"] as const satisfies readonly TrustSignalImpact[];

const enumLabels: Record<Language, Record<EnumGroup, Record<string, string>>> = {
  ru: {
    vehicleEventType: {
      MAINTENANCE: "Обслуживание",
      REPAIR: "Ремонт",
      DIAGNOSTIC: "Диагностика",
      ACCIDENT: "ДТП",
      INSPECTION: "Осмотр",
      ODOMETER: "Пробег",
      FUEL: "Заправка",
      DOCUMENT: "Документ",
      OTHER: "Другое"
    },
    attachmentType: {
      PHOTO: "Фото",
      RECEIPT: "Чек",
      WORK_ORDER: "Заказ-наряд",
      DIAGNOSTIC_REPORT: "Диагностический отчет",
      PART_PHOTO: "Фото детали",
      OTHER: "Другое"
    },
    attachmentVisibility: {
      PRIVATE: "Приватное",
      PUBLIC: "Публичное"
    },
    attachmentVisibilityDescription: {
      PRIVATE: "Не попадет в публичный отчет",
      PUBLIC: "Будет видно покупателю в публичном отчете"
    },
    vehicleAccessRole: {
      OWNER: "Владелец",
      EDITOR: "Редактор",
      VIEWER: "Просмотр"
    },
    publicReportStatus: {
      ACTIVE: "Активен",
      DISABLED: "Отключен"
    },
    userAccountStatus: {
      ACTIVE: "Активен",
      DISABLED: "Отключен"
    },
    reminderType: {
      OIL_CHANGE: "Замена масла",
      INSURANCE: "Страховка",
      INSPECTION: "Осмотр / техосмотр",
      TIRE_SERVICE: "Шины",
      BRAKE_SERVICE: "Тормоза",
      DIAGNOSTIC: "Диагностика",
      CUSTOM: "Другое"
    },
    reminderStatus: {
      ACTIVE: "Активно",
      COMPLETED: "Выполнено",
      CANCELLED: "Отменено"
    },
    reminderDueState: {
      OVERDUE: "Просрочено",
      DUE_SOON: "Скоро",
      UPCOMING: "Запланировано",
      COMPLETED: "Выполнено",
      CANCELLED: "Отменено"
    },
    trustScoreLevel: {
      HIGH: "Высокий",
      MEDIUM: "Средний",
      LOW: "Низкий",
      UNKNOWN: "Недостаточно данных"
    },
    trustSignalImpact: {
      POSITIVE: "Плюс",
      NEGATIVE: "Риск",
      NEUTRAL: "Инфо"
    }
  },
  en: {
    vehicleEventType: {
      MAINTENANCE: "Maintenance",
      REPAIR: "Repair",
      DIAGNOSTIC: "Diagnostic",
      ACCIDENT: "Accident",
      INSPECTION: "Inspection",
      ODOMETER: "Odometer",
      FUEL: "Fuel",
      DOCUMENT: "Document",
      OTHER: "Other"
    },
    attachmentType: {
      PHOTO: "Photo",
      RECEIPT: "Receipt",
      WORK_ORDER: "Work order",
      DIAGNOSTIC_REPORT: "Diagnostic report",
      PART_PHOTO: "Part photo",
      OTHER: "Other"
    },
    attachmentVisibility: {
      PRIVATE: "Private",
      PUBLIC: "Public"
    },
    attachmentVisibilityDescription: {
      PRIVATE: "Will not appear in the public report",
      PUBLIC: "Will be visible to a buyer in the public report"
    },
    vehicleAccessRole: {
      OWNER: "Owner",
      EDITOR: "Editor",
      VIEWER: "Viewer"
    },
    publicReportStatus: {
      ACTIVE: "Active",
      DISABLED: "Disabled"
    },
    userAccountStatus: {
      ACTIVE: "Active",
      DISABLED: "Disabled"
    },
    reminderType: {
      OIL_CHANGE: "Oil change",
      INSURANCE: "Insurance",
      INSPECTION: "Inspection",
      TIRE_SERVICE: "Tire service",
      BRAKE_SERVICE: "Brake service",
      DIAGNOSTIC: "Diagnostic",
      CUSTOM: "Custom"
    },
    reminderStatus: {
      ACTIVE: "Active",
      COMPLETED: "Completed",
      CANCELLED: "Cancelled"
    },
    reminderDueState: {
      OVERDUE: "Overdue",
      DUE_SOON: "Due soon",
      UPCOMING: "Upcoming",
      COMPLETED: "Completed",
      CANCELLED: "Cancelled"
    },
    trustScoreLevel: {
      HIGH: "High",
      MEDIUM: "Medium",
      LOW: "Low",
      UNKNOWN: "Not enough data"
    },
    trustSignalImpact: {
      POSITIVE: "Positive",
      NEGATIVE: "Risk",
      NEUTRAL: "Info"
    }
  }
};

export function getEnumLabel(language: Language, group: EnumGroup, value?: string | null) {
  if (!value) {
    return "—";
  }
  return enumLabels[language]?.[group]?.[value] ?? value;
}

export function getVehicleEventTypeOptions(language: Language) {
  return VEHICLE_EVENT_TYPE_OPTIONS.map((value) => ({
    value,
    label: getEnumLabel(language, "vehicleEventType", value)
  }));
}

export function getAttachmentTypeOptions(language: Language) {
  return ATTACHMENT_TYPE_OPTIONS.map((value) => ({
    value,
    label: getEnumLabel(language, "attachmentType", value)
  }));
}

export function getAttachmentVisibilityOptions(language: Language) {
  return ATTACHMENT_VISIBILITY_OPTIONS.map((value) => ({
    value,
    label: getEnumLabel(language, "attachmentVisibility", value),
    description: getEnumLabel(language, "attachmentVisibilityDescription", value)
  }));
}

export function getReminderTypeOptions(language: Language) {
  return REMINDER_TYPE_OPTIONS.map((value) => ({
    value,
    label: getEnumLabel(language, "reminderType", value)
  }));
}
