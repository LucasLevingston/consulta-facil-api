package com.consultafacil.api.controller;

import com.consultafacil.api.dto.appointment.CreateAppointmentDTO;
import com.consultafacil.api.dto.appointment.RateAppointmentDTO;
import com.consultafacil.api.dto.appointment.RescheduleAppointmentDTO;
import com.consultafacil.domain.entity.Appointment;
import com.consultafacil.domain.enums.AppointmentStatus;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AppointmentControllerBusinessRuleIntegrationTest extends AppointmentControllerIntegrationTestBase {

    @Test
    void testRescheduleToConflictingTimeFails() throws Exception {
        LocalDateTime conflictTime = LocalDateTime.now().withNano(0).plusDays(22);

        CreateAppointmentDTO first = CreateAppointmentDTO.builder()
                .professionalId(professionalProfileId)
                .scheduledAt(conflictTime)
                .reason("Primeira consulta no horário")
                .build();

        mockMvc.perform(post("/appointments")
                .header("Authorization", "Bearer " + patientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(first)))
                .andExpect(status().isCreated());

        CreateAppointmentDTO second = CreateAppointmentDTO.builder()
                .professionalId(professionalProfileId)
                .scheduledAt(LocalDateTime.now().plusDays(23))
                .reason("Segunda consulta")
                .build();

        String secondResponse = mockMvc.perform(post("/appointments")
                .header("Authorization", "Bearer " + patientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(second)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String secondId = objectMapper.readTree(secondResponse).get("id").asText();

        RescheduleAppointmentDTO rescheduleDTO = new RescheduleAppointmentDTO();
        rescheduleDTO.setScheduledAt(conflictTime);

        mockMvc.perform(put("/appointments/" + secondId + "/reschedule")
                .header("Authorization", "Bearer " + patientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(rescheduleDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testRateAlreadyRatedAppointmentFails() throws Exception {
        CreateAppointmentDTO dto = CreateAppointmentDTO.builder()
                .professionalId(professionalProfileId)
                .scheduledAt(LocalDateTime.now().plusDays(1))
                .reason("Consulta já avaliada")
                .build();

        String createResp = mockMvc.perform(post("/appointments")
                .header("Authorization", "Bearer " + patientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String appointmentId = objectMapper.readTree(createResp).get("id").asText();

        Appointment appointment = appointmentRepository.findById(appointmentId).orElseThrow();
        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointment.setRating(4);
        appointmentRepository.saveAndFlush(appointment);

        RateAppointmentDTO rateDTO = RateAppointmentDTO.builder().stars(2).build();

        mockMvc.perform(post("/appointments/" + appointmentId + "/rate")
                .header("Authorization", "Bearer " + patientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(rateDTO)))
                .andExpect(status().isBadRequest());
    }
}
