package com.consultafacil.api.controller;

import com.consultafacil.ConsultaFacilApplication;
import com.consultafacil.api.dto.auth.LoginRequestDTO;
import com.consultafacil.api.dto.user.CreateUserDTO;
import com.consultafacil.domain.entity.ProfessionalProfile;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.Gender;
import com.consultafacil.domain.enums.Specialty;
import com.consultafacil.domain.enums.UserRole;
import com.consultafacil.domain.repository.AppointmentRepository;
import com.consultafacil.domain.repository.ProfessionalProfileRepository;
import com.consultafacil.domain.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = ConsultaFacilApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = {"spring.profiles.active=test"})
@Transactional
abstract class AppointmentControllerIntegrationTestBase {

    @Autowired protected MockMvc mockMvc;
    @Autowired protected ObjectMapper objectMapper;
    @Autowired protected UserRepository userRepository;
    @Autowired protected ProfessionalProfileRepository professionalProfileRepository;
    @Autowired protected AppointmentRepository appointmentRepository;

    protected String patientToken;
    protected String patientUserId;
    protected String adminToken;
    protected String professionalProfileId;

    protected String register(CreateUserDTO dto) throws Exception {
        String response = mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asText();
    }

    protected String login(String email, String password) throws Exception {
        LoginRequestDTO loginDTO = LoginRequestDTO.builder().email(email).password(password).build();
        String response = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("token").asText();
    }

    @BeforeEach
    void setUp() throws Exception {
        CreateUserDTO patientDTO = CreateUserDTO.builder()
                .name("Patient User").email("patient@example.com").password("patient123")
                .cpf("12345678901").phone("11999999999")
                .birthDate(LocalDate.of(1990, 1, 1)).gender(Gender.MALE).build();
        patientUserId = register(patientDTO);
        patientToken = login("patient@example.com", "patient123");

        // Register and promote a doctor user
        CreateUserDTO doctorDTO = CreateUserDTO.builder()
                .name("Doctor User").email("doctor@example.com").password("doctor123")
                .cpf("98765432100").phone("11888888888")
                .birthDate(LocalDate.of(1975, 5, 20)).gender(Gender.MALE).build();
        String doctorUserId = register(doctorDTO);

        User doctorUser = userRepository.findById(doctorUserId).orElseThrow();
        doctorUser.setRole(UserRole.PROFESSIONAL);
        userRepository.saveAndFlush(doctorUser);

        ProfessionalProfile profile = ProfessionalProfile.builder()
                .user(doctorUser).specialty(Specialty.CARDIOLOGIA).licenseNumber("CRM-SP-12345").build();
        ProfessionalProfile savedProfile = professionalProfileRepository.saveAndFlush(profile);
        professionalProfileId = savedProfile.getId();

        adminToken = login("doctor@example.com", "doctor123");
    }
}
