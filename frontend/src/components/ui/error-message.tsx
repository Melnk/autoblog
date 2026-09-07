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
    <div role="alert" className="rounded-2xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-800">
      <div>{message}</div>
      {details.length > 0 ? (
        <ul className="mt-2 list-disc space-y-1 pl-5 text-red-800">
          {details.map((detail, index) => (
            <li key={`${detail.field ?? "detail"}-${index}`}>
              {detail.field ? `${detail.field}: ` : ""}{detail.message}
            </li>
          ))}
        </ul>
      ) : null}
      {process.env.NODE_ENV === "development" && debug ? (
        <details className="mt-3 break-all rounded-xl border border-red-200 p-3 font-mono text-xs text-red-800">
          <summary className="cursor-pointer">Технические сведения</summary>
          {debug.status ? <div>HTTP {debug.status}</div> : null}
          {debug.backendMessage ? <div>{debug.backendMessage}</div> : null}
          {debug.path ? <div>{debug.path}</div> : null}
        </details>
      ) : null}
    </div>
  );
}
