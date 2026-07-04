package com.autoblog.publicreport.api;

import com.autoblog.publicreport.api.dto.PublicReportMetadataResponse;
import com.autoblog.publicreport.application.PublicVehicleReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/vehicles/{vehicleId}/public-report")
@Tag(name = "Public vehicle reports")
@SecurityRequirement(name = "bearerAuth")
public class VehiclePublicReportController {

    private final PublicVehicleReportService publicReports;

    public VehiclePublicReportController(PublicVehicleReportService publicReports) {
        this.publicReports = publicReports;
    }

    @PostMapping
    @Operation(
            summary = "Create or get the active public report for a vehicle",
            responses = @ApiResponse(
                    responseCode = "200",
                    description = "Active public report metadata",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PublicReportMetadataResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "id": "11111111-1111-1111-1111-111111111111",
                                      "vehicleId": "22222222-2222-2222-2222-222222222222",
                                      "publicToken": "safe-random-token",
                                      "publicUrl": "/api/v1/public/reports/safe-random-token",
                                      "status": "ACTIVE",
                                      "createdAt": "2026-07-04T12:00:00Z",
                                      "updatedAt": "2026-07-04T12:00:00Z"
                                    }
                                    """)
                    )
            )
    )
    public PublicReportMetadataResponse createOrGetActivePublicReport(@PathVariable UUID vehicleId) {
        return PublicReportMetadataResponse.from(publicReports.createOrGetActiveReport(vehicleId));
    }
}
