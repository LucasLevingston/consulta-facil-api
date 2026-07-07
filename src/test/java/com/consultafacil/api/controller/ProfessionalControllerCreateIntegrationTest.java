package com.consultafacil.api.controller;

import com.consultafacil.api.dto.professional.CreateProfessionalDTO;
import com.consultafacil.domain.enums.ProfessionalType;
import com.consultafacil.domain.enums.Specialty;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProfessionalControllerCreateIntegrationTest extends ProfessionalControllerTestBase {

    @Test
    void testCreateProfessionalProfile() throws Exception {
        CreateProfessionalDTO dto = CreateProfessionalDTO.builder()
                .profession(ProfessionalType.MEDICO)
                .specialty(Specialty.NEUROLOGIA)
                .licenseNumber("CRM-RJ-99999")
                .build();

        mockMvc.perform(post("/professionals")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.specialty", equalTo("NEUROLOGIA")))
                .andExpect(jsonPath("$.licenseNumber", equalTo("CRM-RJ-99999")));
    }

    @Test
    void testCreateProfileForbiddenForProfessional() throws Exception {
        CreateProfessionalDTO dto = CreateProfessionalDTO.builder()
                .profession(ProfessionalType.MEDICO)
                .specialty(Specialty.DERMATOLOGIA)
                .licenseNumber("CRM-MG-77777")
                .build();

        mockMvc.perform(post("/professionals")
                .header("Authorization", "Bearer " + professionalToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }
}
