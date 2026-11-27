package com.consultafacil.api.controller;

import com.consultafacil.api.dto.billing.feature.CreateFeatureDTO;
import com.consultafacil.api.dto.billing.feature.UpdateFeatureDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class FeatureControllerIntegrationTest extends ProfessionalControllerTestBase {

    private String featureId;

    @BeforeEach
    void createFeature() throws Exception {
        CreateFeatureDTO dto = new CreateFeatureDTO();
        // key is uppercased by CreateFeatureService → stored as "UNLIMITED_APPOINTMENTS"
        dto.setKey("unlimited_appointments");
        dto.setName("Unlimited Appointments");
        dto.setDescription("Book unlimited appointments per month");

        String resp = mockMvc.perform(post("/admin/billing/features")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        featureId = objectMapper.readTree(resp).get("id").asText();
    }

    @Test
    void listFeatures_requiresAdmin() throws Exception {
        mockMvc.perform(get("/admin/billing/features"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listFeatures_forbiddenForNonAdmin() throws Exception {
        mockMvc.perform(get("/admin/billing/features")
                .header("Authorization", "Bearer " + professionalToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void listFeatures_returnsListAsAdmin() throws Exception {
        mockMvc.perform(get("/admin/billing/features")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].key", notNullValue()));
    }

    @Test
    void getFeatureById_returnsOk() throws Exception {
        mockMvc.perform(get("/admin/billing/features/" + featureId)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", equalTo(featureId)))
                .andExpect(jsonPath("$.key", equalTo("UNLIMITED_APPOINTMENTS")));
    }

    @Test
    void getFeatureById_returns404WhenNotFound() throws Exception {
        mockMvc.perform(get("/admin/billing/features/non-existent-id")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void createFeature_returns200WithValidPayload() throws Exception {
        CreateFeatureDTO dto = new CreateFeatureDTO();
        // key is uppercased by the service → "VIDEO_CONSULTATIONS"
        dto.setKey("video_consultations");
        dto.setName("Video Consultations");

        mockMvc.perform(post("/admin/billing/features")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.key", equalTo("VIDEO_CONSULTATIONS")));
    }

    @Test
    void createFeature_returns400WhenMissingKey() throws Exception {
        CreateFeatureDTO dto = new CreateFeatureDTO();
        // key and name are @NotBlank — omit them
        dto.setDescription("Missing key and name");

        mockMvc.perform(post("/admin/billing/features")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createFeature_requiresAuth() throws Exception {
        CreateFeatureDTO dto = new CreateFeatureDTO();
        dto.setKey("no_auth_feature");
        dto.setName("No Auth Feature");

        mockMvc.perform(post("/admin/billing/features")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateFeature_returnsOkAndPersists() throws Exception {
        UpdateFeatureDTO update = new UpdateFeatureDTO();
        update.setName("Unlimited Appointments v2");

        mockMvc.perform(patch("/admin/billing/features/" + featureId)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", equalTo("Unlimited Appointments v2")));
    }

    @Test
    void updateFeature_requiresAdmin() throws Exception {
        UpdateFeatureDTO update = new UpdateFeatureDTO();
        update.setName("Hacked");

        mockMvc.perform(patch("/admin/billing/features/" + featureId)
                .header("Authorization", "Bearer " + professionalToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteFeature_returns204() throws Exception {
        mockMvc.perform(delete("/admin/billing/features/" + featureId)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/admin/billing/features/" + featureId)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteFeature_requiresAdmin() throws Exception {
        mockMvc.perform(delete("/admin/billing/features/" + featureId)
                .header("Authorization", "Bearer " + professionalToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteFeature_requiresAuth() throws Exception {
        mockMvc.perform(delete("/admin/billing/features/" + featureId))
                .andExpect(status().isUnauthorized());
    }
}
