package com.autoblog.trust.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.greaterThan;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
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
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TrustScoreIntegrationTest {

    private static final String VEHICLE_REQUEST = """
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

    private static final byte[] PDF_BYTES = "%PDF-1.4\nreceipt\n".getBytes(StandardCharsets.UTF_8);
    private static final byte[] PNG_BYTES = new byte[] {
            (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 1, 2, 3
    };

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EventAttachmentJpaRepository attachments;

    @Autowired
    private PublicVehicleReportJpaRepository publicReports;

    @Autowired
    private MaintenanceReminderJpaRepository reminders;

    @Autowired
    private VehicleAccessJpaRepository vehicleAccess;

    @Autowired
    private VehicleEventJpaRepository events;

    @Autowired
    private VehicleJpaRepository vehicles;

    @Autowired
    private UserAccountJpaRepository users;

    private AuthUser owner;
    private AuthUser editor;
    private AuthUser viewer;
    private AuthUser stranger;

    @BeforeEach
    void cleanDatabase() throws Exception {
        attachments.deleteAll();
        publicReports.deleteAll();
        reminders.deleteAll();
        vehicleAccess.deleteAll();
        events.deleteAll();
        vehicles.deleteAll();
        users.deleteAll();

        owner = register("owner@example.com", "Owner");
        editor = register("editor@example.com", "Editor");
        viewer = register("viewer@example.com", "Viewer");
        stranger = register("stranger@example.com", "Stranger");
    }

    @Test
    void vehicleWithNoEventsReturnsUnknownTrustScore() throws Exception {
        String vehicleId = createVehicle(owner);

        mockMvc.perform(get("/api/v1/vehicles/{vehicleId}/trust-score", vehicleId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vehicleId").value(vehicleId))
                .andExpect(jsonPath("$.level").value("UNKNOWN"))
                .andExpect(jsonPath("$.metrics.eventsCount").value(0))
                .andExpect(jsonPath("$.signals[*].code", hasItem("NO_EVENTS")))
                .andExpect(jsonPath("$.signals[*].code", hasItem("NO_ATTACHMENTS")))
                .andExpect(jsonPath("$.signals[*].code", hasItem("NO_ODOMETER_DATA")));
    }

    @Test
    void validHashChainSeveralEventsAndAttachmentsIncreaseTrustScore() throws Exception {
        String vehicleId = createVehicle(owner);
        JsonNode firstEvent = createEvent(owner, vehicleId, 120000, LocalDate.now().minusMonths(3));
        createEvent(owner, vehicleId, 121000, LocalDate.now().minusMonths(2));
        JsonNode thirdEvent = createEvent(owner, vehicleId, 122000, LocalDate.now().minusDays(10));
        uploadAttachment(owner, vehicleId, firstEvent.get("id").asText(), "receipt.pdf", "application/pdf", PDF_BYTES, "RECEIPT", "PUBLIC");
        uploadAttachment(owner, vehicleId, thirdEvent.get("id").asText(), "photo.png", "image/png", PNG_BYTES, "PHOTO", "PRIVATE");

        mockMvc.perform(get("/api/v1/vehicles/{vehicleId}/trust-score", vehicleId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score", greaterThan(80)))
                .andExpect(jsonPath("$.level").value("HIGH"))
                .andExpect(jsonPath("$.signals[*].code", hasItem("HASH_CHAIN_VALID")))
                .andExpect(jsonPath("$.signals[*].code", hasItem("HAS_3_EVENTS")))
                .andExpect(jsonPath("$.signals[*].code", hasItem("HAS_EVENT_ATTACHMENTS")))
                .andExpect(jsonPath("$.signals[*].code", hasItem("HAS_PUBLIC_ATTACHMENTS")))
                .andExpect(jsonPath("$.metrics.eventsWithAttachmentsCount").value(2))
                .andExpect(jsonPath("$.metrics.publicAttachmentsCount").value(1))
                .andExpect(jsonPath("$.metrics.privateAttachmentsCount").value(1))
                .andExpect(jsonPath("$.metrics.odometerConsistent").value(true));
    }

    @Test
    void odometerRollbackCreatesNegativeSignal() throws Exception {
        String vehicleId = createVehicle(owner);
        createEvent(owner, vehicleId, 130000, LocalDate.now().minusMonths(2));
        createEvent(owner, vehicleId, 120000, LocalDate.now().minusDays(5));

        mockMvc.perform(get("/api/v1/vehicles/{vehicleId}/trust-score", vehicleId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.metrics.odometerConsistent").value(false))
                .andExpect(jsonPath("$.signals[*].code", hasItem("ODOMETER_INCONSISTENCY")))
                .andExpect(jsonPath("$.signals[*].points", hasItem(-25)));
    }

    @Test
    void overdueReminderCreatesMetricsAndNegativeSignal() throws Exception {
        String vehicleId = createVehicle(owner);
        createEvent(owner, vehicleId, 127842, LocalDate.now().minusDays(3));
        createReminder(owner, vehicleId, "Заменить масло", "OIL_CHANGE", LocalDate.now().minusDays(1), null);

        mockMvc.perform(get("/api/v1/vehicles/{vehicleId}/trust-score", vehicleId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.metrics.activeRemindersCount").value(1))
                .andExpect(jsonPath("$.metrics.overdueRemindersCount").value(1))
                .andExpect(jsonPath("$.signals[*].code", hasItem("HAS_REMINDERS")))
                .andExpect(jsonPath("$.signals[*].code", hasItem("HAS_OVERDUE_REMINDERS")));
    }

    @Test
    void trustScoreAccessFollowsVehicleAccessRoles() throws Exception {
        String vehicleId = createVehicle(owner);
        grantAccess(owner, vehicleId, "editor@example.com", "EDITOR");
        grantAccess(owner, vehicleId, "viewer@example.com", "VIEWER");

        mockMvc.perform(get("/api/v1/vehicles/{vehicleId}/trust-score", vehicleId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token())))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/vehicles/{vehicleId}/trust-score", vehicleId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(editor.token())))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/vehicles/{vehicleId}/trust-score", vehicleId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(viewer.token())))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/vehicles/{vehicleId}/trust-score", vehicleId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(stranger.token())))
                .andExpect(status().isNotFound());
    }

    @Test
    void publicReportIncludesPublicSafeTrustScore() throws Exception {
        String vehicleId = createVehicle(owner);
        JsonNode event = createEvent(owner, vehicleId, 120000, LocalDate.now().minusDays(10));
        uploadAttachment(owner, vehicleId, event.get("id").asText(), "receipt.pdf", "application/pdf", PDF_BYTES, "RECEIPT", "PUBLIC");
        JsonNode publicReport = createPublicReport(owner, vehicleId);

        mockMvc.perform(get("/api/v1/public/reports/{publicToken}", publicReport.get("publicToken").asText()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trustScore.score").isNumber())
                .andExpect(jsonPath("$.trustScore.level").isNotEmpty())
                .andExpect(jsonPath("$.trustScore.vehicleId").doesNotExist())
                .andExpect(jsonPath("$.trustScore.metrics.eventsCount").value(1))
                .andExpect(jsonPath("$.trustScore.metrics.publicAttachmentsCount").value(1))
                .andExpect(jsonPath("$.trustScore.metrics.hashChainValid").value(true));
    }

    private AuthUser register(String email, String displayName) throws Exception {
        String response = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "StrongPassword123!",
                                  "displayName": "%s"
                                }
                                """.formatted(email, displayName)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        JsonNode json = objectMapper.readTree(response);
        return new AuthUser(
                json.at("/user/id").asText(),
                json.at("/user/email").asText(),
                json.get("accessToken").asText()
        );
    }

    private String createVehicle(AuthUser user) throws Exception {
        String response = mockMvc.perform(post("/api/v1/vehicles")
                        .header(HttpHeaders.AUTHORIZATION, bearer(user.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VEHICLE_REQUEST))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        return objectMapper.readTree(response).get("id").asText();
    }

    private JsonNode createEvent(AuthUser user, String vehicleId, int odometerKm, LocalDate eventDate) throws Exception {
        String response = mockMvc.perform(post("/api/v1/vehicles/{vehicleId}/events", vehicleId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(user.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "type": "MAINTENANCE",
                                  "eventDate": "%s",
                                  "odometerKm": %d,
                                  "title": "Сервисная запись",
                                  "costAmount": 1000,
                                  "costCurrency": "RUB"
                                }
                                """.formatted(eventDate, odometerKm)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        return objectMapper.readTree(response);
    }

    private JsonNode createReminder(
            AuthUser user,
            String vehicleId,
            String title,
            String type,
            LocalDate dueDate,
            Integer dueOdometerKm
    ) throws Exception {
        String dueDateJson = dueDate == null ? "null" : "\"" + dueDate + "\"";
        String dueOdometerJson = dueOdometerKm == null ? "null" : dueOdometerKm.toString();
        String response = mockMvc.perform(post("/api/v1/vehicles/{vehicleId}/reminders", vehicleId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(user.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "%s",
                                  "type": "%s",
                                  "dueDate": %s,
                                  "dueOdometerKm": %s
                                }
                                """.formatted(title, type, dueDateJson, dueOdometerJson)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        return objectMapper.readTree(response);
    }

    private JsonNode uploadAttachment(
            AuthUser user,
            String vehicleId,
            String eventId,
            String filename,
            String contentType,
            byte[] content,
            String type,
            String visibility
    ) throws Exception {
        String response = mockMvc.perform(uploadRequest(user, vehicleId, eventId, filename, contentType, content, type, visibility))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        return objectMapper.readTree(response);
    }

    private MockMultipartHttpServletRequestBuilder uploadRequest(
            AuthUser user,
            String vehicleId,
            String eventId,
            String filename,
            String contentType,
            byte[] content,
            String type,
            String visibility
    ) {
        MockMultipartFile file = new MockMultipartFile("file", filename, contentType, content);
        MockMultipartHttpServletRequestBuilder request = multipart(
                "/api/v1/vehicles/{vehicleId}/events/{eventId}/attachments",
                vehicleId,
                eventId
        );
        request.header(HttpHeaders.AUTHORIZATION, bearer(user.token()));
        request.file(file);
        request.param("type", type);
        request.param("visibility", visibility);
        return request;
    }

    private JsonNode createPublicReport(AuthUser user, String vehicleId) throws Exception {
        String response = mockMvc.perform(post("/api/v1/vehicles/{vehicleId}/public-report", vehicleId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(user.token())))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        return objectMapper.readTree(response);
    }

    private JsonNode grantAccess(AuthUser actor, String vehicleId, String email, String role) throws Exception {
        String response = mockMvc.perform(post("/api/v1/vehicles/{vehicleId}/access", vehicleId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(actor.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "role": "%s"
                                }
                                """.formatted(email, role)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        return objectMapper.readTree(response);
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private record AuthUser(
            String userId,
            String email,
            String token
    ) {
    }
}
