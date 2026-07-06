import { clearAccessToken, getAccessToken } from "@/lib/auth-token";
import type { ApiErrorBody, ApiErrorDetail } from "@/lib/api/types";

export const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";
const IS_DEVELOPMENT = process.env.NODE_ENV === "development";

type RequestOptions = {
  method?: "GET" | "POST" | "PUT" | "PATCH" | "DELETE";
  body?: unknown;
  formData?: FormData;
  auth?: boolean;
  redirectOnUnauthorized?: boolean;
  headers?: HeadersInit;
};

export class ApiError extends Error {
  status?: number;
  error?: string;
  path?: string;
  details: ApiErrorDetail[];

  constructor(params: {
    message: string;
    status?: number;
    error?: string;
    path?: string;
    details?: ApiErrorDetail[];
  }) {
    super(params.message);
    this.name = "ApiError";
    this.status = params.status;
    this.error = params.error;
    this.path = params.path;
    this.details = params.details ?? [];
  }
}

export async function apiRequest<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const headers = new Headers(options.headers);
  const auth = options.auth ?? true;
  const method = options.method ?? "GET";
  const url = `${API_BASE_URL}${path}`;
  const body = buildBody(options, headers);

  if (auth) {
    const token = getAccessToken();
    if (token) {
      headers.set("Authorization", `Bearer ${token}`);
    }
  }

  let response: Response;
  try {
    response = await fetch(url, {
      method,
      headers,
      body
    });
  } catch (error) {
    if (IS_DEVELOPMENT) {
      console.error("[NETWORK ERROR]", error);
    }
    throw new ApiError({
      message: "Backend is unavailable or API URL is incorrect",
      path
    });
  }

  if (response.status === 204) {
    return undefined as T;
  }

  if (!response.ok) {
    await handleError(response, {
      method,
      path,
      redirectOnUnauthorized: options.redirectOnUnauthorized ?? true,
      url
    });
  }

  const contentType = response.headers.get("content-type") ?? "";
  if (contentType.includes("application/json")) {
    return response.json() as Promise<T>;
  }

  return response.blob() as Promise<T>;
}

function buildBody(options: RequestOptions, headers: Headers) {
  if (options.formData) {
    return options.formData;
  }
  if (options.body === undefined) {
    return undefined;
  }
  headers.set("Content-Type", "application/json");
  return JSON.stringify(options.body);
}

async function handleError(
  response: Response,
  context: { method: string; path: string; redirectOnUnauthorized: boolean; url: string }
): Promise<never> {
  const body = await readErrorBody(response);
  const message = body.message || fallbackMessage(response.status);
  const details = body.details ?? [];
  const apiError = new ApiError({
    message,
    status: response.status,
    error: body.error || response.statusText,
    path: body.path || context.path,
    details
  });

  if (IS_DEVELOPMENT) {
    console.error("[API ERROR]", {
      method: context.method,
      url: context.url,
      status: apiError.status,
      message: apiError.message,
      details: apiError.details
    });
  }

  if (response.status === 401) {
    clearAccessToken();
    if (context.redirectOnUnauthorized && typeof window !== "undefined" && !window.location.pathname.startsWith("/login")) {
      window.location.assign("/login");
    }
  }

  throw apiError;
}

export function readableApiError(error: unknown) {
  if (error instanceof ApiError) {
    if (error.status === undefined) {
      return "Backend недоступен. Проверьте, что API запущен на localhost:8080";
    }
    if (error.status === 403) {
      return "Недостаточно прав";
    }
    if (error.status === 404) {
      return "Не найдено или нет доступа";
    }
    return formatApiErrorMessage(error);
  }
  return "Что-то пошло не так";
}

export function formatApiErrorMessage(error: ApiError) {
  if (error.details.length > 0) {
    return error.details
      .map((detail) => detail.field ? `${detail.field}: ${detail.message}` : detail.message)
      .join(", ");
  }
  return error.message || fallbackMessage(error.status);
}

async function readErrorBody(response: Response): Promise<Partial<ApiErrorBody>> {
  const contentType = response.headers.get("content-type") ?? "";
  if (!contentType.includes("application/json")) {
    return {};
  }

  try {
    const value = await response.json();
    if (!isRecord(value)) {
      return {};
    }
    return {
      status: typeof value.status === "number" ? value.status : undefined,
      error: typeof value.error === "string" ? value.error : undefined,
      message: typeof value.message === "string" ? value.message : undefined,
      path: typeof value.path === "string" ? value.path : undefined,
      details: parseDetails(value.details)
    };
  } catch {
    return {};
  }
}

function parseDetails(value: unknown): ApiErrorDetail[] | undefined {
  if (!Array.isArray(value)) {
    return undefined;
  }

  return value
    .filter(isRecord)
    .map((detail) => ({
      field: typeof detail.field === "string" ? detail.field : undefined,
      message: typeof detail.message === "string" ? detail.message : "Ошибка валидации"
    }));
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === "object" && value !== null;
}

function fallbackMessage(status?: number) {
  if (status === 401) {
    return "Нужна авторизация";
  }
  if (status === 403) {
    return "Недостаточно прав";
  }
  if (status === 404) {
    return "Не найдено или нет доступа";
  }
  return "Ошибка запроса";
}
