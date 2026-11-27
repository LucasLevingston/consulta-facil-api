package com.consultafacil.api.controller;

import com.consultafacil.api.dto.plan.CreatePlanDTO;
import com.consultafacil.api.dto.plan.UpdatePlanDTO;
import com.consultafacil.domain.enums.BillingPeriod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PlanControllerIntegrationTest extends ProfessionalControllerTestBase {

    private String createdPlanId;

    @BeforeEach
    void createPlan() throws Exception {
        CreatePlanDTO dto = new CreatePlanDTO();
        dto.setSlug("test-monthly");
        dto.setName("Test Monthly Plan");
        dto.setTier("basic");
        dto.setBillingPeriod(BillingPeriod.MONTHLY);
        dto.setPrice(new BigDecimal("99.90"));

        String resp = mockMvc.perform(post("/admin/plans")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        createdPlanId = objectMapper.readTree(resp).get("id").asText();
    }

    // ── Authenticated plan browsing ───────────────────────────────────────────

    @Test
    void listActivePlans_requiresAuth() throws Exception {
        // /plans is not in the permitAll list; unauthenticated gets 401
        mockMvc.perform(get("/plans"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listActivePlans_returnsOkWithAuth() throws Exception {
        mockMvc.perform(get("/plans")
                .header("Authorization", "Bearer " + professionalToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", isA(java.util.List.class)));
    }

    @Test
    void getPlanBySlug_returnsOkWhenFound() throws Exception {
        mockMvc.perform(get("/plans/test-monthly")
                .header("Authorization", "Bearer " + professionalToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug", equalTo("test-monthly")))
                .andExpect(jsonPath("$.name", equalTo("Test Monthly Plan")));
    }

    @Test
    void getPlanBySlug_returns404WhenNotFound() throws Exception {
        mockMvc.perform(get("/plans/non-existent-slug")
                .header("Authorization", "Bearer " + professionalToken))
                .andExpect(status().isNotFound());
    }

    // ── Admin endpoints ───────────────────────────────────────────────────────

    @Test
    void adminListAllPlans_requiresAdmin() throws Exception {
        mockMvc.perform(get("/admin/plans"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminListAllPlans_forbiddenForNonAdmin() throws Exception {
        mockMvc.perform(get("/admin/plans")
                .header("Authorization", "Bearer " + professionalToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminListAllPlans_returnsListAsAdmin() throws Exception {
        mockMvc.perform(get("/admin/plans")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    void adminCreatePlan_returns200WithValidPayload() throws Exception {
        CreatePlanDTO dto = new CreatePlanDTO();
        dto.setSlug("test-annual-new");
        dto.setName("Annual Plan New");
        dto.setTier("premium");
        dto.setBillingPeriod(BillingPeriod.ANNUAL);
        dto.setPrice(new BigDecimal("799.00"));

        mockMvc.perform(post("/admin/plans")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug", equalTo("test-annual-new")));
    }

    @Test
    void adminCreatePlan_returns400WhenMissingRequired() throws Exception {
        CreatePlanDTO dto = new CreatePlanDTO();
        // missing slug, name, tier, billingPeriod, price

        mockMvc.perform(post("/admin/plans")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void adminCreatePlan_requiresAuth() throws Exception {
        CreatePlanDTO dto = new CreatePlanDTO();
        dto.setSlug("no-auth");
        dto.setName("No Auth Plan");
        dto.setTier("basic");
        dto.setBillingPeriod(BillingPeriod.MONTHLY);
        dto.setPrice(BigDecimal.TEN);

        mockMvc.perform(post("/admin/plans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminUpdatePlan_returnsOk() throws Exception {
        UpdatePlanDTO update = new UpdatePlanDTO();
        update.setName("Updated Plan Name");

        mockMvc.perform(patch("/admin/plans/" + createdPlanId)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", equalTo("Updated Plan Name")));
    }

    @Test
    void adminUpdatePlan_requiresAdmin() throws Exception {
        UpdatePlanDTO update = new UpdatePlanDTO();
        update.setName("Hacked");

        mockMvc.perform(patch("/admin/plans/" + createdPlanId)
                .header("Authorization", "Bearer " + professionalToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isForbidden());
    }
}
