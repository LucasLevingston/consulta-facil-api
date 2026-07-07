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
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProfessionalControllerApplicationIntegrationTest extends ProfessionalControllerTestBase {

    private String registerAndLogin(String name, String email, String cpf, String phone,
                                     LocalDate birthDate, Gender gender) throws Exception {
        CreateUserDTO dto = CreateUserDTO.builder()
                .name(name).email(email).password("password123").cpf(cpf).phone(phone)
                .birthDate(birthDate).gender(gender).build();

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        return loginAndGetToken(email, "password123");
    }

    @Test
    void testGetPendingApplications() throws Exception {
        String applicantToken = registerAndLogin("Pending Applicant", "pending.applicant@example.com",
                "33344455500", "11933334444", LocalDate.of(1990, 1, 1), Gender.FEMALE);

        CreateProfessionalDTO dto = CreateProfessionalDTO.builder()
                .profession(ProfessionalType.MEDICO).specialty(Specialty.REUMATOLOGIA).licenseNumber("CRM-SP-88888").build();

        mockMvc.perform(post("/professionals")
                .header("Authorization", "Bearer " + applicantToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/professionals/applications")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.content[0].status", equalTo("PENDING_REVIEW")));
    }

    @Test
    void testGetApplicationStatus() throws Exception {
        String applicantToken = registerAndLogin("Status Applicant", "status.applicant@example.com",
                "44455566600", "11944445555", LocalDate.of(1992, 3, 10), Gender.MALE);

        CreateProfessionalDTO dto = CreateProfessionalDTO.builder()
                .profession(ProfessionalType.MEDICO).specialty(Specialty.PSIQUIATRIA).licenseNumber("CRM-SP-77777").build();

        mockMvc.perform(post("/professionals")
                .header("Authorization", "Bearer " + applicantToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/professionals/application-status")
                .header("Authorization", "Bearer " + applicantToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", equalTo("PENDING_REVIEW")))
                .andExpect(jsonPath("$.specialty", equalTo("PSIQUIATRIA")));
    }

}
