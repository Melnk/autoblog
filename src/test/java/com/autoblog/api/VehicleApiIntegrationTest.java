package com.autoblog.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.autoblog.infrastructure.persistence.VehicleEventJpaRepository;
import com.autoblog.infrastructure.persistence.VehicleJpaRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
class VehicleApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private VehicleJpaRepository vehicles;

    @Autowired
    private VehicleEventJpaRepository events;

    @BeforeEach
    void cleanDatabase() {
        events.deleteAll();
        vehicles.deleteAll();
    }

    @Test
    void createsVehicle() throws Exception {
        mockMvc.perform(post("/api/v1/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(vehicleRequest(" XTA217030C0000000 ")))
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
    void duplicateVinReturnsConflict() throws Exception {
        createVehicle("XTA217030C0000000");

        mockMvc.perform(post("/api/v1/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(vehicleRequest("xta217030c0000000")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.path").value("/api/v1/vehicles"));
    }

    @Test
    void invalidVinReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(vehicleRequest("XTA217030O0000000")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void getsVehicleById() throws Exception {
        String vehicleId = createVehicle("XTA217030C0000000");

        mockMvc.perform(get("/api/v1/vehicles/{vehicleId}", vehicleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(vehicleId))
                .andExpect(jsonPath("$.vin").value("XTA217030C0000000"));
    }

    @Test
    void getsVehicleByVin() throws Exception {
        String vehicleId = createVehicle("XTA217030C0000000");

        mockMvc.perform(get("/api/v1/vehicles/by-vin/{vin}", "xta217030c0000000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(vehicleId))
                .andExpect(jsonPath("$.vin").value("XTA217030C0000000"));
    }

    @Test
    void createsFirstEvent() throws Exception {
        String vehicleId = createVehicle("XTA217030C0000000");

        JsonNode event = addEvent(vehicleId, "MAINTENANCE", "2026-06-28", 180000, "Замена масла");

        assertThat(event.get("sequenceNumber").asLong()).isEqualTo(1L);
        assertThat(event.get("previousEventHash").isNull()).isTrue();
        assertThat(event.get("eventHash").asText()).hasSize(64);
        assertThat(event.get("type").asText()).isEqualTo("MAINTENANCE");
        assertThat(event.get("eventDate").asText()).isEqualTo("2026-06-28");
        assertThat(event.get("costCurrency").asText()).isEqualTo("RUB");
        assertThat(event.get("payload").get("oil").asText()).isEqualTo("5W-40");
    }

    @Test
    void addingEventsPreservesHashChain() throws Exception {
        String vehicleId = createVehicle("XTA217030C0000000");

        JsonNode firstEvent = addEvent(vehicleId, "MAINTENANCE", "2026-06-28", 180000, "Замена масла");
        JsonNode secondEvent = addEvent(vehicleId, "INSPECTION", "2026-07-01", 180100, "Техосмотр");

        assertThat(firstEvent.get("sequenceNumber").asLong()).isEqualTo(1L);
        assertThat(secondEvent.get("sequenceNumber").asLong()).isEqualTo(2L);
        assertThat(secondEvent.get("previousEventHash").asText()).isEqualTo(firstEvent.get("eventHash").asText());
        assertThat(secondEvent.get("eventHash").asText()).isNotEqualTo(firstEvent.get("eventHash").asText());
    }

    @Test
    void getEventsReturnsEventsInSequenceOrder() throws Exception {
        String vehicleId = createVehicle("XTA217030C0000000");
        JsonNode firstEvent = addEvent(vehicleId, "MAINTENANCE", "2026-06-28", 180000, "Замена масла");
        JsonNode secondEvent = addEvent(vehicleId, "INSPECTION", "2026-07-01", 180100, "Техосмотр");

        mockMvc.perform(get("/api/v1/vehicles/{vehicleId}/events", vehicleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(firstEvent.get("id").asText()))
                .andExpect(jsonPath("$[0].sequenceNumber").value(1))
                .andExpect(jsonPath("$[1].id").value(secondEvent.get("id").asText()))
                .andExpect(jsonPath("$[1].sequenceNumber").value(2));
    }

    private String createVehicle(String vin) throws Exception {
        String response = mockMvc.perform(post("/api/v1/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(vehicleRequest(vin)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("id").asText();
    }

    private JsonNode addEvent(
            String vehicleId,
            String type,
            String eventDate,
            int odometerKm,
            String title
    ) throws Exception {
        String response = mockMvc.perform(post("/api/v1/vehicles/{vehicleId}/events", vehicleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "type": "%s",
                                  "eventDate": "%s",
                                  "odometerKm": %d,
                                  "title": "%s",
                                  "description": "Масло 5W-40, масляный фильтр",
                                  "costAmount": 4500,
                                  "serviceName": "Гаражный сервис",
                                  "payload": {
                                    "parts": ["oil_filter"],
                                    "oil": "5W-40"
                                  }
                                }
                                """.formatted(type, eventDate, odometerKm, title)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response);
    }

    private String vehicleRequest(String vin) {
        return """
                {
                  "vin": "%s",
                  "make": "Lada",
                  "model": "Priora",
                  "generation": "2170",
                  "year": 2012,
                  "engine": "1.6",
                  "transmission": "MT",
                  "trim": "Norma",
                  "market": "RU"
                }
                """.formatted(vin);
    }
}
