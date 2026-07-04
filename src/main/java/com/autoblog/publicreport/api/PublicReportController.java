package com.autoblog.publicreport.api;

import com.autoblog.publicreport.api.dto.PublicVehicleReportResponse;
import com.autoblog.publicreport.application.PublicVehicleReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/public/reports")
@Tag(name = "Public vehicle reports")
public class PublicReportController {

    private static final MediaType SVG_MEDIA_TYPE = MediaType.valueOf("image/svg+xml");

    private final PublicVehicleReportService publicReports;

    public PublicReportController(PublicVehicleReportService publicReports) {
        this.publicReports = publicReports;
    }

    @GetMapping("/{publicToken}")
    @Operation(
            summary = "Get a public vehicle report by token",
            responses = @ApiResponse(
                    responseCode = "200",
                    description = "Public-safe vehicle report",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PublicVehicleReportResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "report": {
                                        "publicToken": "safe-random-token",
                                        "status": "ACTIVE",
                                        "createdAt": "2026-07-04T12:00:00Z",
                                        "updatedAt": "2026-07-04T12:00:00Z"
                                      },
                                      "vehicle": {
                                        "vin": "XTA217030C0000000",
                                        "make": "Lada",
                                        "model": "Priora",
                                        "generation": "2170",
                                        "year": 2012,
                                        "engine": "1.6",
                                        "transmission": "MT",
                                        "trim": "Norma",
                                        "market": "RU"
                                      },
                                      "summary": {
                                        "eventsCount": 2,
                                        "firstEventDate": "2026-07-02",
                                        "lastEventDate": "2026-07-10",
                                        "latestOdometerKm": 120500,
                                        "totalKnownCostAmount": 8500,
                                        "costCurrency": "RUB",
                                        "hashChainValid": true
                                      },
                                      "events": [
                                        {
                                          "sequenceNumber": 1,
                                          "type": "MAINTENANCE",
                                          "eventDate": "2026-07-02",
                                          "odometerKm": 120000,
                                          "title": "Замена масла",
                                          "description": "Масло 5W-40, масляный фильтр",
                                          "costAmount": 5000,
                                          "costCurrency": "RUB",
                                          "serviceName": "Гаражный сервис",
                                          "payload": {
                                            "oil": "5W-40",
                                            "parts": ["oil_filter"]
                                          },
                                          "previousEventHash": null,
                                          "eventHash": "hash"
                                        }
                                      ]
                                    }
                                    """)
                    )
            )
    )
    public PublicVehicleReportResponse getPublicReport(@PathVariable String publicToken) {
        return PublicVehicleReportResponse.from(publicReports.getPublicReport(publicToken));
    }

    @GetMapping(value = "/{publicToken}/qr", produces = "image/svg+xml")
    @Operation(
            summary = "Get a QR code SVG for a public vehicle report",
            responses = @ApiResponse(
                    responseCode = "200",
                    description = "QR code as SVG",
                    content = @Content(
                            mediaType = "image/svg+xml",
                            examples = @ExampleObject(value = "<svg xmlns=\"http://www.w3.org/2000/svg\"></svg>")
                    )
            )
    )
    public ResponseEntity<String> getPublicReportQr(@PathVariable String publicToken) {
        return ResponseEntity.ok()
                .contentType(SVG_MEDIA_TYPE)
                .body(publicReports.getQrSvg(publicToken));
    }
}
