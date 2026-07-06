package com.consultafacil.api.controller;

import com.consultafacil.api.dto.appointment.CreateAppointmentDTO;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PatientProfileControllerProfessionalPatientsTest extends PatientProfileControllerTestBase {

    @Test
    void testGetProfessionalPatientsEmptyInitially() throws Exception {
        mockMvc.perform(get("/patients/professional/" + doctorUserId)
                .header("Authorization", "Bearer " + doctorToken)
                .param("page", "0").param("size", "20").param("search", "").param("sort", "recent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", instanceOf(java.util.ArrayList.class)))
                .andExpect(jsonPath("$.totalElements", equalTo(0)));
    }

    @Test
    void testGetProfessionalPatientsAfterAppointment() throws Exception {
        CreateAppointmentDTO dto = CreateAppointmentDTO.builder()
                .professionalId(professionalProfileId)
                .scheduledAt(LocalDateTime.now().plusDays(5))
                .reason("Consulta de rotina").build();

        mockMvc.perform(post("/appointments")
                .header("Authorization", "Bearer " + patientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/patients/professional/" + doctorUserId)
                .header("Authorization", "Bearer " + doctorToken)
                .param("page", "0").param("size", "20").param("search", "").param("sort", "recent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", equalTo(1)))
                .andExpect(jsonPath("$.content[0].name", equalTo("Patient User")))
                .andExpect(jsonPath("$.content[0].totalAppointments", equalTo(1)));
    }

    @Test
    void testGetProfessionalPatientsRequiresAuth() throws Exception {
        mockMvc.perform(get("/patients/professional/" + doctorUserId)
                .param("page", "0").param("size", "20"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testGetProfessionalPatients_unknownUserId_returns404() throws Exception {
        mockMvc.perform(get("/patients/professional/non-existent-user-id")
                .header("Authorization", "Bearer " + doctorToken)
                .param("page", "0").param("size", "10"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetProfessionalPatients_withProfileIdInsteadOfUserId_returns404() throws Exception {
        mockMvc.perform(get("/patients/professional/" + professionalProfileId)
                .header("Authorization", "Bearer " + doctorToken)
                .param("page", "0").param("size", "10"))
                .andExpect(status().isNotFound());
    }
}
