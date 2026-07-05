export type VehicleAccessRole = "OWNER" | "EDITOR" | "VIEWER";

export type AuthResponse = {
  accessToken: string;
  tokenType: "Bearer";
  expiresInSeconds: number;
  user: UserDto;
};

export type UserDto = {
  id: string;
  email: string;
  displayName?: string | null;
};

export type VehicleDto = {
  id: string;
  vin: string;
  make?: string | null;
  model?: string | null;
  generation?: string | null;
  year?: number | null;
  engine?: string | null;
  transmission?: string | null;
  trim?: string | null;
  market: string;
  createdAt: string;
  updatedAt: string;
  role?: VehicleAccessRole;
};

export type VehicleEventType =
  | "MAINTENANCE"
  | "REPAIR"
  | "DIAGNOSTIC"
  | "ACCIDENT"
  | "INSPECTION"
  | "ODOMETER"
  | "FUEL"
  | "DOCUMENT"
  | "OTHER";

export type VehicleEventDto = {
  id: string;
  vehicleId: string;
  sequenceNumber: number;
  type: VehicleEventType;
  eventDate: string;
  odometerKm?: number | null;
  title: string;
  description?: string | null;
  costAmount?: number | null;
  costCurrency: string;
  serviceName?: string | null;
  payload?: unknown;
  previousEventHash?: string | null;
  eventHash: string;
  createdAt: string;
};

export type AttachmentType =
  | "PHOTO"
  | "RECEIPT"
  | "WORK_ORDER"
  | "DIAGNOSTIC_REPORT"
  | "PART_PHOTO"
  | "OTHER";

export type AttachmentVisibility = "PRIVATE" | "PUBLIC";

export type EventAttachmentDto = {
  id: string;
  vehicleId: string;
  eventId: string;
  type: AttachmentType;
  visibility: AttachmentVisibility;
  originalFilename: string;
  contentType: string;
  sizeBytes: number;
  checksumSha256: string;
  description?: string | null;
  createdAt: string;
};

export type PublicReportMetadataDto = {
  id: string;
  vehicleId: string;
  publicToken: string;
  publicUrl: string;
  status: "ACTIVE" | "DISABLED";
  createdAt: string;
  updatedAt: string;
};

export type PublicReportDto = {
  report: {
    publicToken: string;
    status: "ACTIVE" | "DISABLED";
    createdAt: string;
    updatedAt: string;
  };
  vehicle: Omit<VehicleDto, "id" | "createdAt" | "updatedAt" | "role">;
  summary: PublicReportSummaryDto;
  events: PublicReportEventDto[];
};

export type PublicReportSummaryDto = {
  eventsCount: number;
  firstEventDate?: string | null;
  lastEventDate?: string | null;
  latestOdometerKm?: number | null;
  totalKnownCostAmount: number;
  costCurrency: string;
  hashChainValid: boolean;
};

export type PublicReportEventDto = Omit<VehicleEventDto, "id" | "vehicleId" | "createdAt"> & {
  attachments: PublicAttachmentDto[];
};

export type PublicAttachmentDto = {
  id: string;
  type: AttachmentType;
  originalFilename: string;
  contentType: string;
  sizeBytes: number;
  checksumSha256: string;
  description?: string | null;
  downloadUrl: string;
};

export type VehicleAccessDto = {
  id: string;
  vehicleId: string;
  userId: string;
  email: string;
  role: VehicleAccessRole;
  createdAt: string;
};

export type ApiErrorDetail = {
  field?: string;
  message: string;
};

export type ApiErrorBody = {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
  details?: ApiErrorDetail[];
};
