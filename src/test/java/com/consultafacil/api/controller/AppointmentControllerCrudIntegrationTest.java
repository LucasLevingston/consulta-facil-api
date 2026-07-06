package com.consultafacil.api.controller;

import com.consultafacil.api.dto.appointment.CreateAppointmentDTO;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AppointmentControllerCrudIntegrationTest extends AppointmentControllerIntegrationTestBase {

    @Test
    void testGetAppointmentByIdNotFound() throws Exception {
        mockMvc.perform(get("/appointments/non-existent-id")
                .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetPatientAppointmentsReturnsEmptyPage() throws Exception {
        // patientUserId is the user ID; the endpoint /patient/{userId} resolves via PatientProfile
        mockMvc.perform(get("/appointments/patient/" + patientUserId)
                .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", instanceOf(java.util.ArrayList.class)))
                .andExpect(jsonPath("$.totalElements", equalTo(0)));
    }

    @Test
    void testScheduleAppointment() throws Exception {
        CreateAppointmentDTO dto = CreateAppointmentDTO.builder()
                .professionalId(professionalProfileId)
                .scheduledAt(LocalDateTime.now().plusDays(7))
                .reason("Consulta de rotina")
                .build();

        mockMvc.perform(post("/appointments")
                .header("Authorization", "Bearer " + patientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.status", equalTo("PENDING")))
                .andExpect(jsonPath("$.reason", equalTo("Consulta de rotina")))
                .andExpect(jsonPath("$.professionalId", equalTo(professionalProfileId)));
    }

    @Test
    void testGetAppointmentById() throws Exception {
        CreateAppointmentDTO dto = CreateAppointmentDTO.builder()
                .professionalId(professionalProfileId)
                .scheduledAt(LocalDateTime.now().plusDays(3))
                .reason("Consulta de rotina")
                .build();

        String createResponse = mockMvc.perform(post("/appointments")
                .header("Authorization", "Bearer " + patientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String appointmentId = objectMapper.readTree(createResponse).get("id").asText();

        mockMvc.perform(get("/appointments/" + appointmentId)
                .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", equalTo(appointmentId)))
                .andExpect(jsonPath("$.status", equalTo("PENDING")));
    }

    @Test
    void testGetPatientAppointmentsRequiresAuth() throws Exception {
        mockMvc.perform(get("/appointments/patient/" + patientUserId))
                .andExpect(status().isUnauthorized());
    }
}
