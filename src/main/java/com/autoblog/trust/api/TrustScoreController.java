package com.autoblog.trust.api;

import com.autoblog.trust.api.dto.TrustScoreResponse;
import com.autoblog.trust.application.TrustScoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/vehicles/{vehicleId}/trust-score")
@Tag(name = "Trust score")
@SecurityRequirement(name = "bearerAuth")
public class TrustScoreController {

    private final TrustScoreService trustScores;

    public TrustScoreController(TrustScoreService trustScores) {
        this.trustScores = trustScores;
    }

    @GetMapping
    @Operation(
            summary = "Get vehicle trust score",
            description = "Computes an explainable, rule-based vehicle history quality score from events, evidence, odometer data, reminders, and hash-chain validity.",
            responses = @ApiResponse(
                    responseCode = "200",
                    description = "Trust score",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = TrustScoreResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "vehicleId": "22222222-2222-2222-2222-222222222222",
                                      "score": 85,
                                      "level": "HIGH",
                                      "calculatedAt": "2026-07-04T12:00:00Z",
                                      "summary": "История автомобиля выглядит хорошо подтвержденной.",
                                      "signals": [
                                        {
                                          "code": "HASH_CHAIN_VALID",
                                          "impact": "POSITIVE",
                                          "points": 15,
                                          "message": "Event hash-chain is valid"
                                        }
                                      ],
                                      "metrics": {
                                        "eventsCount": 5,
                                        "eventsWithAttachmentsCount": 2,
                                        "publicAttachmentsCount": 1,
                                        "privateAttachmentsCount": 1,
                                        "odometerEventsCount": 5,
                                        "latestOdometerKm": 127842,
                                        "totalKnownCostAmount": 8500.00,
                                        "hashChainValid": true,
                                        "odometerConsistent": true,
                                        "firstEventDate": "2026-07-02",
                                        "lastEventDate": "2026-07-10",
                                        "activeRemindersCount": 1,
                                        "overdueRemindersCount": 0
                                      }
                                    }
                                    """)
                    )
            )
    )
    public TrustScoreResponse getTrustScore(@PathVariable UUID vehicleId) {
        return TrustScoreResponse.from(trustScores.calculateForPrivateVehicle(vehicleId));
    }
}
