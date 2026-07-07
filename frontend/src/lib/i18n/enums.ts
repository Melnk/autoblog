import type {
  AttachmentType,
  AttachmentVisibility,
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
  | "userAccountStatus";

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
