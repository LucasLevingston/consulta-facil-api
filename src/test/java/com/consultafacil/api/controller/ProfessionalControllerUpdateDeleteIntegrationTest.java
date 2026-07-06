package com.consultafacil.api.controller;

import com.consultafacil.api.dto.professional.CreateProfessionalDTO;
import com.consultafacil.api.dto.user.CreateUserDTO;
import com.consultafacil.domain.enums.Gender;
import com.consultafacil.domain.enums.ProfessionalType;
import com.consultafacil.domain.enums.Specialty;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.time.LocalDate;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProfessionalControllerUpdateDeleteIntegrationTest extends ProfessionalControllerTestBase {

    @Test
    void testUpdateProfessionalProfile() throws Exception {
        CreateProfessionalDTO dto = CreateProfessionalDTO.builder()
                .profession(ProfessionalType.MEDICO)
                .specialty(Specialty.ORTOPEDIA)
                .licenseNumber("CRM-SP-12345")
                .build();

        mockMvc.perform(put("/professionals/" + professionalProfileId)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.specialty", equalTo("ORTOPEDIA")));
    }

    @Test
    void testDeleteProfessionalProfile() throws Exception {
        mockMvc.perform(delete("/professionals/" + professionalProfileId)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/professionals/" + professionalProfileId))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteRequiresAdmin() throws Exception {
        mockMvc.perform(delete("/professionals/" + professionalProfileId)
                .header("Authorization", "Bearer " + professionalToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void testRejectApplication() throws Exception {
        CreateUserDTO applicantDTO = CreateUserDTO.builder()
                .name("Reject Applicant").email("reject.applicant@example.com").password("password123")
                .cpf("55566677700").phone("11955556666")
                .birthDate(LocalDate.of(1988, 7, 20)).gender(Gender.FEMALE).build();

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(applicantDTO)))
                .andExpect(status().isCreated());
        String applicantToken = loginAndGetToken("reject.applicant@example.com", "password123");

        CreateProfessionalDTO dto = CreateProfessionalDTO.builder()
                .profession(ProfessionalType.MEDICO).specialty(Specialty.DERMATOLOGIA).licenseNumber("CRM-SP-66666").build();

        String createResp = mockMvc.perform(post("/professionals")
                .header("Authorization", "Bearer " + applicantToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String newProfileId = objectMapper.readTree(createResp).get("id").asText();

        mockMvc.perform(put("/professionals/" + newProfileId + "/reject")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", equalTo("REJECTED")));
    }
}
