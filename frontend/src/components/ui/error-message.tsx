import type { ApiErrorDetail } from "@/lib/api/types";

type ErrorDebugInfo = {
  status?: number;
  backendMessage?: string;
  path?: string;
};

export function ErrorMessage({
  message,
  details = [],
  debug
}: {
  message?: string | null;
  details?: ApiErrorDetail[];
  debug?: ErrorDebugInfo;
}) {
  if (!message) {
    return null;
  }
  return (
    <div className="rounded-lg border border-red-500/30 bg-red-500/10 px-4 py-3 text-sm text-red-100">
      <div>{message}</div>
      {details.length > 0 ? (
        <ul className="mt-2 list-disc space-y-1 pl-5 text-red-100/90">
          {details.map((detail, index) => (
            <li key={`${detail.field ?? "detail"}-${index}`}>
              {detail.field ? `${detail.field}: ` : ""}{detail.message}
            </li>
          ))}
        </ul>
      ) : null}
      {process.env.NODE_ENV === "development" && debug ? (
        <div className="mt-3 rounded-md border border-red-300/20 bg-black/20 p-3 font-mono text-xs text-red-100/80">
          {debug.status ? <div>HTTP {debug.status}</div> : null}
          {debug.backendMessage ? <div>{debug.backendMessage}</div> : null}
          {debug.path ? <div>{debug.path}</div> : null}
        </div>
      ) : null}
    </div>
  );
}
