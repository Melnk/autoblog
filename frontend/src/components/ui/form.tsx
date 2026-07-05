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
      <span className="mb-2 block text-sm font-medium text-slate-200">{label}</span>
      {children}
      {hint ? <span className="mt-1 block text-xs text-slate-500">{hint}</span> : null}
      {error ? <span className="mt-1 block text-xs text-red-300">{error}</span> : null}
    </label>
  );
}

export function inputClassName(className?: string) {
  return cn(
    "h-11 w-full rounded-lg border border-slate-700 bg-surface-900 px-3 text-sm text-slate-100 outline-none transition placeholder:text-slate-500 focus:border-neon-blue focus:ring-2 focus:ring-neon-blue/20",
    className
  );
}

export function textareaClassName(className?: string) {
  return cn(
    "min-h-28 w-full rounded-lg border border-slate-700 bg-surface-900 px-3 py-3 text-sm text-slate-100 outline-none transition placeholder:text-slate-500 focus:border-neon-blue focus:ring-2 focus:ring-neon-blue/20",
    className
  );
}
