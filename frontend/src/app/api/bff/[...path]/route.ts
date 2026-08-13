import { NextRequest, NextResponse } from "next/server";
import { AUTH_COOKIE_NAME, clearAuthCookie, setAuthCookie } from "@/lib/server/auth-cookie";
import { hasTrustedOrigin } from "@/lib/server/bff-security";

const BACKEND_URL = process.env.AUTOBLOG_BACKEND_URL ?? "http://localhost:8080";
const AUTH_ENDPOINTS = new Set(["api/v1/auth/login", "api/v1/auth/register"]);
const FORWARDED_RESPONSE_HEADERS = ["content-disposition", "content-length", "content-type", "location"];

type RouteContext = { params: { path: string[] } };

export function GET(request: NextRequest, context: RouteContext) {
  return proxy(request, context.params.path);
}

export function POST(request: NextRequest, context: RouteContext) {
  return proxy(request, context.params.path);
}

export function PUT(request: NextRequest, context: RouteContext) {
  return proxy(request, context.params.path);
}

export function PATCH(request: NextRequest, context: RouteContext) {
  return proxy(request, context.params.path);
}

export function DELETE(request: NextRequest, context: RouteContext) {
  return proxy(request, context.params.path);
}

async function proxy(request: NextRequest, pathSegments: string[]) {
  if (!hasTrustedOrigin(request)) {
    return NextResponse.json({ message: "Request origin is not allowed" }, { status: 403 });
  }

  const backendPath = pathSegments.join("/");
  if (!isAllowedBackendPath(backendPath)) {
    return NextResponse.json({ message: "Backend path is not allowed" }, { status: 404 });
  }

  try {
    const upstream = await fetch(buildUpstreamUrl(request, pathSegments), {
      method: request.method,
      headers: buildUpstreamHeaders(request),
      body: await readRequestBody(request),
      cache: "no-store",
      redirect: "manual"
    });

    if (AUTH_ENDPOINTS.has(backendPath) && upstream.ok) {
      return createAuthenticatedResponse(upstream);
    }

    const response = new NextResponse(upstream.body, {
      status: upstream.status,
      headers: forwardedResponseHeaders(upstream.headers)
    });
    if (upstream.status === 401) {
      clearAuthCookie(response);
    }
    return response;
  } catch {
    return NextResponse.json(
      { message: "Backend is unavailable", path: `/${backendPath}` },
      { status: 502 }
    );
  }
}

function isAllowedBackendPath(path: string) {
  return path.startsWith("api/v1/") || path.startsWith("api/v2/");
}

function buildUpstreamUrl(request: NextRequest, pathSegments: string[]) {
  const path = pathSegments.map(encodeURIComponent).join("/");
  return `${BACKEND_URL.replace(/\/$/, "")}/${path}${request.nextUrl.search}`;
}

function buildUpstreamHeaders(request: NextRequest) {
  const headers = new Headers();
  for (const name of ["accept", "content-type", "x-request-id"]) {
    const value = request.headers.get(name);
    if (value) {
      headers.set(name, value);
    }
  }

  const token = request.cookies.get(AUTH_COOKIE_NAME)?.value;
  if (token) {
    headers.set("authorization", `Bearer ${token}`);
  }
  return headers;
}

async function readRequestBody(request: NextRequest) {
  if (request.method === "GET" || request.method === "HEAD") {
    return undefined;
  }
  const body = await request.arrayBuffer();
  return body.byteLength === 0 ? undefined : body;
}

async function createAuthenticatedResponse(upstream: Response) {
  const body = await upstream.json() as {
    accessToken: string;
    expiresInSeconds: number;
    user: unknown;
  };
  const response = NextResponse.json({
    expiresInSeconds: body.expiresInSeconds,
    user: body.user
  }, { status: upstream.status });
  setAuthCookie(response, body.accessToken, body.expiresInSeconds);
  return response;
}

function forwardedResponseHeaders(upstreamHeaders: Headers) {
  const headers = new Headers();
  for (const name of FORWARDED_RESPONSE_HEADERS) {
    const value = upstreamHeaders.get(name);
    if (value) {
      headers.set(name, value);
    }
  }
  return headers;
}
