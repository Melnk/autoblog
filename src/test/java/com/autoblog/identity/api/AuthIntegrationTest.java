package com.autoblog.identity.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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
class AuthIntegrationTest {

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

    @BeforeEach
    void cleanDatabase() {
        attachments.deleteAll();
        publicReports.deleteAll();
        reminders.deleteAll();
        vehicleAccess.deleteAll();
        events.deleteAll();
        vehicles.deleteAll();
        users.deleteAll();
    }

    @Test
    void registerUserReturnsToken() throws Exception {
        JsonNode response = register("  OWNER@Example.COM  ", "Owner");

        assertThat(response.get("accessToken").asText()).isNotBlank();
        assertThat(response.get("tokenType").asText()).isEqualTo("Bearer");
        assertThat(response.get("expiresInSeconds").asLong()).isEqualTo(3600);
        assertThat(response.at("/user/email").asText()).isEqualTo("owner@example.com");
        assertThat(response.at("/user/displayName").asText()).isEqualTo("Owner");
    }

    @Test
    void duplicateEmailReturnsConflict() throws Exception {
        register("owner@example.com", "Owner");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody("OWNER@example.com", "Owner 2")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void loginReturnsToken() throws Exception {
        register("owner@example.com", "Owner");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "OWNER@example.com",
                                  "password": "StrongPassword123!"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.user.email").value("owner@example.com"));
    }

    @Test
    void wrongPasswordReturnsUnauthorized() throws Exception {
        register("owner@example.com", "Owner");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "owner@example.com",
                                  "password": "WrongPassword123!"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void currentUserWorksWithToken() throws Exception {
        JsonNode registered = register("owner@example.com", "Owner");

        mockMvc.perform(get("/api/v1/auth/me")
                        .header(HttpHeaders.AUTHORIZATION, bearer(registered.get("accessToken").asText())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(registered.at("/user/id").asText()))
                .andExpect(jsonPath("$.email").value("owner@example.com"))
                .andExpect(jsonPath("$.displayName").value("Owner"));
    }

    @Test
    void protectedEndpointWithoutTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/vehicles"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void corsPreflightForRegisterIsAllowedWithoutAuthentication() throws Exception {
        var result = mockMvc.perform(options("/api/v1/auth/register")
                        .header(HttpHeaders.ORIGIN, "http://localhost:3000")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "content-type"))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:3000"))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isIn(200, 204);
    }

    private JsonNode register(String email, String displayName) throws Exception {
        String response = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody(email, displayName)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        return objectMapper.readTree(response);
    }

    private String registerBody(String email, String displayName) {
        return """
                {
                  "email": "%s",
                  "password": "StrongPassword123!",
                  "displayName": "%s"
                }
                """.formatted(email, displayName);
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
