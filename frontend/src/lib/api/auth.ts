import { apiRequest } from "@/lib/api/client";
import type { AuthResponse, UserDto } from "@/lib/api/types";

export type RegisterPayload = {
  email: string;
  password: string;
  displayName?: string;
};

export type LoginPayload = {
  email: string;
  password: string;
};

export function register(payload: RegisterPayload) {
  return apiRequest<AuthResponse>("/api/v1/auth/register", {
    method: "POST",
    body: payload,
    auth: false
  });
}

export function login(payload: LoginPayload) {
  return apiRequest<AuthResponse>("/api/v1/auth/login", {
    method: "POST",
    body: payload,
    auth: false
  });
}

export function me() {
  return apiRequest<UserDto>("/api/v1/auth/me", {
    redirectOnUnauthorized: false
  });
}
