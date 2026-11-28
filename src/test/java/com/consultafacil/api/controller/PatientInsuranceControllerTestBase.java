package com.consultafacil.api.controller;

import com.consultafacil.ConsultaFacilApplication;
import com.consultafacil.api.dto.auth.LoginRequestDTO;
import com.consultafacil.api.dto.user.CreateUserDTO;
import com.consultafacil.domain.entity.HealthInsurancePlan;
import com.consultafacil.domain.entity.PatientProfile;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.Gender;
import com.consultafacil.domain.enums.UserRole;
import com.consultafacil.domain.port.out.StoragePort;
import com.consultafacil.domain.repository.insurance.HealthInsurancePlanRepository;
import com.consultafacil.domain.repository.patient.PatientProfileRepository;
import com.consultafacil.domain.repository.user.UserRepository;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = ConsultaFacilApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = {"spring.profiles.active=test"})
@Transactional
abstract class PatientInsuranceControllerTestBase {

    @Autowired protected MockMvc mockMvc;
    @Autowired protected ObjectMapper objectMapper;
    @Autowired protected UserRepository userRepository;
    @Autowired protected PatientProfileRepository patientProfileRepository;
    @Autowired protected HealthInsurancePlanRepository planRepository;

    @MockitoBean
    protected StoragePort storagePort;

    protected String patientToken;
    protected String adminToken;
    protected String planId;

    @BeforeEach
    void setUp() throws Exception {
        // Register a patient user
        String patientUserId = registerUser(
                "Patient Ins", "patient-ins@example.com", "patins001",
                "22233344455", "11999993333");
        // Patients are created with PATIENT role by default; ensure patient profile exists
        ensurePatientProfile(patientUserId);
        patientToken = loginAndGetToken("patient-ins@example.com", "patins001");

        // Register an admin user (not a patient — no patient profile)
        String adminUserId = registerUser(
                "Admin Ins", "admin-ins@example.com", "adminins001",
                "66677788899", "11999994444");
        promoteTo(adminUserId, UserRole.ADMIN);
        adminToken = loginAndGetToken("admin-ins@example.com", "adminins001");

        // Seed an active insurance plan
        HealthInsurancePlan plan = planRepository.saveAndFlush(
                HealthInsurancePlan.builder()
                        .name("Patient Test Plan")
                        .code("PTPLAN001")
                        .active(true)
                        .build());
        planId = plan.getId();
    }

    private String registerUser(String name, String email, String password,
                                String cpf, String phone) throws Exception {
        CreateUserDTO dto = CreateUserDTO.builder()
                .name(name).email(email).password(password).cpf(cpf)
                .phone(phone).birthDate(LocalDate.of(1992, 3, 10)).gender(Gender.FEMALE)
                .build();
        String resp = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resp).get("id").asText();
    }

    private void ensurePatientProfile(String userId) {
        User user = userRepository.findById(userId).orElseThrow();
        boolean exists = patientProfileRepository.findByUserId(userId).isPresent();
        if (!exists) {
            patientProfileRepository.saveAndFlush(PatientProfile.builder().user(user).build());
        }
    }

    private void promoteTo(String userId, UserRole role) {
        User user = userRepository.findById(userId).orElseThrow();
        user.setRole(role);
        userRepository.saveAndFlush(user);
    }

    protected String loginAndGetToken(String email, String password) throws Exception {
        String resp = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                LoginRequestDTO.builder().email(email).password(password).build())))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resp).get("token").asText();
    }
}
