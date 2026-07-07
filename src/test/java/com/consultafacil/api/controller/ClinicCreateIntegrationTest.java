package com.consultafacil.api.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ClinicCreateIntegrationTest extends ClinicControllerTestBase {

    @Test
    void testCreateClinic_success() throws Exception {
        Map<String, Object> body = Map.of(
                "name", "Cardio Clinic",
                "description", "Especializada em coração",
                "phone", "1122223333",
                "city", "São Paulo",
                "state", "SP"
        );

        mockMvc.perform(post("/clinics")
                .header("Authorization", "Bearer " + doctorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", equalTo("Cardio Clinic")))
                .andExpect(jsonPath("$.city", equalTo("São Paulo")))
                .andExpect(jsonPath("$.status", equalTo("ACTIVE")))
                .andExpect(jsonPath("$.ownerId", equalTo(doctorUserId)))
                .andExpect(jsonPath("$.members", hasSize(1)))
                .andExpect(jsonPath("$.members[0].role", equalTo("OWNER")));
    }

    @Test
    void testCreateClinic_withCoordinates_savesLocation() throws Exception {
        Map<String, Object> body = Map.of(
                "name", "Geo Clinic",
                "city", "São Paulo",
                "latitude", -23.5505,
                "longitude", -46.6333
        );

        mockMvc.perform(post("/clinics")
                .header("Authorization", "Bearer " + doctorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.latitude", closeTo(-23.5505, 0.001)))
                .andExpect(jsonPath("$.longitude", closeTo(-46.6333, 0.001)));
    }

    @Test
    void testCreateClinic_missingName_returns400() throws Exception {
        Map<String, Object> body = Map.of("city", "São Paulo");

        mockMvc.perform(post("/clinics")
                .header("Authorization", "Bearer " + doctorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateClinic_requiresAuthentication() throws Exception {
        Map<String, Object> body = Map.of("name", "Anon Clinic");

        mockMvc.perform(post("/clinics")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden());
    }
}
