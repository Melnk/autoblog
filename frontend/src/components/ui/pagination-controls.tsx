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
    <div className="mt-5 flex items-center justify-between gap-3">
      <Button
        type="button"
        variant="secondary"
        disabled={page === 0}
        onClick={() => onPageChange(page - 1)}
      >
        {t("common.previous")}
      </Button>
      <span className="text-sm text-slate-400">
        {t("common.page")} {page + 1} / {totalPages}
      </span>
      <Button
        type="button"
        variant="secondary"
        disabled={page + 1 >= totalPages}
        onClick={() => onPageChange(page + 1)}
      >
        {t("common.next")}
      </Button>
    </div>
  );
}
