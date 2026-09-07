"use client";

import { Button } from "@/components/ui/button";
import { useLanguage } from "@/lib/i18n";

export function PaginationControls({
  page,
  totalPages,
  onPageChange
}: {
  page: number;
  totalPages: number;
  onPageChange: (page: number) => void;
}) {
  const { t } = useLanguage();
  if (totalPages <= 1) {
    return null;
  }

  return (
    <nav aria-label={t("common.page")} className="mt-5 flex flex-wrap items-center justify-between gap-2">
      <Button
        type="button"
        variant="secondary"
        className="px-3"
        disabled={page === 0}
        onClick={() => onPageChange(page - 1)}
      >
        {t("common.previous")}
      </Button>
      <span aria-live="polite" className="order-first w-full text-center text-xs text-muted sm:order-none sm:w-auto">
        {t("common.page")} {page + 1} / {totalPages}
      </span>
      <Button
        type="button"
        variant="secondary"
        className="px-3"
        disabled={page + 1 >= totalPages}
        onClick={() => onPageChange(page + 1)}
      >
        {t("common.next")}
      </Button>
    </nav>
  );
}
