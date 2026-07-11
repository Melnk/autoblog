package com.autoblog.reminder.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
import java.util.UUID;
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
class MaintenanceReminderIntegrationTest {

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

    private static final String EVENT_REQUEST = """
            {
              "type": "MAINTENANCE",
              "eventDate": "2026-07-02",
              "odometerKm": %d,
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
    void ownerAndEditorCanCreateReminderButViewerAndStrangerCannot() throws Exception {
        String vehicleId = createVehicle(owner);
        grantAccess(owner, vehicleId, "editor@example.com", "EDITOR");
        grantAccess(owner, vehicleId, "viewer@example.com", "VIEWER");

        JsonNode ownerReminder = createReminder(owner, vehicleId, reminderBody(
                "Заменить масло",
                "OIL_CHANGE",
                LocalDate.now().plusDays(30),
                135000
        ));
        assertThat(ownerReminder.get("status").asText()).isEqualTo("ACTIVE");
        assertThat(ownerReminder.get("dueState").asText()).isEqualTo("UPCOMING");
        assertThat(ownerReminder.get("latestOdometerKm").isNull()).isTrue();

        mockMvc.perform(post("/api/v1/vehicles/{vehicleId}/reminders", vehicleId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(editor.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reminderBody("Проверить тормоза", "BRAKE_SERVICE", null, 130000)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("BRAKE_SERVICE"));

        mockMvc.perform(post("/api/v1/vehicles/{vehicleId}/reminders", vehicleId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(viewer.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reminderBody("Страховка", "INSURANCE", LocalDate.now().plusDays(60), null)))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/v1/vehicles/{vehicleId}/reminders", vehicleId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(stranger.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reminderBody("Страховка", "INSURANCE", LocalDate.now().plusDays(60), null)))
                .andExpect(status().isNotFound());
    }

    @Test
    void reminderValidationReturnsFieldLevelErrors() throws Exception {
        String vehicleId = createVehicle(owner);

        mockMvc.perform(post("/api/v1/vehicles/{vehicleId}/reminders", vehicleId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "type": "OIL_CHANGE",
                                  "dueDate": "%s"
                                }
                                """.formatted(LocalDate.now().plusDays(30))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details[*].field", hasItem("title")));

        mockMvc.perform(post("/api/v1/vehicles/{vehicleId}/reminders", vehicleId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Заменить масло",
                                  "type": "OIL_CHANGE"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details[*].field", hasItem("dueDate")))
                .andExpect(jsonPath("$.details[*].message", hasItem("Either due date or due odometer is required")));

        mockMvc.perform(post("/api/v1/vehicles/{vehicleId}/reminders", vehicleId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reminderBody("Пробег", "CUSTOM", null, -1)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details[*].field", hasItem("dueOdometerKm")));
    }

    @Test
    void listsRemindersWithDueStateLatestOdometerSortingAndFiltering() throws Exception {
        String vehicleId = createVehicle(owner);
        addEvent(owner, vehicleId, 120000);
        addEvent(owner, vehicleId, 127842);
        grantAccess(owner, vehicleId, "viewer@example.com", "VIEWER");

        JsonNode upcoming = createReminder(owner, vehicleId, reminderBody(
                "Страховка",
                "INSURANCE",
                LocalDate.now().plusDays(45),
                null
        ));
        JsonNode overdue = createReminder(owner, vehicleId, reminderBody(
                "Просроченная диагностика",
                "DIAGNOSTIC",
                LocalDate.now().minusDays(1),
                null
        ));
        JsonNode dueSoon = createReminder(owner, vehicleId, reminderBody(
                "Шины",
                "TIRE_SERVICE",
                null,
                128000
        ));

        mockMvc.perform(get("/api/v1/vehicles/{vehicleId}/reminders", vehicleId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(viewer.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].id").value(overdue.get("id").asText()))
                .andExpect(jsonPath("$[0].dueState").value("OVERDUE"))
                .andExpect(jsonPath("$[0].latestOdometerKm").value(127842))
                .andExpect(jsonPath("$[1].id").value(dueSoon.get("id").asText()))
                .andExpect(jsonPath("$[1].dueState").value("DUE_SOON"))
                .andExpect(jsonPath("$[2].id").value(upcoming.get("id").asText()))
                .andExpect(jsonPath("$[2].dueState").value("UPCOMING"));

        mockMvc.perform(get("/api/v1/vehicles/{vehicleId}/reminders", vehicleId)
                        .queryParam("dueState", "DUE_SOON")
                        .header(HttpHeaders.AUTHORIZATION, bearer(viewer.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(dueSoon.get("id").asText()));

        mockMvc.perform(get("/api/v1/vehicles/{vehicleId}/reminders", vehicleId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(stranger.token())))
                .andExpect(status().isNotFound());
    }

    @Test
    void dueStateCoversDateOdometerUpcomingCompletedAndCancelled() throws Exception {
        String vehicleId = createVehicle(owner);
        addEvent(owner, vehicleId, 127842);

        createReminder(owner, vehicleId, reminderBody("Дата просрочена", "CUSTOM", LocalDate.now().minusDays(1), null));
        createReminder(owner, vehicleId, reminderBody("Пробег просрочен", "CUSTOM", null, 127842));
        createReminder(owner, vehicleId, reminderBody("Дата скоро", "CUSTOM", LocalDate.now().plusDays(14), null));
        createReminder(owner, vehicleId, reminderBody("Пробег скоро", "CUSTOM", null, 128000));
        JsonNode upcoming = createReminder(owner, vehicleId, reminderBody("Потом", "CUSTOM", LocalDate.now().plusDays(60), 140000));
        JsonNode cancelledCandidate = createReminder(owner, vehicleId, reminderBody("Отменить", "CUSTOM", LocalDate.now().plusDays(60), null));

        JsonNode completed = completeReminder(owner, vehicleId, upcoming.get("id").asText());
        JsonNode cancelled = cancelReminder(owner, vehicleId, cancelledCandidate.get("id").asText());

        assertThat(completed.get("dueState").asText()).isEqualTo("COMPLETED");
        assertThat(completed.get("completedAt").asText()).isNotBlank();
        assertThat(cancelled.get("dueState").asText()).isEqualTo("CANCELLED");
        assertThat(cancelled.get("cancelledAt").asText()).isNotBlank();

        mockMvc.perform(get("/api/v1/vehicles/{vehicleId}/reminders", vehicleId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].dueState").value("OVERDUE"))
                .andExpect(jsonPath("$[1].dueState").value("OVERDUE"))
                .andExpect(jsonPath("$[2].dueState").value("DUE_SOON"))
                .andExpect(jsonPath("$[3].dueState").value("DUE_SOON"))
                .andExpect(jsonPath("$[4].dueState").value("COMPLETED"))
                .andExpect(jsonPath("$[5].dueState").value("CANCELLED"));
    }

    @Test
    void completeAndCancelRespectAccessAndAreIdempotent() throws Exception {
        String vehicleId = createVehicle(owner);
        grantAccess(owner, vehicleId, "editor@example.com", "EDITOR");
        grantAccess(owner, vehicleId, "viewer@example.com", "VIEWER");

        JsonNode ownerReminder = createReminder(owner, vehicleId, reminderBody(
                "Осмотр",
                "INSPECTION",
                LocalDate.now().plusDays(40),
                null
        ));
        JsonNode editorReminder = createReminder(owner, vehicleId, reminderBody(
                "Диагностика",
                "DIAGNOSTIC",
                LocalDate.now().plusDays(40),
                null
        ));
        JsonNode cancelReminder = createReminder(owner, vehicleId, reminderBody(
                "Шины",
                "TIRE_SERVICE",
                LocalDate.now().plusDays(40),
                null
        ));

        JsonNode completedByOwner = completeReminder(owner, vehicleId, ownerReminder.get("id").asText());
        JsonNode completedByEditor = completeReminder(editor, vehicleId, editorReminder.get("id").asText());
        JsonNode completedAgain = completeReminder(editor, vehicleId, editorReminder.get("id").asText());
        assertThat(completedByOwner.get("status").asText()).isEqualTo("COMPLETED");
        assertThat(completedByEditor.get("completedAt").asText()).isEqualTo(completedAgain.get("completedAt").asText());

        mockMvc.perform(patch("/api/v1/vehicles/{vehicleId}/reminders/{reminderId}/complete", vehicleId, cancelReminder.get("id").asText())
                        .header(HttpHeaders.AUTHORIZATION, bearer(viewer.token())))
                .andExpect(status().isForbidden());

        JsonNode cancelledByOwner = cancelReminder(owner, vehicleId, cancelReminder.get("id").asText());
        JsonNode cancelledAgain = cancelReminder(owner, vehicleId, cancelReminder.get("id").asText());
        assertThat(cancelledByOwner.get("status").asText()).isEqualTo("CANCELLED");
        assertThat(cancelledByOwner.get("cancelledAt").asText()).isEqualTo(cancelledAgain.get("cancelledAt").asText());

        mockMvc.perform(patch("/api/v1/vehicles/{vehicleId}/reminders/{reminderId}/cancel", vehicleId, ownerReminder.get("id").asText())
                        .header(HttpHeaders.AUTHORIZATION, bearer(viewer.token())))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/api/v1/vehicles/{vehicleId}/reminders/{reminderId}/complete", vehicleId, UUID.randomUUID())
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token())))
                .andExpect(status().isNotFound());
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

    private void addEvent(AuthUser user, String vehicleId, int odometerKm) throws Exception {
        mockMvc.perform(post("/api/v1/vehicles/{vehicleId}/events", vehicleId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(user.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(EVENT_REQUEST.formatted(odometerKm)))
                .andExpect(status().isCreated());
    }

    private JsonNode createReminder(AuthUser user, String vehicleId, String requestBody) throws Exception {
        String response = mockMvc.perform(post("/api/v1/vehicles/{vehicleId}/reminders", vehicleId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(user.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        return objectMapper.readTree(response);
    }

    private JsonNode completeReminder(AuthUser user, String vehicleId, String reminderId) throws Exception {
        String response = mockMvc.perform(patch("/api/v1/vehicles/{vehicleId}/reminders/{reminderId}/complete", vehicleId, reminderId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(user.token())))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        return objectMapper.readTree(response);
    }

    private JsonNode cancelReminder(AuthUser user, String vehicleId, String reminderId) throws Exception {
        String response = mockMvc.perform(patch("/api/v1/vehicles/{vehicleId}/reminders/{reminderId}/cancel", vehicleId, reminderId)
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

    private String reminderBody(String title, String type, LocalDate dueDate, Integer dueOdometerKm) {
        String dueDateJson = dueDate == null ? "null" : "\"" + dueDate + "\"";
        String dueOdometerJson = dueOdometerKm == null ? "null" : dueOdometerKm.toString();
        return """
                {
                  "title": "%s",
                  "description": "Плановое напоминание",
                  "type": "%s",
                  "dueDate": %s,
                  "dueOdometerKm": %s
                }
                """.formatted(title, type, dueDateJson, dueOdometerJson);
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
