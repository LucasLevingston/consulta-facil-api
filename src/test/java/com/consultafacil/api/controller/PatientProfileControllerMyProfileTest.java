package com.consultafacil.api.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PatientProfileControllerMyProfileTest extends PatientProfileControllerTestBase {

    @Test
    void testGetMyProfile() throws Exception {
        mockMvc.perform(get("/patients/me")
                .header("Authorization", "Bearer " + patientToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId", notNullValue()))
                .andExpect(jsonPath("$.name", equalTo("Patient User")))
                .andExpect(jsonPath("$.email", equalTo("patient@example.com")));
    }

    @Test
    void testGetPatientProfileById() throws Exception {
        mockMvc.perform(get("/patients/" + patientUserId)
                .header("Authorization", "Bearer " + patientToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId", equalTo(patientUserId)));
    }

    @Test
    void testUpdateMyProfile() throws Exception {
        Map<String, Object> updates = new HashMap<>();
        updates.put("occupation", "Software Engineer");

        mockMvc.perform(put("/patients/me")
                .header("Authorization", "Bearer " + patientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.occupation", equalTo("Software Engineer")));
    }
}
