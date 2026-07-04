package com.autoblog.attachment.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.autoblog.access.infrastructure.VehicleAccessJpaRepository;
import com.autoblog.attachment.application.AttachmentChecksumService;
import com.autoblog.attachment.infrastructure.EventAttachmentJpaRepository;
import com.autoblog.identity.infrastructure.UserAccountJpaRepository;
import com.autoblog.infrastructure.persistence.VehicleEventJpaRepository;
import com.autoblog.infrastructure.persistence.VehicleJpaRepository;
import com.autoblog.publicreport.infrastructure.PublicVehicleReportJpaRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
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
class EventAttachmentIntegrationTest {

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

    private static final String MAINTENANCE_EVENT_REQUEST = """
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
    private static final byte[] PNG_BYTES = new byte[] {
            (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 1, 2, 3
    };

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
    private EventAttachmentJpaRepository attachments;

    @Autowired
    private AttachmentChecksumService checksumService;

    @Autowired
    private VehicleAccessJpaRepository vehicleAccess;

    @Autowired
    private UserAccountJpaRepository users;

    private String ownerToken;

    @BeforeEach
    void cleanDatabase() throws Exception {
        attachments.deleteAll();
        publicReports.deleteAll();
        vehicleAccess.deleteAll();
        events.deleteAll();
        vehicles.deleteAll();
        users.deleteAll();
        ownerToken = register("owner@example.com");
    }

    @Test
    void uploadsAttachmentAndReturnsMetadataWithoutStorageKey() throws Exception {
        String vehicleId = createVehicle();
        String eventId = createEvent(vehicleId);

        mockMvc.perform(uploadRequest(
                        vehicleId,
                        eventId,
                        "receipt.pdf",
                        "application/pdf",
                        PDF_BYTES,
                        "RECEIPT",
                        "PUBLIC",
                        "Чек за замену масла"
                ))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.vehicleId").value(vehicleId))
                .andExpect(jsonPath("$.eventId").value(eventId))
                .andExpect(jsonPath("$.type").value("RECEIPT"))
                .andExpect(jsonPath("$.visibility").value("PUBLIC"))
                .andExpect(jsonPath("$.originalFilename").value("receipt.pdf"))
                .andExpect(jsonPath("$.contentType").value("application/pdf"))
                .andExpect(jsonPath("$.sizeBytes").value(PDF_BYTES.length))
                .andExpect(jsonPath("$.checksumSha256").value(checksumService.sha256(PDF_BYTES)))
                .andExpect(jsonPath("$.description").value("Чек за замену масла"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.storageKey").doesNotExist());
    }

    @Test
    void listsAttachmentsForEventSortedByCreatedAt() throws Exception {
        String vehicleId = createVehicle();
        String eventId = createEvent(vehicleId);
        JsonNode first = uploadAttachment(vehicleId, eventId, "receipt.pdf", "application/pdf", PDF_BYTES, "RECEIPT", "PUBLIC");
        JsonNode second = uploadAttachment(vehicleId, eventId, "photo.png", "image/png", PNG_BYTES, "PHOTO", null);

        mockMvc.perform(get("/api/v1/vehicles/{vehicleId}/events/{eventId}/attachments", vehicleId, eventId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(first.get("id").asText()))
                .andExpect(jsonPath("$[1].id").value(second.get("id").asText()));
    }

    @Test
    void downloadsUploadedAttachmentBytes() throws Exception {
        String vehicleId = createVehicle();
        String eventId = createEvent(vehicleId);
        JsonNode attachment = uploadAttachment(vehicleId, eventId, "receipt.pdf", "application/pdf", PDF_BYTES, "RECEIPT", "PUBLIC");

        byte[] downloaded = mockMvc.perform(get(
                        "/api/v1/vehicles/{vehicleId}/events/{eventId}/attachments/{attachmentId}/download",
                        vehicleId,
                        eventId,
                        attachment.get("id").asText()
                ).header(HttpHeaders.AUTHORIZATION, bearer(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/pdf"))
                .andReturn()
                .getResponse()
                .getContentAsByteArray();

        assertThat(downloaded).isEqualTo(PDF_BYTES);
    }

    @Test
    void publicReportIncludesOnlyPublicAttachmentsAndPublicDownloadRespectsVisibility() throws Exception {
        String vehicleId = createVehicle();
        String eventId = createEvent(vehicleId);
        JsonNode publicAttachment = uploadAttachment(vehicleId, eventId, "receipt.pdf", "application/pdf", PDF_BYTES, "RECEIPT", "PUBLIC");
        JsonNode privateAttachment = uploadAttachment(vehicleId, eventId, "photo.png", "image/png", PNG_BYTES, "PHOTO", null);
        String publicToken = createPublicReport(vehicleId).get("publicToken").asText();

        String reportBody = mockMvc.perform(get("/api/v1/public/reports/{publicToken}", publicToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.events[0].attachments", hasSize(1)))
                .andExpect(jsonPath("$.events[0].attachments[0].id").value(publicAttachment.get("id").asText()))
                .andExpect(jsonPath("$.events[0].attachments[0].downloadUrl")
                        .value("/api/v1/public/reports/" + publicToken + "/attachments/" + publicAttachment.get("id").asText()))
                .andExpect(jsonPath("$.events[0].attachments[0].vehicleId").doesNotExist())
                .andExpect(jsonPath("$.events[0].attachments[0].eventId").doesNotExist())
                .andExpect(jsonPath("$.events[0].attachments[0].storageKey").doesNotExist())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        JsonNode publicAttachments = objectMapper.readTree(reportBody).at("/events/0/attachments");
        assertThat(publicAttachments.toString()).doesNotContain(privateAttachment.get("id").asText());

        byte[] downloaded = mockMvc.perform(get(
                        "/api/v1/public/reports/{publicToken}/attachments/{attachmentId}",
                        publicToken,
                        publicAttachment.get("id").asText()
                ))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/pdf"))
                .andReturn()
                .getResponse()
                .getContentAsByteArray();
        assertThat(downloaded).isEqualTo(PDF_BYTES);

        mockMvc.perform(get(
                        "/api/v1/public/reports/{publicToken}/attachments/{attachmentId}",
                        publicToken,
                        privateAttachment.get("id").asText()
                ))
                .andExpect(status().isNotFound());
    }

    @Test
    void rejectsInvalidAttachmentRequests() throws Exception {
        String vehicleId = createVehicle();
        String eventId = createEvent(vehicleId);
        String unknownId = UUID.randomUUID().toString();

        mockMvc.perform(uploadRequest(vehicleId, eventId, "empty.pdf", "application/pdf", new byte[0], "RECEIPT", null, null))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details[0].field").value("file"));

        mockMvc.perform(uploadRequest(vehicleId, eventId, "notes.txt", "text/plain", "nope".getBytes(StandardCharsets.UTF_8), "OTHER", null, null))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details[0].field").value("file"));

        mockMvc.perform(uploadRequest(vehicleId, eventId, "receipt.pdf", "application/pdf", PDF_BYTES, "BAD", null, null))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details[0].field").value("type"));

        mockMvc.perform(uploadRequest(unknownId, eventId, "receipt.pdf", "application/pdf", PDF_BYTES, "RECEIPT", null, null))
                .andExpect(status().isNotFound());

        mockMvc.perform(uploadRequest(vehicleId, unknownId, "receipt.pdf", "application/pdf", PDF_BYTES, "RECEIPT", null, null))
                .andExpect(status().isNotFound());

        mockMvc.perform(get(
                        "/api/v1/vehicles/{vehicleId}/events/{eventId}/attachments/{attachmentId}/download",
                        vehicleId,
                        eventId,
                        unknownId
                ).header(HttpHeaders.AUTHORIZATION, bearer(ownerToken)))
                .andExpect(status().isNotFound());
    }

    private String createVehicle() throws Exception {
        String response = mockMvc.perform(post("/api/v1/vehicles")
                        .header(HttpHeaders.AUTHORIZATION, bearer(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VEHICLE_REQUEST))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        return objectMapper.readTree(response).get("id").asText();
    }

    private String createEvent(String vehicleId) throws Exception {
        String response = mockMvc.perform(post("/api/v1/vehicles/{vehicleId}/events", vehicleId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(MAINTENANCE_EVENT_REQUEST))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        return objectMapper.readTree(response).get("id").asText();
    }

    private JsonNode createPublicReport(String vehicleId) throws Exception {
        String response = mockMvc.perform(post("/api/v1/vehicles/{vehicleId}/public-report", vehicleId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(ownerToken)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        return objectMapper.readTree(response);
    }

    private JsonNode uploadAttachment(
            String vehicleId,
            String eventId,
            String filename,
            String contentType,
            byte[] content,
            String type,
            String visibility
    ) throws Exception {
        String response = mockMvc.perform(uploadRequest(vehicleId, eventId, filename, contentType, content, type, visibility, null))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        return objectMapper.readTree(response);
    }

    private MockMultipartHttpServletRequestBuilder uploadRequest(
            String vehicleId,
            String eventId,
            String filename,
            String contentType,
            byte[] content,
            String type,
            String visibility,
            String description
    ) {
        MockMultipartFile file = new MockMultipartFile("file", filename, contentType, content);
        MockMultipartHttpServletRequestBuilder request = multipart(
                "/api/v1/vehicles/{vehicleId}/events/{eventId}/attachments",
                vehicleId,
                eventId
        );
        request.header(HttpHeaders.AUTHORIZATION, bearer(ownerToken));
        request.file(file);
        request.param("type", type);
        if (visibility != null) {
            request.param("visibility", visibility);
        }
        if (description != null) {
            request.param("description", description);
        }
        return request;
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
