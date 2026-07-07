package com.consultafacil.api.controller;

import com.consultafacil.domain.enums.Gender;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ClinicPublicAndMyClinicIntegrationTest extends ClinicControllerTestBase {

    @Test
    void testListClinics_public_returnsEmptyWhenNone() throws Exception {
        mockMvc.perform(get("/clinics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", instanceOf(java.util.ArrayList.class)));
    }

    @Test
    void testListClinics_public_returnsClinics() throws Exception {
        createClinicAndGetId(doctorToken, "Clínica A");

        mockMvc.perform(get("/clinics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name", equalTo("Clínica A")))
                .andExpect(jsonPath("$[0].ownerId", equalTo(doctorUserId)));
    }

    @Test
    void testGetClinicById_returnsClinic() throws Exception {
        String clinicId = createClinicAndGetId(doctorToken, "Cardio Center");

        mockMvc.perform(get("/clinics/" + clinicId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", equalTo(clinicId)))
                .andExpect(jsonPath("$.name", equalTo("Cardio Center")))
                .andExpect(jsonPath("$.city", equalTo("São Paulo")));
    }

    @Test
    void testGetClinicById_notFound_returns404() throws Exception {
        mockMvc.perform(get("/clinics/non-existent-id"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetMyClinic_returnsEmptyWhenNone() throws Exception {
        mockMvc.perform(get("/clinics/my")
                .header("Authorization", "Bearer " + doctorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void testGetMyClinic_returnsOwnClinic() throws Exception {
        createClinicAndGetId(doctorToken, "Minha Clínica");

        mockMvc.perform(get("/clinics/my")
                .header("Authorization", "Bearer " + doctorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", equalTo("Minha Clínica")))
                .andExpect(jsonPath("$[0].members[0].role", equalTo("OWNER")));
    }

    @Test
    void testGetMyClinic_requiresAuthentication() throws Exception {
        mockMvc.perform(get("/clinics/my"))
                .andExpect(status().isForbidden());
    }

    @Test
    void testGetMyClinic_adminCanAccess() throws Exception {
        mockMvc.perform(get("/clinics/my")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void testGetMyClinic_patientCannotAccess() throws Exception {
        registerUser("Patient", "patient@example.com", "pass1234",
                "33344455566", "11900000003", LocalDate.of(1995, 3, 20), Gender.MALE);
        String patientToken = loginAndGetToken("patient@example.com", "pass1234");

        mockMvc.perform(get("/clinics/my")
                .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isForbidden());
    }
}
