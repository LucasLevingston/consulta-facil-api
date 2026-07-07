package com.consultafacil.api.controller;

import com.consultafacil.api.dto.appointment.CreateAppointmentDTO;
import com.consultafacil.api.dto.appointment.RescheduleAppointmentDTO;
import com.consultafacil.domain.entity.Appointment;
import com.consultafacil.domain.enums.AppointmentStatus;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AppointmentControllerRescheduleIntegrationTest extends AppointmentControllerIntegrationTestBase {

    @Test
    void testReschedulePendingAppointment() throws Exception {
        LocalDateTime original = LocalDateTime.now().withNano(0).plusDays(20);
        LocalDateTime newTime = LocalDateTime.now().withNano(0).plusDays(25);

        CreateAppointmentDTO dto = CreateAppointmentDTO.builder()
                .professionalId(professionalProfileId)
                .scheduledAt(original)
                .reason("Consulta original")
                .build();

        String createResponse = mockMvc.perform(post("/appointments")
                .header("Authorization", "Bearer " + patientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String appointmentId = objectMapper.readTree(createResponse).get("id").asText();

        RescheduleAppointmentDTO rescheduleDTO = new RescheduleAppointmentDTO();
        rescheduleDTO.setScheduledAt(newTime);
        rescheduleDTO.setReason("Novo motivo");

        mockMvc.perform(put("/appointments/" + appointmentId + "/reschedule")
                .header("Authorization", "Bearer " + patientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(rescheduleDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", equalTo("PENDING")))
                .andExpect(jsonPath("$.previousScheduledAt", notNullValue()))
                .andExpect(jsonPath("$.reason", equalTo("Novo motivo")));
    }

    @Test
    void testRescheduleCompletedAppointmentFails() throws Exception {
        CreateAppointmentDTO dto = CreateAppointmentDTO.builder()
                .professionalId(professionalProfileId)
                .scheduledAt(LocalDateTime.now().plusDays(21))
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

        RescheduleAppointmentDTO rescheduleDTO = new RescheduleAppointmentDTO();
        rescheduleDTO.setScheduledAt(LocalDateTime.now().plusDays(30));

        mockMvc.perform(put("/appointments/" + appointmentId + "/reschedule")
                .header("Authorization", "Bearer " + patientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(rescheduleDTO)))
                .andExpect(status().isBadRequest());
    }
}
