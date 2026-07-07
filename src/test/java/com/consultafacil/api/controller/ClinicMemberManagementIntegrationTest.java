package com.consultafacil.api.controller;

import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ClinicMemberManagementIntegrationTest extends ClinicControllerTestBase {

    @Test
    void testAddMember_ownerAddsDoctor() throws Exception {
        String clinicId = createClinicAndGetId(doctorToken, "Multi Clinic");

        mockMvc.perform(post("/clinics/" + clinicId + "/members/" + secondProfessionalProfileId)
                .header("Authorization", "Bearer " + doctorToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/clinics/" + clinicId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.members", hasSize(2)));
    }

    @Test
    void testAddMember_nonOwnerCannotAdd() throws Exception {
        String clinicId = createClinicAndGetId(doctorToken, "Protected Clinic");

        mockMvc.perform(post("/clinics/" + clinicId + "/members/" + secondProfessionalProfileId)
                .header("Authorization", "Bearer " + secondDoctorToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testAddMember_duplicateReturns400() throws Exception {
        String clinicId = createClinicAndGetId(doctorToken, "Dupe Test Clinic");

        // Owner already added as OWNER member on creation — adding again should fail
        mockMvc.perform(post("/clinics/" + clinicId + "/members/" + professionalProfileId)
                .header("Authorization", "Bearer " + doctorToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testRemoveMember_ownerRemovesDoctor() throws Exception {
        String clinicId = createClinicAndGetId(doctorToken, "Remove Test Clinic");

        mockMvc.perform(post("/clinics/" + clinicId + "/members/" + secondProfessionalProfileId)
                .header("Authorization", "Bearer " + doctorToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/clinics/" + clinicId + "/members/" + secondProfessionalProfileId)
                .header("Authorization", "Bearer " + doctorToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/clinics/" + clinicId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.members", hasSize(1)));
    }

    @Test
    void testRemoveMember_nonOwnerCannotRemove() throws Exception {
        String clinicId = createClinicAndGetId(doctorToken, "Non-Owner Remove Test");

        mockMvc.perform(post("/clinics/" + clinicId + "/members/" + secondProfessionalProfileId)
                .header("Authorization", "Bearer " + doctorToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/clinics/" + clinicId + "/members/" + secondProfessionalProfileId)
                .header("Authorization", "Bearer " + secondDoctorToken))
                .andExpect(status().isBadRequest());
    }
}
