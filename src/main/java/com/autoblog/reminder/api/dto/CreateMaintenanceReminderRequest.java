package com.autoblog.reminder.api.dto;

import com.autoblog.reminder.domain.ReminderType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

@Schema(description = "Maintenance reminder creation request. At least one of dueDate or dueOdometerKm is required.")
public record CreateMaintenanceReminderRequest(
        @NotBlank(message = "Title is required")
        @Size(max = 240, message = "Title must be 240 characters or fewer")
        @Schema(example = "Заменить масло", requiredMode = Schema.RequiredMode.REQUIRED)
        String title,

        @Size(max = 2000, message = "Description must be 2000 characters or fewer")
        @Schema(example = "Следующая замена масла после ТО")
        String description,

        @NotNull(message = "Reminder type is required")
        @Schema(example = "OIL_CHANGE", requiredMode = Schema.RequiredMode.REQUIRED)
        ReminderType type,

        @Schema(example = "2026-09-01")
        LocalDate dueDate,

        @Positive(message = "Due odometer must be positive")
        @Schema(example = "135000")
        Integer dueOdometerKm
) {
}
