import { cn } from "@/lib/utils";

export function Field({
  label,
  error,
  children,
  hint
}: {
  label: string;
  error?: string;
  hint?: string;
  children: React.ReactNode;
}) {
  return (
    <label className="block">
      <span className="mb-2 block text-sm font-semibold text-ink">{label}</span>
      {children}
      {hint ? <span className="mt-2 block text-xs leading-5 text-muted">{hint}</span> : null}
      {error ? <span role="alert" className="mt-2 block text-xs text-red-700">{error}</span> : null}
    </label>
  );
}

export function inputClassName(className?: string) {
  return cn(
    "h-12 min-w-0 w-full rounded-2xl border border-line bg-canvas/60 px-4 text-base text-ink transition-colors placeholder:text-muted focus:border-ink focus:bg-white focus:ring-2 focus:ring-brand-yellow/40 sm:text-sm",
    className
  );
}

export function textareaClassName(className?: string) {
  return cn(
    "min-h-28 w-full rounded-2xl border border-line bg-canvas/60 px-4 py-3 text-base text-ink transition-colors placeholder:text-muted focus:border-ink focus:bg-white focus:ring-2 focus:ring-brand-yellow/40 sm:text-sm",
    className
  );
}
