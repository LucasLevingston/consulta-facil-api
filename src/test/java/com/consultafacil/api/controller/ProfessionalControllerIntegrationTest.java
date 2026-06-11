package com.consultafacil.api.controller;

import com.consultafacil.api.dto.auth.LoginRequestDTO;
import com.consultafacil.api.dto.professional.CreateProfessionalDTO;
import com.consultafacil.api.dto.user.CreateUserDTO;
import com.consultafacil.domain.entity.ProfessionalProfile;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.ProfessionalProfileStatus;
import com.consultafacil.domain.enums.Gender;
import com.consultafacil.domain.enums.UserRole;
import com.consultafacil.domain.repository.ProfessionalProfileRepository;
import com.consultafacil.domain.repository.UserRepository;
import com.consultafacil.ConsultaFacilApplication;
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

@SpringBootTest(classes = ConsultaFacilApplication.class)
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
                .profession("Médico")
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
                .profession("Médico")
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
                .profession("Médico")
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
                .profession("Médico")
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

    @Test
    void testGetMyProfile_professionalUserWithNoProfile_returns404() throws Exception {
        // A PROFESSIONAL-role user who has no ProfessionalProfile entity returns 404.
        // This is the likely cause of the frontend 404 on GET /v1/professionals/me.
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

        String loginResponse = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(LoginRequestDTO.builder()
                        .email("noprofile.pro@example.com")
                        .password("password123")
                        .build())))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String token = objectMapper.readTree(loginResponse).get("token").asText();

        mockMvc.perform(get("/professionals/me")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetPendingApplications() throws Exception {
        CreateUserDTO applicantDTO = CreateUserDTO.builder()
                .name("Pending Applicant")
                .email("pending.applicant@example.com")
                .password("password123")
                .cpf("33344455500")
                .phone("11933334444")
                .birthDate(LocalDate.of(1990, 1, 1))
                .gender(Gender.FEMALE)
                .build();

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(applicantDTO)))
                .andExpect(status().isCreated());

        String applicantLogin = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(LoginRequestDTO.builder()
                        .email("pending.applicant@example.com").password("password123").build())))
                .andReturn().getResponse().getContentAsString();
        String applicantToken = objectMapper.readTree(applicantLogin).get("token").asText();

        CreateProfessionalDTO dto = CreateProfessionalDTO.builder()
                .profession("Médico").specialty("Reumatologia").licenseNumber("CRM-SP-88888").build();

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
        CreateUserDTO applicantDTO = CreateUserDTO.builder()
                .name("Status Applicant")
                .email("status.applicant@example.com")
                .password("password123")
                .cpf("44455566600")
                .phone("11944445555")
                .birthDate(LocalDate.of(1992, 3, 10))
                .gender(Gender.MALE)
                .build();

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(applicantDTO)))
                .andExpect(status().isCreated());

        String applicantLogin = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(LoginRequestDTO.builder()
                        .email("status.applicant@example.com").password("password123").build())))
                .andReturn().getResponse().getContentAsString();
        String applicantToken = objectMapper.readTree(applicantLogin).get("token").asText();

        CreateProfessionalDTO dto = CreateProfessionalDTO.builder()
                .profession("Médico").specialty("Psiquiatria").licenseNumber("CRM-SP-77777").build();

        mockMvc.perform(post("/professionals")
                .header("Authorization", "Bearer " + applicantToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/professionals/application-status")
                .header("Authorization", "Bearer " + applicantToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", equalTo("PENDING_REVIEW")))
                .andExpect(jsonPath("$.specialty", equalTo("Psiquiatria")));
    }

    @Test
    void testRejectApplication() throws Exception {
        CreateUserDTO applicantDTO = CreateUserDTO.builder()
                .name("Reject Applicant")
                .email("reject.applicant@example.com")
                .password("password123")
                .cpf("55566677700")
                .phone("11955556666")
                .birthDate(LocalDate.of(1988, 7, 20))
                .gender(Gender.FEMALE)
                .build();

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(applicantDTO)))
                .andExpect(status().isCreated());

        String applicantLogin = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(LoginRequestDTO.builder()
                        .email("reject.applicant@example.com").password("password123").build())))
                .andReturn().getResponse().getContentAsString();
        String applicantToken = objectMapper.readTree(applicantLogin).get("token").asText();

        CreateProfessionalDTO dto = CreateProfessionalDTO.builder()
                .profession("Médico").specialty("Dermatologia").licenseNumber("CRM-SP-66666").build();

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
