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

class AppointmentControllerCompleteIntegrationTest extends AppointmentControllerIntegrationTestBase {

    @Test
    void testCompleteAppointment() throws Exception {
        // @Future validation on CreateAppointmentDTO requires a future date at creation
        CreateAppointmentDTO dto = CreateAppointmentDTO.builder()
                .professionalId(professionalProfileId)
                .scheduledAt(LocalDateTime.now().plusDays(1))
                .reason("Consulta")
                .build();

        String createResponse = mockMvc.perform(post("/appointments")
                .header("Authorization", "Bearer " + patientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String appointmentId = objectMapper.readTree(createResponse).get("id").asText();

        // Set CONFIRMED status and past scheduledAt directly via repository
        // so completeAppointment's checks (CONFIRMED + in the past) both pass
        Appointment appointment = appointmentRepository.findById(appointmentId).orElseThrow();
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointment.setScheduledAt(LocalDateTime.now().minusHours(1));
        appointmentRepository.saveAndFlush(appointment);

        mockMvc.perform(put("/appointments/" + appointmentId + "/complete")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", equalTo("COMPLETED")));
    }

    @Test
    void testCompleteNonConfirmedFails() throws Exception {
        CreateAppointmentDTO dto = CreateAppointmentDTO.builder()
                .professionalId(professionalProfileId)
                .scheduledAt(LocalDateTime.now().plusDays(9))
                .reason("Consulta")
                .build();

        String createResponse = mockMvc.perform(post("/appointments")
                .header("Authorization", "Bearer " + patientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String appointmentId = objectMapper.readTree(createResponse).get("id").asText();

        // PENDING → complete should fail
        mockMvc.perform(put("/appointments/" + appointmentId + "/complete")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest());
    }
}
