package com.consultafacil.api.controller;

import com.consultafacil.api.dto.insurance.RegisterPatientInsuranceRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PatientInsuranceControllerIntegrationTest extends PatientInsuranceControllerTestBase {

    @Test
    void registerInsurance_patientCanRegisterOwnPlan() throws Exception {
        RegisterPatientInsuranceRequest req = new RegisterPatientInsuranceRequest(
                planId, "MBR-001", "John Doe", LocalDate.of(2026, 12, 31));

        mockMvc.perform(post("/patients/me/insurance")
                        .header("Authorization", "Bearer " + patientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.memberNumber", equalTo("MBR-001")))
                .andExpect(jsonPath("$.holderName", equalTo("John Doe")));
    }

    @Test
    void registerInsurance_requiresAuth() throws Exception {
        RegisterPatientInsuranceRequest req = new RegisterPatientInsuranceRequest(
                planId, "MBR-001", "John Doe", null);

        mockMvc.perform(post("/patients/me/insurance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registerInsurance_forbiddenForAdmin() throws Exception {
        // Admin role does not have PATIENT role, so canManageOwnInsurance returns false
        RegisterPatientInsuranceRequest req = new RegisterPatientInsuranceRequest(
                planId, "MBR-002", "Admin User", null);

        mockMvc.perform(post("/patients/me/insurance")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    void registerInsurance_returns400WhenMandatoryFieldsMissing() throws Exception {
        // memberNumber is @NotBlank
        RegisterPatientInsuranceRequest req = new RegisterPatientInsuranceRequest(
                planId, "", "John Doe", null);

        mockMvc.perform(post("/patients/me/insurance")
                        .header("Authorization", "Bearer " + patientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listInsurance_patientSeesOwnPlans() throws Exception {
        // Register one first
        RegisterPatientInsuranceRequest req = new RegisterPatientInsuranceRequest(
                planId, "MBR-LIST", "Jane Doe", null);
        mockMvc.perform(post("/patients/me/insurance")
                        .header("Authorization", "Bearer " + patientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/patients/me/insurance")
                        .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", isA(java.util.List.class)))
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    void listInsurance_requiresAuth() throws Exception {
        mockMvc.perform(get("/patients/me/insurance"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listInsurance_forbiddenForAdmin() throws Exception {
        mockMvc.perform(get("/patients/me/insurance")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isForbidden());
    }
}
