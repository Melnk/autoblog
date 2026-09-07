import Link from "next/link";
import type { ButtonHTMLAttributes, ComponentProps } from "react";
import { cn } from "@/lib/utils";

type Variant = "primary" | "secondary" | "ghost" | "danger";

const variants: Record<Variant, string> = {
  primary: "bg-brand-yellow text-ink hover:bg-[#F4C524] active:bg-[#EABD25]",
  secondary: "border border-line bg-white text-ink hover:border-ink/25 hover:bg-canvas",
  ghost: "text-muted hover:bg-ink/5 hover:text-ink",
  danger: "border border-red-200 bg-red-50 text-red-700 hover:bg-red-100"
};

export function Button({
  className,
  variant = "primary",
  ...props
}: ButtonHTMLAttributes<HTMLButtonElement> & { variant?: Variant }) {
  return (
    <button
      className={cn(
        "inline-flex min-h-11 shrink-0 items-center justify-center gap-2 rounded-2xl px-5 py-3 text-sm font-semibold transition-colors disabled:cursor-not-allowed disabled:opacity-50",
        variants[variant],
        className
      )}
      {...props}
    />
  );
}

export function ButtonLink({
  className,
  variant = "primary",
  ...props
}: ComponentProps<typeof Link> & { variant?: Variant }) {
  return (
    <Link
      className={cn(
        "inline-flex min-h-11 shrink-0 items-center justify-center gap-2 rounded-2xl px-5 py-3 text-sm font-semibold transition-colors",
        variants[variant],
        className
      )}
      {...props}
    />
  );
}
