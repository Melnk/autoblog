package com.autoblog.access.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.autoblog.access.infrastructure.VehicleAccessJpaRepository;
import com.autoblog.attachment.infrastructure.EventAttachmentJpaRepository;
import com.autoblog.identity.infrastructure.UserAccountJpaRepository;
import com.autoblog.infrastructure.persistence.VehicleEventJpaRepository;
import com.autoblog.infrastructure.persistence.VehicleJpaRepository;
import com.autoblog.publicreport.infrastructure.PublicVehicleReportJpaRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
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
class VehicleAccessControlIntegrationTest {

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

    private static final byte[] PDF_BYTES = "%PDF-1.4\nreceipt\n".getBytes(StandardCharsets.UTF_8);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EventAttachmentJpaRepository attachments;

    @Autowired
    private PublicVehicleReportJpaRepository publicReports;

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
    void createdVehicleGivesCreatorOwnerAccessAndOwnerCanReadIt() throws Exception {
        String vehicleId = createVehicle(owner);

        mockMvc.perform(get("/api/v1/vehicles/{vehicleId}", vehicleId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(vehicleId));

        mockMvc.perform(get("/api/v1/vehicles/{vehicleId}/access", vehicleId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].userId").value(owner.userId()))
                .andExpect(jsonPath("$[0].role").value("OWNER"));
    }

    @Test
    void inaccessibleVehicleIsHiddenByIdAndVinAndListShowsOnlyAccessibleVehicles() throws Exception {
        String vehicleId = createVehicle(owner);

        mockMvc.perform(get("/api/v1/vehicles/{vehicleId}", vehicleId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(stranger.token())))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/v1/vehicles/by-vin/{vin}", "XTA217030C0000000")
                        .header(HttpHeaders.AUTHORIZATION, bearer(stranger.token())))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/v1/vehicles")
                        .header(HttpHeaders.AUTHORIZATION, bearer(stranger.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void vehicleEventRolesAreEnforced() throws Exception {
        String vehicleId = createVehicle(owner);
        grantAccess(owner, vehicleId, "editor@example.com", "EDITOR");
        grantAccess(owner, vehicleId, "viewer@example.com", "VIEWER");

        JsonNode ownerEvent = createEvent(owner, vehicleId);
        JsonNode editorEvent = createEvent(editor, vehicleId);

        mockMvc.perform(post("/api/v1/vehicles/{vehicleId}/events", vehicleId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(viewer.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(EVENT_REQUEST))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/v1/vehicles/{vehicleId}/events", vehicleId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(stranger.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(EVENT_REQUEST))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/v1/vehicles/{vehicleId}/events", vehicleId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(viewer.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(ownerEvent.get("id").asText()))
                .andExpect(jsonPath("$[1].id").value(editorEvent.get("id").asText()));
    }

    @Test
    void attachmentRolesAreEnforced() throws Exception {
        String vehicleId = createVehicle(owner);
        String eventId = createEvent(owner, vehicleId).get("id").asText();
        grantAccess(owner, vehicleId, "editor@example.com", "EDITOR");
        grantAccess(owner, vehicleId, "viewer@example.com", "VIEWER");

        JsonNode ownerAttachment = uploadAttachment(owner, vehicleId, eventId, "owner-receipt.pdf", "PUBLIC");
        uploadAttachment(editor, vehicleId, eventId, "editor-receipt.pdf", "PRIVATE");

        mockMvc.perform(uploadRequest(viewer, vehicleId, eventId, "viewer-receipt.pdf", "PUBLIC"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/vehicles/{vehicleId}/events/{eventId}/attachments", vehicleId, eventId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(viewer.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        byte[] downloaded = mockMvc.perform(get(
                        "/api/v1/vehicles/{vehicleId}/events/{eventId}/attachments/{attachmentId}/download",
                        vehicleId,
                        eventId,
                        ownerAttachment.get("id").asText()
                ).header(HttpHeaders.AUTHORIZATION, bearer(viewer.token())))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/pdf"))
                .andReturn()
                .getResponse()
                .getContentAsByteArray();
        assertThat(downloaded).isEqualTo(PDF_BYTES);

        mockMvc.perform(get("/api/v1/vehicles/{vehicleId}/events/{eventId}/attachments", vehicleId, eventId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(stranger.token())))
                .andExpect(status().isNotFound());

        mockMvc.perform(get(
                        "/api/v1/vehicles/{vehicleId}/events/{eventId}/attachments/{attachmentId}/download",
                        vehicleId,
                        eventId,
                        ownerAttachment.get("id").asText()
                ).header(HttpHeaders.AUTHORIZATION, bearer(stranger.token())))
                .andExpect(status().isNotFound());
    }

    @Test
    void publicReportManagementRequiresOwnerOrEditorAndReadStaysPublic() throws Exception {
        String vehicleId = createVehicle(owner);
        String eventId = createEvent(owner, vehicleId).get("id").asText();
        JsonNode attachment = uploadAttachment(owner, vehicleId, eventId, "receipt.pdf", "PUBLIC");
        grantAccess(owner, vehicleId, "editor@example.com", "EDITOR");
        grantAccess(owner, vehicleId, "viewer@example.com", "VIEWER");

        JsonNode metadata = createPublicReport(editor, vehicleId);

        mockMvc.perform(post("/api/v1/vehicles/{vehicleId}/public-report", vehicleId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(viewer.token())))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/public/reports/{publicToken}", metadata.get("publicToken").asText()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.events[0].attachments[0].id").value(attachment.get("id").asText()));

        byte[] downloaded = mockMvc.perform(get(
                        "/api/v1/public/reports/{publicToken}/attachments/{attachmentId}",
                        metadata.get("publicToken").asText(),
                        attachment.get("id").asText()
                ))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/pdf"))
                .andReturn()
                .getResponse()
                .getContentAsByteArray();
        assertThat(downloaded).isEqualTo(PDF_BYTES);
    }

    @Test
    void accessManagementRequiresOwnerAndSupportsGrantListRevoke() throws Exception {
        String vehicleId = createVehicle(owner);

        JsonNode editorAccess = grantAccess(owner, vehicleId, "editor@example.com", "EDITOR");
        JsonNode viewerAccess = grantAccess(owner, vehicleId, "viewer@example.com", "VIEWER");

        mockMvc.perform(get("/api/v1/vehicles/{vehicleId}/access", vehicleId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[1].id").value(editorAccess.get("id").asText()))
                .andExpect(jsonPath("$[1].role").value("EDITOR"))
                .andExpect(jsonPath("$[2].id").value(viewerAccess.get("id").asText()))
                .andExpect(jsonPath("$[2].role").value("VIEWER"));

        mockMvc.perform(post("/api/v1/vehicles/{vehicleId}/access", vehicleId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(editor.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(grantAccessBody("stranger@example.com", "VIEWER")))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/v1/vehicles/{vehicleId}/access", vehicleId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(viewer.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(grantAccessBody("stranger@example.com", "VIEWER")))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/v1/vehicles/{vehicleId}/access/{userId}", vehicleId, editor.userId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token())))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/v1/vehicles/{vehicleId}/access/{userId}", vehicleId, viewer.userId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token())))
                .andExpect(status().isNoContent());
    }

    @Test
    void grantAccessRequiresEmailNotUserId() throws Exception {
        String vehicleId = createVehicle(owner);

        mockMvc.perform(post("/api/v1/vehicles/{vehicleId}/access", vehicleId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": "%s",
                                  "role": "EDITOR"
                                }
                                """.formatted(editor.userId())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details[*].field", hasItem("email")))
                .andExpect(jsonPath("$.details[*].message", hasItem("email is required")));
    }

    @Test
    void ownerCannotGrantOwnerOrRevokeOwnOwnerAccess() throws Exception {
        String vehicleId = createVehicle(owner);

        mockMvc.perform(post("/api/v1/vehicles/{vehicleId}/access", vehicleId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(grantAccessBody("editor@example.com", "OWNER")))
                .andExpect(status().isBadRequest());

        mockMvc.perform(delete("/api/v1/vehicles/{vehicleId}/access/{userId}", vehicleId, owner.userId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token())))
                .andExpect(status().isBadRequest());
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

    private JsonNode createEvent(AuthUser user, String vehicleId) throws Exception {
        String response = mockMvc.perform(post("/api/v1/vehicles/{vehicleId}/events", vehicleId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(user.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(EVENT_REQUEST))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        return objectMapper.readTree(response);
    }

    private JsonNode uploadAttachment(AuthUser user, String vehicleId, String eventId, String filename, String visibility) throws Exception {
        String response = mockMvc.perform(uploadRequest(user, vehicleId, eventId, filename, visibility))
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
            String visibility
    ) {
        MockMultipartFile file = new MockMultipartFile("file", filename, "application/pdf", PDF_BYTES);
        MockMultipartHttpServletRequestBuilder request = multipart(
                "/api/v1/vehicles/{vehicleId}/events/{eventId}/attachments",
                vehicleId,
                eventId
        );
        request.header(HttpHeaders.AUTHORIZATION, bearer(user.token()));
        request.file(file);
        request.param("type", "RECEIPT");
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
                        .content(grantAccessBody(email, role)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        return objectMapper.readTree(response);
    }

    private String grantAccessBody(String email, String role) {
        return """
                {
                  "email": "%s",
                  "role": "%s"
                }
                """.formatted(email, role);
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
