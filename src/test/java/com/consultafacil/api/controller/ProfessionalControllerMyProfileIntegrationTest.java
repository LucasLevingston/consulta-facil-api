package com.consultafacil.api.controller;

import com.consultafacil.api.dto.user.CreateUserDTO;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.Gender;
import com.consultafacil.domain.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.time.LocalDate;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProfessionalControllerMyProfileIntegrationTest extends ProfessionalControllerTestBase {

    @Test
    void testGetMyProfile() throws Exception {
        mockMvc.perform(get("/professionals/me")
                .header("Authorization", "Bearer " + professionalToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.specialty", equalTo("CARDIOLOGIA")))
                .andExpect(jsonPath("$.licenseNumber", equalTo("CRM-SP-12345")));
    }

    @Test
    void testGetMyProfileForbiddenForPatient() throws Exception {
        registerUser("Patient", "patient@example.com", "password123",
                "11122233300", "11777776666", LocalDate.of(2000, 1, 1), Gender.MALE);
        String patientToken = loginAndGetToken("patient@example.com", "password123");

        mockMvc.perform(get("/professionals/me")
                .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void testGetMyProfileWithoutAuthIsForbidden() throws Exception {
        mockMvc.perform(get("/professionals/me"))
                .andExpect(status().isForbidden());
    }

    @Test
    void testGetMyProfile_professionalUserWithNoProfile_returns404() throws Exception {
        CreateUserDTO dto = CreateUserDTO.builder()
                .name("No Profile Pro")
                .email("noprofile.pro@example.com")
                .password("password123")
                .cpf("77788899900")
                .phone("11977778888")
                .birthDate(LocalDate.of(1985, 6, 1))
                .gender(Gender.MALE)
                .build();

        String regResponse = mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String userId = objectMapper.readTree(regResponse).get("id").asText();
        User user = userRepository.findById(userId).orElseThrow();
        user.setRole(UserRole.PROFESSIONAL);
        userRepository.saveAndFlush(user);

        String token = loginAndGetToken("noprofile.pro@example.com", "password123");

        mockMvc.perform(get("/professionals/me")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }
}
