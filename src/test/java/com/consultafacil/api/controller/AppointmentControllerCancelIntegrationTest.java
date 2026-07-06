package com.consultafacil.api.controller;

import com.consultafacil.api.dto.appointment.CancelAppointmentDTO;
import com.consultafacil.api.dto.appointment.CreateAppointmentDTO;
import com.consultafacil.domain.entity.Appointment;
import com.consultafacil.domain.enums.AppointmentStatus;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AppointmentControllerCancelIntegrationTest extends AppointmentControllerIntegrationTestBase {

    @Test
    void testScheduleAndCancelAppointment() throws Exception {
        CreateAppointmentDTO dto = CreateAppointmentDTO.builder()
                .professionalId(professionalProfileId)
                .scheduledAt(LocalDateTime.now().plusDays(5))
                .reason("Consulta de retorno")
                .build();

        String createResponse = mockMvc.perform(post("/appointments")
                .header("Authorization", "Bearer " + patientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String appointmentId = objectMapper.readTree(createResponse).get("id").asText();

        CancelAppointmentDTO cancelDTO = CancelAppointmentDTO.builder()
                .cancellationReason("Paciente não pode comparecer")
                .build();

        mockMvc.perform(put("/appointments/" + appointmentId + "/cancel")
                .header("Authorization", "Bearer " + patientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cancelDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", equalTo("CANCELED")))
                .andExpect(jsonPath("$.cancellationReason", equalTo("Paciente não pode comparecer")));
    }

    @Test
    void testCancelCompletedAppointmentFails() throws Exception {
        CreateAppointmentDTO dto = CreateAppointmentDTO.builder()
                .professionalId(professionalProfileId)
                .scheduledAt(LocalDateTime.now().plusDays(11))
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
        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointmentRepository.saveAndFlush(appointment);

        CancelAppointmentDTO cancelDTO = CancelAppointmentDTO.builder()
                .cancellationReason("Tentativa inválida")
                .build();

        mockMvc.perform(put("/appointments/" + appointmentId + "/cancel")
                .header("Authorization", "Bearer " + patientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cancelDTO)))
                .andExpect(status().isBadRequest());
    }
}
