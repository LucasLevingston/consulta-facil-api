package com.consultafacil.api.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ClinicNearbyIntegrationTest extends ClinicControllerTestBase {

    @Test
    void testGetClinicsNearby_returnsClinicWithinRadius() throws Exception {
        // São Paulo coordinates
        Map<String, Object> body = Map.of(
                "name", "Nearby Clinic",
                "city", "São Paulo",
                "latitude", -23.5505,
                "longitude", -46.6333
        );

        mockMvc.perform(post("/clinics")
                .header("Authorization", "Bearer " + doctorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated());

        // Query from same location with 10km radius
        mockMvc.perform(get("/clinics/nearby")
                .param("lat", "-23.5505")
                .param("lng", "-46.6333")
                .param("radiusKm", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].name", equalTo("Nearby Clinic")));
    }

    @Test
    void testGetClinicsNearby_excludesFarClinics() throws Exception {
        // Clinic in São Paulo
        Map<String, Object> body = Map.of(
                "name", "SP Clinic", "city", "São Paulo",
                "latitude", -23.5505, "longitude", -46.6333
        );
        mockMvc.perform(post("/clinics")
                .header("Authorization", "Bearer " + doctorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated());

        // Query from Manaus (3000km away) with 50km radius — should return nothing
        mockMvc.perform(get("/clinics/nearby")
                .param("lat", "-3.1316")
                .param("lng", "-60.0213")
                .param("radiusKm", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void testGetClinicsNearby_clinicWithoutCoordinatesExcluded() throws Exception {
        // Clinic without coordinates
        Map<String, Object> body = Map.of("name", "No Location Clinic", "city", "São Paulo");
        mockMvc.perform(post("/clinics")
                .header("Authorization", "Bearer " + doctorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/clinics/nearby")
                .param("lat", "-23.5505")
                .param("lng", "-46.6333")
                .param("radiusKm", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
