package com.consultafacil.api.controller;

import com.consultafacil.api.dto.appointment.CancelAppointmentDTO;
import com.consultafacil.api.dto.appointment.CreateAppointmentDTO;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AppointmentControllerCancelDuplicateIntegrationTest extends AppointmentControllerIntegrationTestBase {

    @Test
    void testCancelAlreadyCancelledFails() throws Exception {
        CreateAppointmentDTO dto = CreateAppointmentDTO.builder()
                .professionalId(professionalProfileId)
                .scheduledAt(LocalDateTime.now().plusDays(17))
                .reason("Consulta")
                .build();

        String createResponse = mockMvc.perform(post("/appointments")
                .header("Authorization", "Bearer " + patientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String appointmentId = objectMapper.readTree(createResponse).get("id").asText();

        CancelAppointmentDTO cancelDTO = CancelAppointmentDTO.builder()
                .cancellationReason("Primeiro cancelamento")
                .build();

        mockMvc.perform(put("/appointments/" + appointmentId + "/cancel")
                .header("Authorization", "Bearer " + patientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cancelDTO)))
                .andExpect(status().isOk());

        mockMvc.perform(put("/appointments/" + appointmentId + "/cancel")
                .header("Authorization", "Bearer " + patientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cancelDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testScheduleDuplicateAppointmentFails() throws Exception {
        // withNano(0) avoids H2 nanosecond truncation causing equality check to miss
        LocalDateTime scheduledAt = LocalDateTime.now().withNano(0).plusDays(14);

        CreateAppointmentDTO dto = CreateAppointmentDTO.builder()
                .professionalId(professionalProfileId)
                .scheduledAt(scheduledAt)
                .reason("Primeira consulta")
                .build();

        mockMvc.perform(post("/appointments")
                .header("Authorization", "Bearer " + patientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/appointments")
                .header("Authorization", "Bearer " + patientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }
}
