package com.autoblog.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Vehicle creation request")
public record CreateVehicleRequest(
        @NotBlank(message = "VIN is required")
        @Size(max = 64, message = "VIN is too long")
        @Schema(description = "17-character VIN, normalized by trimming and uppercasing", example = "XTA217030C0000000", requiredMode = Schema.RequiredMode.REQUIRED)
        String vin,

        @Size(max = 120, message = "Make must be 120 characters or fewer")
        @Schema(example = "Lada")
        String make,

        @Size(max = 120, message = "Model must be 120 characters or fewer")
        @Schema(example = "Priora")
        String model,

        @Size(max = 120, message = "Generation must be 120 characters or fewer")
        @Schema(example = "2170")
        String generation,

        @Min(value = 1886, message = "Year must be 1886 or later")
        @Max(value = 2100, message = "Year must be 2100 or earlier")
        @Schema(example = "2012")
        Integer year,

        @Size(max = 120, message = "Engine must be 120 characters or fewer")
        @Schema(example = "1.6")
        String engine,

        @Size(max = 64, message = "Transmission must be 64 characters or fewer")
        @Schema(example = "MT")
        String transmission,

        @Size(max = 120, message = "Trim must be 120 characters or fewer")
        @Schema(example = "Norma")
        String trim,

        @Size(min = 2, max = 8, message = "Market must be between 2 and 8 characters")
        @Schema(description = "Market code. Defaults to RU when omitted.", example = "RU")
        String market
) {
}
