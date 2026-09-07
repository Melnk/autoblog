import { cn } from "@/lib/utils";

export function Card({ className, children }: { className?: string; children: React.ReactNode }) {
  return (
    <div className={cn("min-w-0 rounded-3xl border border-line bg-white p-5 shadow-card sm:p-6", className)}>
      {children}
    </div>
  );
}

export function SectionHeader({
  title,
  description,
  action
}: {
  title: string;
  description?: string;
  action?: React.ReactNode;
}) {
  return (
    <div className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
      <div>
        <h1 className="break-words text-3xl font-bold tracking-tight text-ink">{title}</h1>
        {description ? <p className="mt-2 break-words text-sm leading-6 text-muted">{description}</p> : null}
      </div>
      {action}
    </div>
  );
}
