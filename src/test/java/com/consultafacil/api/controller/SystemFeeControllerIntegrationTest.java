package com.consultafacil.api.controller;

import com.consultafacil.api.dto.billing.systemfee.UpdateSystemFeeDTO;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class SystemFeeControllerIntegrationTest extends ProfessionalControllerTestBase {

    @Test
    void listSystemFees_requiresAuth() throws Exception {
        mockMvc.perform(get("/admin/billing/system-fees"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listSystemFees_forbiddenForNonAdmin() throws Exception {
        mockMvc.perform(get("/admin/billing/system-fees")
                .header("Authorization", "Bearer " + professionalToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void listSystemFees_returnsListAsAdmin() throws Exception {
        mockMvc.perform(get("/admin/billing/system-fees")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", isA(java.util.List.class)));
    }

    @Test
    void getSystemFeeById_returns404ForNonExistent() throws Exception {
        mockMvc.perform(get("/admin/billing/system-fees/non-existent-id")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void getSystemFeeById_requiresAdmin() throws Exception {
        mockMvc.perform(get("/admin/billing/system-fees/any-id")
                .header("Authorization", "Bearer " + professionalToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateSystemFee_returns404ForNonExistent() throws Exception {
        UpdateSystemFeeDTO dto = new UpdateSystemFeeDTO();
        dto.setFixedFee(new BigDecimal("1.00"));
        dto.setPercentageFee(new BigDecimal("2.50"));
        dto.setActive(true);

        mockMvc.perform(patch("/admin/billing/system-fees/non-existent-id")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateSystemFee_requiresAdmin() throws Exception {
        UpdateSystemFeeDTO dto = new UpdateSystemFeeDTO();
        dto.setFixedFee(new BigDecimal("1.00"));

        mockMvc.perform(patch("/admin/billing/system-fees/any-id")
                .header("Authorization", "Bearer " + professionalToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateSystemFee_requiresAuth() throws Exception {
        UpdateSystemFeeDTO dto = new UpdateSystemFeeDTO();
        dto.setFixedFee(new BigDecimal("1.00"));

        mockMvc.perform(patch("/admin/billing/system-fees/any-id")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }
}
