package com.consultafacil.api.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PatientProfileControllerMedicalRecordsTest extends PatientProfileControllerTestBase {

    @Test
    void testGetPatientMedicalRecordsNotFound() throws Exception {
        mockMvc.perform(get("/patients/" + patientUserId + "/medical-records")
                .header("Authorization", "Bearer " + patientToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdatePatientMedicalRecords() throws Exception {
        Map<String, Object> updates = new HashMap<>();
        updates.put("allergies", "Penicillin");
        updates.put("currentMedication", "Ibuprofen");
        updates.put("privacyConsent", true);

        mockMvc.perform(put("/patients/" + patientUserId + "/medical-records")
                .header("Authorization", "Bearer " + patientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.allergies", equalTo("Penicillin")))
                .andExpect(jsonPath("$.currentMedication", equalTo("Ibuprofen")))
                .andExpect(jsonPath("$.privacyConsent", equalTo(true)));
    }
}
