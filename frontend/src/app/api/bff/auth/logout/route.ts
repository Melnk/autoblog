import { NextRequest, NextResponse } from "next/server";
import { clearAuthCookie } from "@/lib/server/auth-cookie";
import { hasTrustedOrigin } from "@/lib/server/bff-security";

export async function POST(request: NextRequest) {
  if (!hasTrustedOrigin(request)) {
    return NextResponse.json({ message: "Request origin is not allowed" }, { status: 403 });
  }

  const response = new NextResponse(null, { status: 204 });
  clearAuthCookie(response);
  return response;
}
