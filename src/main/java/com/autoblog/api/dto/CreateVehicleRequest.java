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

        @Min(value = 1886, message = "Year must be 1886 or later")
        @Max(value = 2100, message = "Year must be 2100 or earlier")
        Integer year
) {
}
