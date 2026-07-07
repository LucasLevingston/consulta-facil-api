package com.consultafacil.api.controller;

import com.consultafacil.ConsultaFacilApplication;
import com.consultafacil.api.dto.auth.LoginRequestDTO;
import com.consultafacil.api.dto.user.CreateUserDTO;
import com.consultafacil.domain.entity.ProfessionalProfile;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.Gender;
import com.consultafacil.domain.enums.Specialty;
import com.consultafacil.domain.enums.UserRole;
import com.consultafacil.domain.repository.professional.profile.ProfessionalProfileRepository;
import com.consultafacil.domain.repository.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = ConsultaFacilApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = {"spring.profiles.active=test"})
@Transactional
abstract class PatientProfileControllerTestBase {

    @Autowired protected MockMvc mockMvc;
    @Autowired protected ObjectMapper objectMapper;
    @Autowired protected UserRepository userRepository;
    @Autowired protected ProfessionalProfileRepository professionalProfileRepository;

    protected String patientToken, patientUserId, doctorToken, doctorUserId, professionalProfileId;

    @BeforeEach
    void setUp() throws Exception {
        CreateUserDTO patientDTO = CreateUserDTO.builder()
                .name("Patient User").email("patient@example.com").password("patient123")
                .cpf("12345678901").phone("11999999999")
                .birthDate(LocalDate.of(1990, 1, 1)).gender(Gender.MALE).build();

        String patientResponse = mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patientDTO)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        patientUserId = objectMapper.readTree(patientResponse).get("id").asText();
        patientToken = loginAndGetToken("patient@example.com", "patient123");

        CreateUserDTO doctorDTO = CreateUserDTO.builder()
                .name("Doctor User").email("doctor@example.com").password("doctor123")
                .cpf("98765432100").phone("11888888888")
                .birthDate(LocalDate.of(1975, 5, 20)).gender(Gender.MALE).build();

        String doctorRegResponse = mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(doctorDTO)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        doctorUserId = objectMapper.readTree(doctorRegResponse).get("id").asText();

        User doctorUser = userRepository.findById(doctorUserId).orElseThrow();
        doctorUser.setRole(UserRole.PROFESSIONAL);
        userRepository.saveAndFlush(doctorUser);

        ProfessionalProfile profile = ProfessionalProfile.builder()
                .user(doctorUser).specialty(Specialty.CARDIOLOGIA).licenseNumber("CRM-SP-99999").build();
        professionalProfileId = professionalProfileRepository.saveAndFlush(profile).getId();
        doctorToken = loginAndGetToken("doctor@example.com", "doctor123");
    }

    protected String loginAndGetToken(String email, String password) throws Exception {
        String resp = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        LoginRequestDTO.builder().email(email).password(password).build())))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resp).get("token").asText();
    }
}
