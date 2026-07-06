package com.consultafacil.api.controller;

import com.consultafacil.api.dto.auth.LoginRequestDTO;
import com.consultafacil.api.dto.user.CreateUserDTO;
import com.consultafacil.domain.entity.Clinic;
import com.consultafacil.domain.entity.Notification;
import com.consultafacil.domain.entity.ProfessionalProfile;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.Gender;
import com.consultafacil.domain.enums.NotificationStatus;
import com.consultafacil.domain.enums.NotificationType;
import com.consultafacil.domain.enums.Specialty;
import com.consultafacil.domain.enums.UserRole;
import com.consultafacil.domain.repository.ClinicRepository;
import com.consultafacil.domain.repository.NotificationRepository;
import com.consultafacil.domain.repository.ProfessionalProfileRepository;
import com.consultafacil.domain.repository.UserRepository;
import com.consultafacil.ConsultaFacilApplication;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = ConsultaFacilApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = {"spring.profiles.active=test"})
@Transactional
abstract class NotificationControllerTestBase {

    @Autowired protected MockMvc mockMvc;
    @Autowired protected ObjectMapper objectMapper;
    @Autowired protected UserRepository userRepository;
    @Autowired protected ProfessionalProfileRepository professionalProfileRepository;
    @Autowired protected ClinicRepository clinicRepository;
    @Autowired protected NotificationRepository notificationRepository;

    protected String ownerToken, professionalToken, ownerUserId, professionalUserId, professionalProfileId;
    protected Clinic clinic;

    @BeforeEach
    void setUp() throws Exception {
        String ownerResp = mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(CreateUserDTO.builder()
                        .name("Clinic Owner").email("notif.owner@test.com").password("password1")
                        .cpf("11122233344").phone("11900000001")
                        .birthDate(LocalDate.of(1980, 1, 1)).gender(Gender.MALE).build())))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        ownerUserId = objectMapper.readTree(ownerResp).get("id").asText();
        ownerToken = loginToken("notif.owner@test.com", "password1");

        String profResp = mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(CreateUserDTO.builder()
                        .name("Dr. Invited").email("notif.doctor@test.com").password("password1")
                        .cpf("55566677788").phone("11900000002")
                        .birthDate(LocalDate.of(1975, 6, 15)).gender(Gender.FEMALE).build())))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        professionalUserId = objectMapper.readTree(profResp).get("id").asText();

        User profUser = userRepository.findById(professionalUserId).orElseThrow();
        profUser.setRole(UserRole.PROFESSIONAL);
        userRepository.saveAndFlush(profUser);

        ProfessionalProfile profile = ProfessionalProfile.builder()
                .user(profUser).specialty(Specialty.CARDIOLOGIA).licenseNumber("CRM-SP-99999").build();
        professionalProfileId = professionalProfileRepository.saveAndFlush(profile).getId();
        professionalToken = loginToken("notif.doctor@test.com", "password1");

        User ownerUser = userRepository.findById(ownerUserId).orElseThrow();
        clinic = clinicRepository.saveAndFlush(Clinic.builder().name("Test Clinic").owner(ownerUser).build());
    }

    protected String loginToken(String email, String password) throws Exception {
        String resp = mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        LoginRequestDTO.builder().email(email).password(password).build())))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resp).get("token").asText();
    }

    protected Notification createInvite(NotificationStatus status) {
        User target = userRepository.findById(professionalUserId).orElseThrow();
        ProfessionalProfile profile = professionalProfileRepository.findById(professionalProfileId).orElseThrow();
        return notificationRepository.saveAndFlush(Notification.builder()
                .type(NotificationType.CLINIC_INVITE).title("Convite para clínica")
                .message("Convite para " + clinic.getName()).targetUser(target)
                .clinic(clinic).professionalProfile(profile).status(status).build());
    }
}
