package com.autoblog.publicreport.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PublicVehicleReportIntegrationTest {

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

    private static final String REPAIR_EVENT_REQUEST = """
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

    @BeforeEach
    void cleanDatabase() {
        publicReports.deleteAll();
        events.deleteAll();
        vehicles.deleteAll();
    }

    @Test
    void createsPublicReport() throws Exception {
        String vehicleId = createVehicle();

        JsonNode report = createPublicReport(vehicleId);

        assertThat(report.get("publicToken").asText()).isNotBlank();
        assertThat(report.get("publicUrl").asText()).isEqualTo("/api/v1/public/reports/" + report.get("publicToken").asText());
        assertThat(report.get("status").asText()).isEqualTo("ACTIVE");
        assertThat(report.get("vehicleId").asText()).isEqualTo(vehicleId);
    }

    @Test
    void creatingPublicReportIsIdempotentForActiveReport() throws Exception {
        String vehicleId = createVehicle();

        JsonNode firstReport = createPublicReport(vehicleId);
        JsonNode secondReport = createPublicReport(vehicleId);

        assertThat(secondReport.get("id").asText()).isEqualTo(firstReport.get("id").asText());
        assertThat(secondReport.get("publicToken").asText()).isEqualTo(firstReport.get("publicToken").asText());
    }

    @Test
    void getsPublicReportWithSummaryAndEvents() throws Exception {
        String vehicleId = createVehicle();
        JsonNode firstEvent = addEvent(vehicleId, MAINTENANCE_EVENT_REQUEST);
        JsonNode secondEvent = addEvent(vehicleId, REPAIR_EVENT_REQUEST);
        JsonNode metadata = createPublicReport(vehicleId);

        mockMvc.perform(get("/api/v1/public/reports/{publicToken}", metadata.get("publicToken").asText()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.report.publicToken").value(metadata.get("publicToken").asText()))
                .andExpect(jsonPath("$.report.status").value("ACTIVE"))
                .andExpect(jsonPath("$.report.id").doesNotExist())
                .andExpect(jsonPath("$.vehicle.vin").value("XTA217030C0000000"))
                .andExpect(jsonPath("$.vehicle.make").value("Lada"))
                .andExpect(jsonPath("$.vehicle.id").doesNotExist())
                .andExpect(jsonPath("$.summary.eventsCount").value(2))
                .andExpect(jsonPath("$.summary.firstEventDate").value("2026-07-02"))
                .andExpect(jsonPath("$.summary.lastEventDate").value("2026-07-10"))
                .andExpect(jsonPath("$.summary.latestOdometerKm").value(120500))
                .andExpect(jsonPath("$.summary.totalKnownCostAmount").value(8500.00))
                .andExpect(jsonPath("$.summary.costCurrency").value("RUB"))
                .andExpect(jsonPath("$.summary.hashChainValid").value(true))
                .andExpect(jsonPath("$.events[0].sequenceNumber").value(1))
                .andExpect(jsonPath("$.events[0].eventHash").value(firstEvent.get("eventHash").asText()))
                .andExpect(jsonPath("$.events[0].id").doesNotExist())
                .andExpect(jsonPath("$.events[0].vehicleId").doesNotExist())
                .andExpect(jsonPath("$.events[1].sequenceNumber").value(2))
                .andExpect(jsonPath("$.events[1].previousEventHash").value(firstEvent.get("eventHash").asText()))
                .andExpect(jsonPath("$.events[1].eventHash").value(secondEvent.get("eventHash").asText()))
                .andExpect(jsonPath("$.events[1].id").doesNotExist())
                .andExpect(jsonPath("$.events[1].vehicleId").doesNotExist());
    }

    @Test
    void unknownPublicTokenReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/public/reports/{publicToken}", "unknown-token"))
                .andExpect(status().isNotFound());
    }

    @Test
    void unknownVehicleReturnsNotFoundWhenCreatingPublicReport() throws Exception {
        mockMvc.perform(post("/api/v1/vehicles/{vehicleId}/public-report", "11111111-1111-1111-1111-111111111111"))
                .andExpect(status().isNotFound());
    }

    @Test
    void qrEndpointReturnsSvg() throws Exception {
        String vehicleId = createVehicle();
        JsonNode metadata = createPublicReport(vehicleId);

        String svg = mockMvc.perform(get("/api/v1/public/reports/{publicToken}/qr", metadata.get("publicToken").asText()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("image/svg+xml"))
                .andExpect(content().string(not("")))
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        assertThat(svg).contains("<svg");
        assertThat(svg).contains("/api/v1/public/reports/" + metadata.get("publicToken").asText());
    }

    private String createVehicle() throws Exception {
        String response = mockMvc.perform(post("/api/v1/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VEHICLE_REQUEST))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        return objectMapper.readTree(response).get("id").asText();
    }

    private JsonNode addEvent(String vehicleId, String requestBody) throws Exception {
        String response = mockMvc.perform(post("/api/v1/vehicles/{vehicleId}/events", vehicleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        return objectMapper.readTree(response);
    }

    private JsonNode createPublicReport(String vehicleId) throws Exception {
        String response = mockMvc.perform(post("/api/v1/vehicles/{vehicleId}/public-report", vehicleId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        return objectMapper.readTree(response);
    }
}
