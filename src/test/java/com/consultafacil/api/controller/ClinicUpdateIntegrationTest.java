package com.consultafacil.api.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ClinicUpdateIntegrationTest extends ClinicControllerTestBase {

    @Test
    void testUpdateClinic_ownerCanUpdate() throws Exception {
        String clinicId = createClinicAndGetId(doctorToken, "Original Name");

        Map<String, Object> update = Map.of(
                "name", "Updated Name",
                "city", "Rio de Janeiro",
                "state", "RJ"
        );

        mockMvc.perform(put("/clinics/" + clinicId)
                .header("Authorization", "Bearer " + doctorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", equalTo("Updated Name")))
                .andExpect(jsonPath("$.city", equalTo("Rio de Janeiro")));
    }

    @Test
    void testUpdateClinic_nonOwnerCannotUpdate() throws Exception {
        String clinicId = createClinicAndGetId(doctorToken, "Locked Clinic");

        Map<String, Object> update = Map.of("name", "Hacked Name", "city", "Hack City");

        mockMvc.perform(put("/clinics/" + clinicId)
                .header("Authorization", "Bearer " + secondDoctorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateClinic_withImageUrl() throws Exception {
        String clinicId = createClinicAndGetId(doctorToken, "Image Clinic");

        Map<String, Object> update = new java.util.HashMap<>();
        update.put("name", "Image Clinic Updated");
        update.put("imageUrl", "https://example.com/logo.png");

        mockMvc.perform(put("/clinics/" + clinicId)
                .header("Authorization", "Bearer " + doctorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", equalTo("Image Clinic Updated")));
    }
}
