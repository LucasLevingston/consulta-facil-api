package com.consultafacil.api.controller;

import com.consultafacil.api.dto.appointment.CreateAppointmentDTO;
import com.consultafacil.api.dto.appointment.RateAppointmentDTO;
import com.consultafacil.domain.entity.Appointment;
import com.consultafacil.domain.enums.AppointmentStatus;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AppointmentControllerRatingIntegrationTest extends AppointmentControllerIntegrationTestBase {

    @Test
    void testRateCompletedAppointment() throws Exception {
        CreateAppointmentDTO dto = CreateAppointmentDTO.builder()
                .professionalId(professionalProfileId)
                .scheduledAt(LocalDateTime.now().plusDays(1))
                .reason("Consulta para avaliar")
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
        appointment.setScheduledAt(LocalDateTime.now().minusHours(1));
        appointmentRepository.saveAndFlush(appointment);

        RateAppointmentDTO rateDTO = RateAppointmentDTO.builder()
                .stars(5)
                .comment("Excelente atendimento!")
                .build();

        mockMvc.perform(post("/appointments/" + appointmentId + "/rate")
                .header("Authorization", "Bearer " + patientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(rateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating", equalTo(5)))
                .andExpect(jsonPath("$.ratingComment", equalTo("Excelente atendimento!")));
    }

    @Test
    void testRateNonCompletedAppointmentFails() throws Exception {
        CreateAppointmentDTO dto = CreateAppointmentDTO.builder()
                .professionalId(professionalProfileId)
                .scheduledAt(LocalDateTime.now().plusDays(2))
                .reason("Consulta pendente")
                .build();

        String createResp = mockMvc.perform(post("/appointments")
                .header("Authorization", "Bearer " + patientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String appointmentId = objectMapper.readTree(createResp).get("id").asText();

        RateAppointmentDTO rateDTO = RateAppointmentDTO.builder().stars(3).build();

        mockMvc.perform(post("/appointments/" + appointmentId + "/rate")
                .header("Authorization", "Bearer " + patientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(rateDTO)))
                .andExpect(status().isBadRequest());
    }
}
