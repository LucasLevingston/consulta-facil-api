package com.consultafacil.api.controller;

import com.consultafacil.api.dto.appointment.CreateAppointmentDTO;
import com.consultafacil.domain.entity.Appointment;
import com.consultafacil.domain.enums.AppointmentStatus;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AppointmentControllerCompleteAccessIntegrationTest extends AppointmentControllerIntegrationTestBase {

    @Test
    void testCompleteRequiresDoctorOrAdminRole() throws Exception {
        CreateAppointmentDTO dto = CreateAppointmentDTO.builder()
                .professionalId(professionalProfileId)
                .scheduledAt(LocalDateTime.now().plusDays(15))
                .reason("Consulta")
                .build();

        String createResponse = mockMvc.perform(post("/appointments")
                .header("Authorization", "Bearer " + patientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String appointmentId = objectMapper.readTree(createResponse).get("id").asText();

        Appointment appointment = appointmentRepository.findById(appointmentId).orElseThrow();
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointmentRepository.saveAndFlush(appointment);

        // Patient cannot complete
        mockMvc.perform(put("/appointments/" + appointmentId + "/complete")
                .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void testCompleteConfirmedAppointment() throws Exception {
        CreateAppointmentDTO dto = CreateAppointmentDTO.builder()
                .professionalId(professionalProfileId)
                .scheduledAt(LocalDateTime.now().plusDays(16))
                .reason("Consulta")
                .build();

        String createResponse = mockMvc.perform(post("/appointments")
                .header("Authorization", "Bearer " + patientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String appointmentId = objectMapper.readTree(createResponse).get("id").asText();

        Appointment appointment = appointmentRepository.findById(appointmentId).orElseThrow();
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointmentRepository.saveAndFlush(appointment);

        mockMvc.perform(put("/appointments/" + appointmentId + "/complete")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", equalTo("COMPLETED")));
    }
}
