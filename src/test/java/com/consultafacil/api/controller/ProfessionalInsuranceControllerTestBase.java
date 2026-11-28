package com.consultafacil.api.controller;

import com.consultafacil.ConsultaFacilApplication;
import com.consultafacil.api.dto.auth.LoginRequestDTO;
import com.consultafacil.api.dto.user.CreateUserDTO;
import com.consultafacil.domain.entity.HealthInsurancePlan;
import com.consultafacil.domain.entity.ProfessionalProfile;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.Gender;
import com.consultafacil.domain.enums.ProfessionalProfileStatus;
import com.consultafacil.domain.enums.Specialty;
import com.consultafacil.domain.enums.UserRole;
import com.consultafacil.domain.port.out.StoragePort;
import com.consultafacil.domain.repository.insurance.HealthInsurancePlanRepository;
import com.consultafacil.domain.repository.professional.profile.ProfessionalProfileRepository;
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
abstract class ProfessionalInsuranceControllerTestBase {

    @Autowired protected MockMvc mockMvc;
    @Autowired protected ObjectMapper objectMapper;
    @Autowired protected UserRepository userRepository;
    @Autowired protected ProfessionalProfileRepository professionalProfileRepository;
    @Autowired protected HealthInsurancePlanRepository planRepository;

    @MockitoBean
    protected StoragePort storagePort;

    protected String professionalToken;
    protected String patientToken;
    protected String planId;

    @BeforeEach
    void setUp() throws Exception {
        // Register professional user
        String professionalUserId = registerUser(
                "Prof Insurance", "prof-insurance@example.com", "profpass1",
                "11122233344", "11999991111");
        promoteTo(professionalUserId, UserRole.PROFESSIONAL);
        createProfessionalProfile(professionalUserId);
        professionalToken = loginAndGetToken("prof-insurance@example.com", "profpass1");

        // Register patient user (no professional profile)
        String patientUserId = registerUser(
                "Pat Insurance", "pat-insurance@example.com", "patpass1",
                "55566677788", "11999992222");
        patientToken = loginAndGetToken("pat-insurance@example.com", "patpass1");

        // Seed an active insurance plan
        HealthInsurancePlan plan = planRepository.saveAndFlush(
                HealthInsurancePlan.builder()
                        .name("Test Plan")
                        .code("TESTPLAN001")
                        .active(true)
                        .build());
        planId = plan.getId();
    }

    private String registerUser(String name, String email, String password,
                                String cpf, String phone) throws Exception {
        CreateUserDTO dto = CreateUserDTO.builder()
                .name(name).email(email).password(password).cpf(cpf)
                .phone(phone).birthDate(LocalDate.of(1985, 6, 15)).gender(Gender.MALE)
                .build();
        String resp = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resp).get("id").asText();
    }

    private void promoteTo(String userId, UserRole role) {
        User user = userRepository.findById(userId).orElseThrow();
        user.setRole(role);
        userRepository.saveAndFlush(user);
    }

    private void createProfessionalProfile(String userId) {
        User user = userRepository.findById(userId).orElseThrow();
        ProfessionalProfile profile = ProfessionalProfile.builder()
                .user(user)
                .specialty(Specialty.CARDIOLOGIA)
                .licenseNumber("CRM-SP-99001")
                .status(ProfessionalProfileStatus.ACTIVE)
                .build();
        professionalProfileRepository.saveAndFlush(profile);
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
