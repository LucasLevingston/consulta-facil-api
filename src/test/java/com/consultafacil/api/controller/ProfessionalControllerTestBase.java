package com.consultafacil.api.controller;

import com.consultafacil.ConsultaFacilApplication;
import com.consultafacil.api.dto.auth.LoginRequestDTO;
import com.consultafacil.api.dto.user.CreateUserDTO;
import com.consultafacil.domain.entity.ProfessionalProfile;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.Gender;
import com.consultafacil.domain.enums.ProfessionalProfileStatus;
import com.consultafacil.domain.enums.Specialty;
import com.consultafacil.domain.enums.UserRole;
import com.consultafacil.domain.repository.ProfessionalProfileRepository;
import com.consultafacil.domain.repository.UserRepository;
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
abstract class ProfessionalControllerTestBase {

    @Autowired protected MockMvc mockMvc;
    @Autowired protected ObjectMapper objectMapper;
    @Autowired protected UserRepository userRepository;
    @Autowired protected ProfessionalProfileRepository professionalProfileRepository;

    protected String adminToken;
    protected String adminUserId;
    protected String professionalToken;
    protected String professionalProfileId;

    @BeforeEach
    void setUp() throws Exception {
        adminUserId = registerUser("Admin User", "admin@example.com", "admin123",
                "12345678901", "11999999999", LocalDate.of(1990, 1, 1), Gender.MALE);
        promoteRole(adminUserId, UserRole.ADMIN);
        adminToken = loginAndGetToken("admin@example.com", "admin123");

        String professionalUserId = registerUser("Professional User", "professional@example.com", "prof1234",
                "98765432100", "11888888888", LocalDate.of(1975, 5, 20), Gender.FEMALE);
        promoteRole(professionalUserId, UserRole.PROFESSIONAL);
        professionalProfileId = createProfile(professionalUserId, Specialty.CARDIOLOGIA, "CRM-SP-12345");
        professionalToken = loginAndGetToken("professional@example.com", "prof1234");
    }

    protected String registerUser(String name, String email, String password, String cpf,
                                   String phone, LocalDate birthDate, Gender gender) throws Exception {
        CreateUserDTO dto = CreateUserDTO.builder()
                .name(name).email(email).password(password).cpf(cpf).phone(phone)
                .birthDate(birthDate).gender(gender).build();
        String resp = mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resp).get("id").asText();
    }

    protected void promoteRole(String userId, UserRole role) {
        User user = userRepository.findById(userId).orElseThrow();
        user.setRole(role);
        userRepository.saveAndFlush(user);
    }

    protected String createProfile(String userId, Specialty specialty, String license) {
        User user = userRepository.findById(userId).orElseThrow();
        ProfessionalProfile profile = ProfessionalProfile.builder()
                .user(user).specialty(specialty)
                .licenseNumber(license).status(ProfessionalProfileStatus.ACTIVE).build();
        return professionalProfileRepository.saveAndFlush(profile).getId();
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
