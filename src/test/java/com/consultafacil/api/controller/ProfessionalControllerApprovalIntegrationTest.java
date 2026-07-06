package com.consultafacil.api.controller;

import com.consultafacil.api.dto.professional.CreateProfessionalDTO;
import com.consultafacil.api.dto.user.CreateUserDTO;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.Gender;
import com.consultafacil.domain.enums.ProfessionalType;
import com.consultafacil.domain.enums.Specialty;
import com.consultafacil.domain.enums.UserRole;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.time.LocalDate;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProfessionalControllerApprovalIntegrationTest extends ProfessionalControllerTestBase {

    @Test
    void testPatientCanSubmitApplicationAndAdminCanApprove() throws Exception {
        CreateUserDTO patientDTO = CreateUserDTO.builder()
                .name("Future Professional")
                .email("futurepro@example.com")
                .password("password123")
                .cpf("55566677788")
                .phone("11955554444")
                .birthDate(LocalDate.of(1985, 3, 15))
                .gender(Gender.FEMALE)
                .build();

        String patientId = mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patientDTO)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String patientUserId = objectMapper.readTree(patientId).get("id").asText();

        String patientToken = loginAndGetToken("futurepro@example.com", "password123");

        CreateProfessionalDTO dto = CreateProfessionalDTO.builder()
                .profession(ProfessionalType.MEDICO)
                .specialty(Specialty.PEDIATRIA)
                .licenseNumber("CRM-SP-55555")
                .build();

        String createResponse = mockMvc.perform(post("/professionals")
                .header("Authorization", "Bearer " + patientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.specialty", equalTo("PEDIATRIA")))
                .andExpect(jsonPath("$.status", equalTo("PENDING_REVIEW")))
                .andReturn().getResponse().getContentAsString();

        User user = userRepository.findById(patientUserId).orElseThrow();
        Assertions.assertEquals(UserRole.PATIENT, user.getRole());

        String newProfileId = objectMapper.readTree(createResponse).get("id").asText();
        mockMvc.perform(put("/professionals/" + newProfileId + "/approve")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", equalTo("ACTIVE")));

        userRepository.findById(patientUserId).ifPresent(u ->
                Assertions.assertEquals(UserRole.PROFESSIONAL, u.getRole()));
    }
}
