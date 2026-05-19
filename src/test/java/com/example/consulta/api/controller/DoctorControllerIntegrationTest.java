package com.example.consulta.api.controller;

import com.example.consulta.api.dto.auth.LoginRequestDTO;
import com.example.consulta.api.dto.doctor.CreateDoctorDTO;
import com.example.consulta.api.dto.user.CreateUserDTO;
import com.example.consulta.domain.entity.DoctorProfile;
import com.example.consulta.domain.entity.User;
import com.example.consulta.domain.enums.Gender;
import com.example.consulta.domain.enums.UserRole;
import com.example.consulta.domain.repository.DoctorProfileRepository;
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
class DoctorControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DoctorProfileRepository doctorProfileRepository;

    private String adminToken;
    private String adminUserId;
    private String doctorToken;
    private String doctorProfileId;

    @BeforeEach
    void setUp() throws Exception {
        // Register admin user
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

        // Register doctor user
        CreateUserDTO doctorDTO = CreateUserDTO.builder()
                .name("Doctor User")
                .email("doctor@example.com")
                .password("doctor123")
                .cpf("98765432100")
                .phone("11888888888")
                .birthDate(LocalDate.of(1975, 5, 20))
                .gender(Gender.FEMALE)
                .build();

        String doctorRegResponse = mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(doctorDTO)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String doctorUserId = objectMapper.readTree(doctorRegResponse).get("id").asText();

        User doctorUser = userRepository.findById(doctorUserId).orElseThrow();
        doctorUser.setRole(UserRole.DOCTOR);
        userRepository.saveAndFlush(doctorUser);

        DoctorProfile profile = DoctorProfile.builder()
                .user(doctorUser)
                .specialty("Cardiologia")
                .licenseNumber("CRM-SP-12345")
                .build();
        doctorProfileId = doctorProfileRepository.saveAndFlush(profile).getId();

        String doctorLoginResponse = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(LoginRequestDTO.builder()
                        .email("doctor@example.com")
                        .password("doctor123")
                        .build())))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        doctorToken = objectMapper.readTree(doctorLoginResponse).get("token").asText();
    }

    @Test
    void testListDoctors() throws Exception {
        mockMvc.perform(get("/doctors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", instanceOf(java.util.ArrayList.class)))
                .andExpect(jsonPath("$.totalElements", greaterThanOrEqualTo(1)));
    }

    @Test
    void testSearchDoctorsBySpecialty() throws Exception {
        mockMvc.perform(get("/doctors/search").param("specialty", "Cardiologia"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].specialty", equalTo("Cardiologia")));
    }

    @Test
    void testGetDoctorById() throws Exception {
        mockMvc.perform(get("/doctors/" + doctorProfileId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", equalTo(doctorProfileId)))
                .andExpect(jsonPath("$.specialty", equalTo("Cardiologia")));
    }

    @Test
    void testGetDoctorByIdNotFound() throws Exception {
        mockMvc.perform(get("/doctors/non-existent-id"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetMyDoctorProfile() throws Exception {
        mockMvc.perform(get("/doctors/me")
                .header("Authorization", "Bearer " + doctorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.specialty", equalTo("Cardiologia")))
                .andExpect(jsonPath("$.licenseNumber", equalTo("CRM-SP-12345")));
    }

    @Test
    void testGetMyDoctorProfileForbiddenForPatient() throws Exception {
        // Register regular patient
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

        mockMvc.perform(get("/doctors/me")
                .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void testCreateDoctorProfile() throws Exception {
        // Admin creates a doctor profile for themselves
        CreateDoctorDTO dto = CreateDoctorDTO.builder()
                .specialty("Neurologia")
                .licenseNumber("CRM-RJ-99999")
                .build();

        mockMvc.perform(post("/doctors")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.specialty", equalTo("Neurologia")))
                .andExpect(jsonPath("$.licenseNumber", equalTo("CRM-RJ-99999")));
    }

    @Test
    void testCreateDoctorProfileForbiddenForDoctor() throws Exception {
        CreateDoctorDTO dto = CreateDoctorDTO.builder()
                .specialty("Dermatologia")
                .licenseNumber("CRM-MG-77777")
                .build();

        mockMvc.perform(post("/doctors")
                .header("Authorization", "Bearer " + doctorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testPatientCanBecomeDoctorViaApi() throws Exception {
        CreateUserDTO patientDTO = CreateUserDTO.builder()
                .name("Future Doctor")
                .email("futuredoctor@example.com")
                .password("password123")
                .cpf("55566677788")
                .phone("11955554444")
                .birthDate(java.time.LocalDate.of(1985, 3, 15))
                .gender(com.example.consulta.domain.enums.Gender.FEMALE)
                .build();

        String regResponse = mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patientDTO)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String patientLoginResponse = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(LoginRequestDTO.builder()
                        .email("futuredoctor@example.com")
                        .password("password123")
                        .build())))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String patientToken = objectMapper.readTree(patientLoginResponse).get("token").asText();

        CreateDoctorDTO dto = CreateDoctorDTO.builder()
                .specialty("Pediatria")
                .licenseNumber("CRM-SP-55555")
                .build();

        mockMvc.perform(post("/doctors")
                .header("Authorization", "Bearer " + patientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.specialty", equalTo("Pediatria")))
                .andExpect(jsonPath("$.licenseNumber", equalTo("CRM-SP-55555")));

        // After becoming a doctor the user role should be DOCTOR
        String patientId = objectMapper.readTree(regResponse).get("id").asText();
        com.example.consulta.domain.entity.User user = userRepository.findById(patientId).orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals(
                com.example.consulta.domain.enums.UserRole.DOCTOR, user.getRole());
    }

    @Test
    void testUpdateDoctorProfile() throws Exception {
        CreateDoctorDTO dto = CreateDoctorDTO.builder()
                .specialty("Ortopedia")
                .licenseNumber("CRM-SP-12345")
                .build();

        mockMvc.perform(put("/doctors/" + doctorProfileId)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.specialty", equalTo("Ortopedia")));
    }

    @Test
    void testDeleteDoctorProfile() throws Exception {
        mockMvc.perform(delete("/doctors/" + doctorProfileId)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/doctors/" + doctorProfileId))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteDoctorRequiresAdmin() throws Exception {
        mockMvc.perform(delete("/doctors/" + doctorProfileId)
                .header("Authorization", "Bearer " + doctorToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void testSearchDoctorsBySpecialtyNoResults() throws Exception {
        mockMvc.perform(get("/doctors/search").param("specialty", "EspecialidadeInexistente"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", instanceOf(java.util.ArrayList.class)))
                .andExpect(jsonPath("$.totalElements", equalTo(0)));
    }

    @Test
    void testGetMyDoctorProfileWithoutAuthIsForbidden() throws Exception {
        // /doctors/me is @PreAuthorize-gated — returns 403 for both unauthenticated and wrong role
        mockMvc.perform(get("/doctors/me"))
                .andExpect(status().isForbidden());
    }
}
