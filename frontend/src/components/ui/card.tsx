import { cn } from "@/lib/utils";

export function Card({ className, children }: { className?: string; children: React.ReactNode }) {
  return (
    <div className={cn("glass-panel rounded-xl p-5", className)}>
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
        <h1 className="text-3xl font-bold tracking-normal text-white">{title}</h1>
        {description ? <p className="mt-2 text-sm text-slate-400">{description}</p> : null}
      </div>
      {action}
    </div>
  );
}
