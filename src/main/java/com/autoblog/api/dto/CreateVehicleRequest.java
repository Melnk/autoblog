package com.autoblog.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateVehicleRequest(
        @NotBlank(message = "VIN is required")
        @Size(max = 64, message = "VIN is too long")
        String vin,

        @Size(max = 120, message = "Make must be 120 characters or fewer")
        String make,

        @Size(max = 120, message = "Model must be 120 characters or fewer")
        String model,

        @Size(max = 120, message = "Generation must be 120 characters or fewer")
        String generation,

        @Min(value = 1886, message = "Year must be 1886 or later")
        @Max(value = 2100, message = "Year must be 2100 or earlier")
        Integer year,

        @Size(max = 120, message = "Engine must be 120 characters or fewer")
        String engine,

        @Size(max = 64, message = "Transmission must be 64 characters or fewer")
        String transmission,

        @Size(max = 120, message = "Trim must be 120 characters or fewer")
        String trim,

        @Size(min = 2, max = 8, message = "Market must be between 2 and 8 characters")
        String market
) {
}
