package com.autoblog.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.autoblog.access.infrastructure.VehicleAccessJpaRepository;
import com.autoblog.attachment.infrastructure.EventAttachmentJpaRepository;
import com.autoblog.identity.infrastructure.UserAccountJpaRepository;
import com.autoblog.infrastructure.persistence.VehicleEventJpaRepository;
import com.autoblog.infrastructure.persistence.VehicleJpaRepository;
import com.autoblog.publicreport.infrastructure.PublicVehicleReportJpaRepository;
import com.autoblog.reminder.infrastructure.MaintenanceReminderJpaRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class VehicleApiIntegrationTest {

    private static final String README_VEHICLE_REQUEST = """
            {
              "vin": "XTA217030C0000000",
              "make": "Lada",
              "model": "Priora",
              "generation": "2170",
              "year": 2012,
              "engine": "1.6",
              "transmission": "MT",
              "trim": "Norma",
              "market": "RU"
            }
            """;

    private static final String README_MAINTENANCE_EVENT_REQUEST = """
            {
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
              }
            }
            """;

    private static final String README_REPAIR_EVENT_REQUEST = """
            {
              "type": "REPAIR",
              "eventDate": "2026-07-10",
              "odometerKm": 120500,
              "title": "Замена передних тормозных колодок",
              "description": "Заменены передние тормозные колодки",
              "costAmount": 3500,
              "costCurrency": "RUB",
              "serviceName": "Гаражный сервис",
              "payload": {
                "parts": ["front_brake_pads"]
              }
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private VehicleJpaRepository vehicles;

    @Autowired
    private VehicleEventJpaRepository events;

    @Autowired
    private PublicVehicleReportJpaRepository publicReports;

    @Autowired
    private MaintenanceReminderJpaRepository reminders;

    @Autowired
    private EventAttachmentJpaRepository attachments;

    @Autowired
    private VehicleAccessJpaRepository vehicleAccess;

    @Autowired
    private UserAccountJpaRepository users;

    private String ownerToken;

    @BeforeEach
    void cleanDatabase() throws Exception {
        attachments.deleteAll();
        publicReports.deleteAll();
        reminders.deleteAll();
        vehicleAccess.deleteAll();
        events.deleteAll();
        vehicles.deleteAll();
        users.deleteAll();
        ownerToken = register("owner@example.com");
    }

    @Test
    void createsVehicleUsingReadmeJsonExample() throws Exception {
        mockMvc.perform(post("/api/v1/vehicles")
                        .header(HttpHeaders.AUTHORIZATION, bearer(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(README_VEHICLE_REQUEST))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.vin").value("XTA217030C0000000"))
                .andExpect(jsonPath("$.make").value("Lada"))
                .andExpect(jsonPath("$.model").value("Priora"))
                .andExpect(jsonPath("$.generation").value("2170"))
                .andExpect(jsonPath("$.year").value(2012))
                .andExpect(jsonPath("$.engine").value("1.6"))
                .andExpect(jsonPath("$.transmission").value("MT"))
                .andExpect(jsonPath("$.trim").value("Norma"))
                .andExpect(jsonPath("$.market").value("RU"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.updatedAt").isNotEmpty());
    }

    @Test
    void protectedEndpointWithoutTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/vehicles"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void duplicateVinReturnsConflict() throws Exception {
        createVehicle(README_VEHICLE_REQUEST);

        mockMvc.perform(post("/api/v1/vehicles")
                        .header(HttpHeaders.AUTHORIZATION, bearer(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(README_VEHICLE_REQUEST))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.path").value("/api/v1/vehicles"));
    }

    @Test
    void invalidVinWithForbiddenCharacterReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/vehicles")
                        .header(HttpHeaders.AUTHORIZATION, bearer(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(README_VEHICLE_REQUEST.replace("XTA217030C0000000", "XTA217030O0000000")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void emptyVehiclePathVariableReturnsNotFoundInsteadOfServerError() throws Exception {
        mockMvc.perform(get("/api/v1/vehicles/")
                        .header(HttpHeaders.AUTHORIZATION, bearer(ownerToken)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void emptyVehiclePathVariableForEventsReturnsBadRequestInsteadOfServerError() throws Exception {
        mockMvc.perform(get("/api/v1/vehicles//events")
                        .header(HttpHeaders.AUTHORIZATION, bearer(ownerToken)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void getsVehicleById() throws Exception {
        String vehicleId = createVehicle(README_VEHICLE_REQUEST);

        mockMvc.perform(get("/api/v1/vehicles/{vehicleId}", vehicleId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(vehicleId))
                .andExpect(jsonPath("$.vin").value("XTA217030C0000000"));
    }

    @Test
    void getsVehicleByVin() throws Exception {
        String vehicleId = createVehicle(README_VEHICLE_REQUEST);

        mockMvc.perform(get("/api/v1/vehicles/by-vin/{vin}", "xta217030c0000000")
                        .header(HttpHeaders.AUTHORIZATION, bearer(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(vehicleId))
                .andExpect(jsonPath("$.vin").value("XTA217030C0000000"));
    }

    @Test
    void createsFirstMaintenanceEventUsingReadmeJsonExample() throws Exception {
        String vehicleId = createVehicle(README_VEHICLE_REQUEST);

        JsonNode event = addEvent(vehicleId, README_MAINTENANCE_EVENT_REQUEST);

        assertThat(event.get("sequenceNumber").asLong()).isEqualTo(1L);
        assertThat(event.get("previousEventHash").isNull()).isTrue();
        assertThat(event.get("eventHash").asText()).hasSize(64);
        assertThat(event.get("type").asText()).isEqualTo("MAINTENANCE");
        assertThat(event.get("eventDate").asText()).isEqualTo("2026-07-02");
        assertThat(event.get("odometerKm").asInt()).isEqualTo(120000);
        assertThat(event.get("costAmount").decimalValue()).isEqualByComparingTo(new BigDecimal("5000"));
        assertThat(event.get("costCurrency").asText()).isEqualTo("RUB");
        assertThat(event.get("serviceName").asText()).isEqualTo("Гаражный сервис");
        assertThat(event.get("payload").get("oil").asText()).isEqualTo("5W-40");
    }

    @Test
    void createsSecondRepairEventAndPreservesHashChain() throws Exception {
        String vehicleId = createVehicle(README_VEHICLE_REQUEST);
        JsonNode firstEvent = addEvent(vehicleId, README_MAINTENANCE_EVENT_REQUEST);

        JsonNode secondEvent = addEvent(vehicleId, README_REPAIR_EVENT_REQUEST);

        assertThat(secondEvent.get("sequenceNumber").asLong()).isEqualTo(2L);
        assertThat(secondEvent.get("type").asText()).isEqualTo("REPAIR");
        assertThat(secondEvent.get("eventDate").asText()).isEqualTo("2026-07-10");
        assertThat(secondEvent.get("previousEventHash").asText()).isEqualTo(firstEvent.get("eventHash").asText());
        assertThat(secondEvent.get("eventHash").asText()).isNotEqualTo(firstEvent.get("eventHash").asText());
    }

    @Test
    void getEventsReturnsEventsInSequenceOrder() throws Exception {
        String vehicleId = createVehicle(README_VEHICLE_REQUEST);
        JsonNode firstEvent = addEvent(vehicleId, README_MAINTENANCE_EVENT_REQUEST);
        JsonNode secondEvent = addEvent(vehicleId, README_REPAIR_EVENT_REQUEST);

        mockMvc.perform(get("/api/v1/vehicles/{vehicleId}/events", vehicleId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(firstEvent.get("id").asText()))
                .andExpect(jsonPath("$[0].sequenceNumber").value(1))
                .andExpect(jsonPath("$[1].id").value(secondEvent.get("id").asText()))
                .andExpect(jsonPath("$[1].sequenceNumber").value(2));
    }

    @Test
    void paginatedQueriesReturnMetadataAndRejectUnsafePageSizes() throws Exception {
        String firstVehicleId = createVehicle(README_VEHICLE_REQUEST);
        createVehicle(README_VEHICLE_REQUEST.replace("XTA217030C0000000", "XTA217030C0000001"));
        addEvent(firstVehicleId, README_MAINTENANCE_EVENT_REQUEST);
        addEvent(firstVehicleId, README_REPAIR_EVENT_REQUEST);

        mockMvc.perform(get("/api/v2/vehicles")
                        .param("page", "0")
                        .param("size", "1")
                        .header(HttpHeaders.AUTHORIZATION, bearer(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].role").value("OWNER"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(1))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(false));

        mockMvc.perform(get("/api/v2/vehicles/{vehicleId}/events", firstVehicleId)
                        .param("page", "1")
                        .param("size", "1")
                        .header(HttpHeaders.AUTHORIZATION, bearer(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].sequenceNumber").value(2))
                .andExpect(jsonPath("$.last").value(true));

        mockMvc.perform(get("/api/v2/vehicles")
                        .param("size", "101")
                        .header(HttpHeaders.AUTHORIZATION, bearer(ownerToken)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.details[0].field").value("size"));
    }

    @Test
    void actuatorHealthIsAvailableWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void generatedOpenApiContainsPaginatedAndPublicReportLifecycleEndpoints() throws Exception {
        String response = mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        JsonNode paths = objectMapper.readTree(response).get("paths");

        assertThat(paths.has("/api/v2/vehicles")).isTrue();
        assertThat(paths.has("/api/v2/vehicles/{vehicleId}/events")).isTrue();
        assertThat(paths.has("/api/v1/vehicles/{vehicleId}/public-report/rotations")).isTrue();
        assertThat(paths.get("/api/v1/vehicles/{vehicleId}/public-report").has("delete")).isTrue();
    }

    @Test
    void concurrentEventsReceiveUniqueSequentialNumbers() throws Exception {
        String vehicleId = createVehicle(README_VEHICLE_REQUEST);
        var executor = Executors.newFixedThreadPool(2);
        var ready = new CountDownLatch(2);
        var start = new CountDownLatch(1);

        try {
            Future<Integer> first = executor.submit(() -> createEventConcurrently(vehicleId, ready, start));
            Future<Integer> second = executor.submit(() -> createEventConcurrently(vehicleId, ready, start));
            ready.await();
            start.countDown();

            assertThat(first.get()).isEqualTo(201);
            assertThat(second.get()).isEqualTo(201);
        } finally {
            executor.shutdownNow();
        }

        mockMvc.perform(get("/api/v1/vehicles/{vehicleId}/events", vehicleId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].sequenceNumber").value(1))
                .andExpect(jsonPath("$[1].sequenceNumber").value(2));
    }

    @Test
    void invalidEventTypeReturnsFieldLevelError() throws Exception {
        String vehicleId = createVehicle(README_VEHICLE_REQUEST);

        mockMvc.perform(post("/api/v1/vehicles/{vehicleId}/events", vehicleId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(README_MAINTENANCE_EVENT_REQUEST.replace("MAINTENANCE", "REGISTRATION")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.details[0].field").value("type"))
                .andExpect(jsonPath("$.details[0].message", containsString("Unsupported event type: REGISTRATION")))
                .andExpect(jsonPath("$.details[0].message", containsString("MAINTENANCE")))
                .andExpect(jsonPath("$.details[0].message", containsString("OTHER")));
    }

    @Test
    void missingTitleReturnsFieldLevelError() throws Exception {
        String vehicleId = createVehicle(README_VEHICLE_REQUEST);

        mockMvc.perform(post("/api/v1/vehicles/{vehicleId}/events", vehicleId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "type": "MAINTENANCE",
                                  "eventDate": "2026-07-02",
                                  "odometerKm": 120000
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.details[*].field", hasItem("title")));
    }

    @Test
    void malformedJsonReturnsHelpfulBodyError() throws Exception {
        String vehicleId = createVehicle(README_VEHICLE_REQUEST);

        mockMvc.perform(post("/api/v1/vehicles/{vehicleId}/events", vehicleId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "type": "MAINTENANCE",
                                  "eventDate": "2026-07-02",
                                  "title": "Замена масла",
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.details[*].field", hasItem("body")));
    }

    private String createVehicle(String requestBody) throws Exception {
        String response = mockMvc.perform(post("/api/v1/vehicles")
                        .header(HttpHeaders.AUTHORIZATION, bearer(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        return objectMapper.readTree(response).get("id").asText();
    }

    private JsonNode addEvent(String vehicleId, String requestBody) throws Exception {
        String response = mockMvc.perform(post("/api/v1/vehicles/{vehicleId}/events", vehicleId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        return objectMapper.readTree(response);
    }

    private int createEventConcurrently(
            String vehicleId,
            CountDownLatch ready,
            CountDownLatch start
    ) throws Exception {
        ready.countDown();
        start.await();
        return mockMvc.perform(post("/api/v1/vehicles/{vehicleId}/events", vehicleId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(README_MAINTENANCE_EVENT_REQUEST))
                .andReturn()
                .getResponse()
                .getStatus();
    }

    private String register(String email) throws Exception {
        String response = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "StrongPassword123!",
                                  "displayName": "Owner"
                                }
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        return objectMapper.readTree(response).get("accessToken").asText();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
