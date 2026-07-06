package com.consultafacil.api.controller;

import com.consultafacil.api.dto.appointment.CreateAppointmentDTO;
import com.consultafacil.api.dto.auth.LoginRequestDTO;
import com.consultafacil.domain.entity.User;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AppointmentControllerListIntegrationTest extends AppointmentControllerIntegrationTestBase {

    @Test
    void testGetDoctorAppointments() throws Exception {
        CreateAppointmentDTO dto = CreateAppointmentDTO.builder()
                .professionalId(professionalProfileId)
                .scheduledAt(LocalDateTime.now().plusDays(4))
                .reason("Checkup")
                .build();

        mockMvc.perform(post("/appointments")
                .header("Authorization", "Bearer " + patientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/appointments/professional/" + professionalProfileId)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", instanceOf(java.util.ArrayList.class)))
                .andExpect(jsonPath("$.totalElements", equalTo(1)));
    }

    @Test
    void testDeleteAppointment() throws Exception {
        CreateAppointmentDTO dto = CreateAppointmentDTO.builder()
                .professionalId(professionalProfileId)
                .scheduledAt(LocalDateTime.now().plusDays(8))
                .reason("Consulta para deletar")
                .build();

        String createResponse = mockMvc.perform(post("/appointments")
                .header("Authorization", "Bearer " + patientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String appointmentId = objectMapper.readTree(createResponse).get("id").asText();

        // adminToken here has DOCTOR role — promote to ADMIN role inline
        User doctorUser = userRepository.findByEmail("doctor@example.com").orElseThrow();
        doctorUser.setRole(com.consultafacil.domain.enums.UserRole.ADMIN);
        userRepository.saveAndFlush(doctorUser);

        String freshAdminLoginResponse = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(LoginRequestDTO.builder()
                        .email("doctor@example.com")
                        .password("doctor123")
                        .build())))
                .andReturn().getResponse().getContentAsString();
        String freshAdminToken = objectMapper.readTree(freshAdminLoginResponse).get("token").asText();

        mockMvc.perform(delete("/appointments/" + appointmentId)
                .header("Authorization", "Bearer " + freshAdminToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/appointments/" + appointmentId)
                .header("Authorization", "Bearer " + freshAdminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void testScheduleRequiresPatientRole() throws Exception {
        // adminToken here is a DOCTOR — doctors cannot schedule appointments
        CreateAppointmentDTO dto = CreateAppointmentDTO.builder()
                .professionalId(professionalProfileId)
                .scheduledAt(LocalDateTime.now().plusDays(12))
                .reason("Consulta")
                .build();

        mockMvc.perform(post("/appointments")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }
}
