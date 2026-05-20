package com.example.consulta.api.controller;

import com.example.consulta.api.dto.auth.LoginRequestDTO;
import com.example.consulta.api.dto.professional.CreateProfessionalDTO;
import com.example.consulta.api.dto.user.CreateUserDTO;
import com.example.consulta.domain.entity.ProfessionalProfile;
import com.example.consulta.domain.entity.User;
import com.example.consulta.domain.enums.ProfessionalProfileStatus;
import com.example.consulta.domain.enums.Gender;
import com.example.consulta.domain.enums.UserRole;
import com.example.consulta.domain.repository.ProfessionalProfileRepository;
import com.example.consulta.domain.repository.UserRepository;
import com.example.demo.DemoApplication;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = DemoApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = {"spring.profiles.active=test"})
@Transactional
class ProfessionalControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfessionalProfileRepository professionalProfileRepository;

    private String adminToken;
    private String adminUserId;
    private String professionalToken;
    private String professionalProfileId;

    @BeforeEach
    void setUp() throws Exception {
        CreateUserDTO adminDTO = CreateUserDTO.builder()
                .name("Admin User")
                .email("admin@example.com")
                .password("admin123")
                .cpf("12345678901")
                .phone("11999999999")
                .birthDate(LocalDate.of(1990, 1, 1))
                .gender(Gender.MALE)
                .build();

        String adminRegResponse = mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminDTO)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        adminUserId = objectMapper.readTree(adminRegResponse).get("id").asText();

        User adminUser = userRepository.findById(adminUserId).orElseThrow();
        adminUser.setRole(UserRole.ADMIN);
        userRepository.saveAndFlush(adminUser);

        String adminLoginResponse = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(LoginRequestDTO.builder()
                        .email("admin@example.com")
                        .password("admin123")
                        .build())))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        adminToken = objectMapper.readTree(adminLoginResponse).get("token").asText();

        CreateUserDTO profDTO = CreateUserDTO.builder()
                .name("Professional User")
                .email("professional@example.com")
                .password("prof1234")
                .cpf("98765432100")
                .phone("11888888888")
                .birthDate(LocalDate.of(1975, 5, 20))
                .gender(Gender.FEMALE)
                .build();

        String profRegResponse = mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(profDTO)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String professionalUserId = objectMapper.readTree(profRegResponse).get("id").asText();

        User professionalUser = userRepository.findById(professionalUserId).orElseThrow();
        professionalUser.setRole(UserRole.PROFESSIONAL);
        userRepository.saveAndFlush(professionalUser);

        ProfessionalProfile profile = ProfessionalProfile.builder()
                .user(professionalUser)
                .specialty("Cardiologia")
                .licenseNumber("CRM-SP-12345")
                .status(ProfessionalProfileStatus.ACTIVE)
                .build();
        professionalProfileId = professionalProfileRepository.saveAndFlush(profile).getId();

        String profLoginResponse = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(LoginRequestDTO.builder()
                        .email("professional@example.com")
                        .password("prof1234")
                        .build())))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        professionalToken = objectMapper.readTree(profLoginResponse).get("token").asText();
    }

    @Test
    void testListProfessionals() throws Exception {
        mockMvc.perform(get("/professionals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", instanceOf(java.util.ArrayList.class)))
                .andExpect(jsonPath("$.totalElements", greaterThanOrEqualTo(1)));
    }

    @Test
    void testSearchProfessionalsBySpecialty() throws Exception {
        mockMvc.perform(get("/professionals/search").param("specialty", "Cardiologia"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].specialty", equalTo("Cardiologia")));
    }

    @Test
    void testGetProfessionalById() throws Exception {
        mockMvc.perform(get("/professionals/" + professionalProfileId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", equalTo(professionalProfileId)))
                .andExpect(jsonPath("$.specialty", equalTo("Cardiologia")));
    }

    @Test
    void testGetProfessionalByIdNotFound() throws Exception {
        mockMvc.perform(get("/professionals/non-existent-id"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetMyProfile() throws Exception {
        mockMvc.perform(get("/professionals/me")
                .header("Authorization", "Bearer " + professionalToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.specialty", equalTo("Cardiologia")))
                .andExpect(jsonPath("$.licenseNumber", equalTo("CRM-SP-12345")));
    }

    @Test
    void testGetMyProfileForbiddenForPatient() throws Exception {
        CreateUserDTO patientDTO = CreateUserDTO.builder()
                .name("Patient")
                .email("patient@example.com")
                .password("password123")
                .cpf("11122233300")
                .phone("11777776666")
                .birthDate(LocalDate.of(2000, 1, 1))
                .gender(Gender.MALE)
                .build();

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patientDTO)));

        String patientLoginResponse = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(LoginRequestDTO.builder()
                        .email("patient@example.com")
                        .password("password123")
                        .build())))
                .andReturn().getResponse().getContentAsString();

        String patientToken = objectMapper.readTree(patientLoginResponse).get("token").asText();

        mockMvc.perform(get("/professionals/me")
                .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void testCreateProfessionalProfile() throws Exception {
        CreateProfessionalDTO dto = CreateProfessionalDTO.builder()
                .specialty("Neurologia")
                .licenseNumber("CRM-RJ-99999")
                .build();

        mockMvc.perform(post("/professionals")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.specialty", equalTo("Neurologia")))
                .andExpect(jsonPath("$.licenseNumber", equalTo("CRM-RJ-99999")));
    }

    @Test
    void testCreateProfileForbiddenForProfessional() throws Exception {
        CreateProfessionalDTO dto = CreateProfessionalDTO.builder()
                .specialty("Dermatologia")
                .licenseNumber("CRM-MG-77777")
                .build();

        mockMvc.perform(post("/professionals")
                .header("Authorization", "Bearer " + professionalToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

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

        String regResponse = mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patientDTO)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String patientLoginResponse = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(LoginRequestDTO.builder()
                        .email("futurepro@example.com")
                        .password("password123")
                        .build())))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String patientToken = objectMapper.readTree(patientLoginResponse).get("token").asText();

        CreateProfessionalDTO dto = CreateProfessionalDTO.builder()
                .specialty("Pediatria")
                .licenseNumber("CRM-SP-55555")
                .build();

        String createResponse = mockMvc.perform(post("/professionals")
                .header("Authorization", "Bearer " + patientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.specialty", equalTo("Pediatria")))
                .andExpect(jsonPath("$.status", equalTo("PENDING_REVIEW")))
                .andReturn().getResponse().getContentAsString();

        String patientId = objectMapper.readTree(regResponse).get("id").asText();
        User user = userRepository.findById(patientId).orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals(UserRole.PATIENT, user.getRole());

        String newProfileId = objectMapper.readTree(createResponse).get("id").asText();
        mockMvc.perform(put("/professionals/" + newProfileId + "/approve")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", equalTo("ACTIVE")));

        userRepository.findById(patientId).ifPresent(u ->
                org.junit.jupiter.api.Assertions.assertEquals(UserRole.PROFESSIONAL, u.getRole()));
    }

    @Test
    void testUpdateProfessionalProfile() throws Exception {
        CreateProfessionalDTO dto = CreateProfessionalDTO.builder()
                .specialty("Ortopedia")
                .licenseNumber("CRM-SP-12345")
                .build();

        mockMvc.perform(put("/professionals/" + professionalProfileId)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.specialty", equalTo("Ortopedia")));
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
    void testSearchBySpecialtyNoResults() throws Exception {
        mockMvc.perform(get("/professionals/search").param("specialty", "EspecialidadeInexistente"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", instanceOf(java.util.ArrayList.class)))
                .andExpect(jsonPath("$.totalElements", equalTo(0)));
    }

    @Test
    void testGetMyProfileWithoutAuthIsForbidden() throws Exception {
        mockMvc.perform(get("/professionals/me"))
                .andExpect(status().isForbidden());
    }
}
