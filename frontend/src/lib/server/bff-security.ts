import "server-only";

import type { NextRequest } from "next/server";
import { isTrustedRequestOrigin } from "@/lib/origin-policy";

export function hasTrustedOrigin(request: NextRequest) {
  const configuredOrigin = process.env.AUTOBLOG_PUBLIC_ORIGIN?.replace(/\/$/, "");
  return isTrustedRequestOrigin(
    request.method,
    request.headers.get("origin"),
    configuredOrigin ?? new URL(request.url).origin
  );
}
