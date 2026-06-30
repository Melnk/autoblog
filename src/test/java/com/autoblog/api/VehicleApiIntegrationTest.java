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
        mockMvc.perform(post("/api/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "vin": " 1hg-cm82633a004352 ",
                                  "make": "Honda",
                                  "model": "Accord",
                                  "year": 2003
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.vin").value("1HGCM82633A004352"))
                .andExpect(jsonPath("$.make").value("Honda"))
                .andExpect(jsonPath("$.model").value("Accord"))
                .andExpect(jsonPath("$.year").value(2003));
    }

    @Test
    void addingEventsPreservesHashChain() throws Exception {
        String vehicleId = createVehicle("1HGCM82633A004352");

        JsonNode firstEvent = addEvent(vehicleId, "MAINTENANCE", "2025-01-02T03:04:05Z", "Oil changed");
        JsonNode secondEvent = addEvent(vehicleId, "INSPECTION", "2025-02-02T03:04:05Z", "Passed inspection");

        assertThat(firstEvent.get("previousHash").isNull()).isTrue();
        assertThat(secondEvent.get("previousHash").asText()).isEqualTo(firstEvent.get("hash").asText());
        assertThat(secondEvent.get("hash").asText()).isNotEqualTo(firstEvent.get("hash").asText());
    }

    @Test
    void timelineReturnsEvents() throws Exception {
        String vehicleId = createVehicle("2HGCM82633A004352");
        JsonNode firstEvent = addEvent(vehicleId, "MAINTENANCE", "2025-01-02T03:04:05Z", "Oil changed");
        JsonNode secondEvent = addEvent(vehicleId, "INSPECTION", "2025-02-02T03:04:05Z", "Passed inspection");

        mockMvc.perform(get("/api/vehicles/{vehicleId}/timeline", vehicleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(firstEvent.get("id").asText()))
                .andExpect(jsonPath("$[0].sequenceNumber").value(1))
                .andExpect(jsonPath("$[1].id").value(secondEvent.get("id").asText()))
                .andExpect(jsonPath("$[1].sequenceNumber").value(2));
    }

    private String createVehicle(String vin) throws Exception {
        String response = mockMvc.perform(post("/api/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "vin": "%s",
                                  "make": "Honda",
                                  "model": "Accord",
                                  "year": 2003
                                }
                                """.formatted(vin)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("id").asText();
    }

    private JsonNode addEvent(String vehicleId, String eventType, String occurredAt, String description) throws Exception {
        String response = mockMvc.perform(post("/api/vehicles/{vehicleId}/events", vehicleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "eventType": "%s",
                                  "occurredAt": "%s",
                                  "description": "%s"
                                }
                                """.formatted(eventType, occurredAt, description)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response);
    }
}
