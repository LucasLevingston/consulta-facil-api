package com.consultafacil.api.controller;

import com.consultafacil.api.dto.appointment.CreateAppointmentDTO;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AppointmentControllerQueueIntegrationTest extends AppointmentControllerIntegrationTestBase {

    @Test
    void testGetProfessionalAppointments_emptyPage() throws Exception {
        mockMvc.perform(get("/appointments/professional/" + professionalProfileId)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", instanceOf(java.util.ArrayList.class)))
                .andExpect(jsonPath("$.totalElements", equalTo(0)));
    }

    @Test
    void testGetProfessionalAppointments_nonExistentProfessional_returnsEmptyPageNotError() throws Exception {
        mockMvc.perform(get("/appointments/professional/non-existent-id")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", equalTo(0)));
    }

    @Test
    void testGetProfessionalAppointmentsBySource_filtersCorrectly() throws Exception {
        CreateAppointmentDTO dto = CreateAppointmentDTO.builder()
                .professionalId(professionalProfileId)
                .scheduledAt(LocalDateTime.now().plusDays(30))
                .reason("Consulta online")
                .build();

        mockMvc.perform(post("/appointments")
                .header("Authorization", "Bearer " + patientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        // source=ONLINE should find the appointment (default source is ONLINE)
        mockMvc.perform(get("/appointments/professional/" + professionalProfileId)
                .param("source", "ONLINE")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", equalTo(1)))
                .andExpect(jsonPath("$.content[0].professionalId", equalTo(professionalProfileId)));

        // source=WALK_IN should return empty
        mockMvc.perform(get("/appointments/professional/" + professionalProfileId)
                .param("source", "WALK_IN")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", equalTo(0)));
    }

    @Test
    void testGetProfessionalAppointments_patientForbidden() throws Exception {
        mockMvc.perform(get("/appointments/professional/" + professionalProfileId)
                .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isForbidden());
    }
}
