import "server-only";

import type { NextResponse } from "next/server";

export const AUTH_COOKIE_NAME = "autoblog.session";

export function setAuthCookie(response: NextResponse, token: string, maxAgeSeconds: number) {
  response.cookies.set({
    name: AUTH_COOKIE_NAME,
    value: token,
    httpOnly: true,
    maxAge: maxAgeSeconds,
    path: "/",
    sameSite: "strict",
    secure: process.env.NODE_ENV === "production"
  });
}

export function clearAuthCookie(response: NextResponse) {
  response.cookies.set({
    name: AUTH_COOKIE_NAME,
    value: "",
    httpOnly: true,
    maxAge: 0,
    path: "/",
    sameSite: "strict",
    secure: process.env.NODE_ENV === "production"
  });
}
