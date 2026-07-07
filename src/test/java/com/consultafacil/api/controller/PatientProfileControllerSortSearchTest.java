package com.consultafacil.api.controller;

import com.consultafacil.api.dto.appointment.CreateAppointmentDTO;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PatientProfileControllerSortSearchTest extends PatientProfileControllerTestBase {

    @Test
    void testGetProfessionalPatientsSortByName() throws Exception {
        CreateAppointmentDTO dto = CreateAppointmentDTO.builder()
                .professionalId(professionalProfileId)
                .scheduledAt(LocalDateTime.now().plusDays(5))
                .reason("Consulta sort-by-name test").build();

        mockMvc.perform(post("/appointments")
                .header("Authorization", "Bearer " + patientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/patients/professional/" + doctorUserId)
                .header("Authorization", "Bearer " + doctorToken)
                .param("page", "0").param("size", "20").param("search", "").param("sort", "name"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", equalTo(1)));
    }

    @Test
    void testGetProfessionalPatientsSearchByName() throws Exception {
        CreateAppointmentDTO dto = CreateAppointmentDTO.builder()
                .professionalId(professionalProfileId)
                .scheduledAt(LocalDateTime.now().plusDays(7))
                .reason("Consulta search test").build();

        mockMvc.perform(post("/appointments")
                .header("Authorization", "Bearer " + patientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/patients/professional/" + doctorUserId)
                .header("Authorization", "Bearer " + doctorToken)
                .param("search", "Patient").param("sort", "recent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", equalTo(1)));

        mockMvc.perform(get("/patients/professional/" + doctorUserId)
                .header("Authorization", "Bearer " + doctorToken)
                .param("search", "ZZZNaoExiste").param("sort", "recent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", equalTo(0)));
    }
}
