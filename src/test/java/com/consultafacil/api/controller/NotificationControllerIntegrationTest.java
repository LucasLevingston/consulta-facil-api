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
import com.consultafacil.domain.enums.UserRole;
import com.consultafacil.domain.repository.ClinicRepository;
import com.consultafacil.domain.repository.NotificationRepository;
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
import com.consultafacil.domain.enums.Specialty;

import java.time.LocalDate;
import com.consultafacil.domain.enums.Specialty;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.consultafacil.domain.enums.Specialty;

@SpringBootTest(classes = ConsultaFacilApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = {"spring.profiles.active=test"})
@Transactional
class NotificationControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private ProfessionalProfileRepository professionalProfileRepository;
    @Autowired private ClinicRepository clinicRepository;
    @Autowired private NotificationRepository notificationRepository;

    private String ownerToken;
    private String professionalToken;
    private String ownerUserId;
    private String professionalUserId;
    private String professionalProfileId;
    private Clinic clinic;

    @BeforeEach
    void setUp() throws Exception {
        String ownerResp = mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(CreateUserDTO.builder()
                        .name("Clinic Owner")
                        .email("notif.owner@test.com")
                        .password("password1")
                        .cpf("11122233344")
                        .phone("11900000001")
                        .birthDate(LocalDate.of(1980, 1, 1))
                        .gender(Gender.MALE)
                        .build())))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        ownerUserId = objectMapper.readTree(ownerResp).get("id").asText();
        ownerToken = loginToken("notif.owner@test.com", "password1");

        String profResp = mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(CreateUserDTO.builder()
                        .name("Dr. Invited")
                        .email("notif.doctor@test.com")
                        .password("password1")
                        .cpf("55566677788")
                        .phone("11900000002")
                        .birthDate(LocalDate.of(1975, 6, 15))
                        .gender(Gender.FEMALE)
                        .build())))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        professionalUserId = objectMapper.readTree(profResp).get("id").asText();

        User profUser = userRepository.findById(professionalUserId).orElseThrow();
        profUser.setRole(UserRole.PROFESSIONAL);
        userRepository.saveAndFlush(profUser);

        ProfessionalProfile profile = ProfessionalProfile.builder()
                .user(profUser).specialty(Specialty.CARDIOLOGIA).licenseNumber("CRM-SP-99999").build();
        professionalProfileId = professionalProfileRepository.saveAndFlush(profile).getId();

        professionalToken = loginToken("notif.doctor@test.com", "password1");

        User ownerUser = userRepository.findById(ownerUserId).orElseThrow();
        clinic = clinicRepository.saveAndFlush(Clinic.builder()
                .name("Test Clinic").owner(ownerUser).build());
    }

    private String loginToken(String email, String password) throws Exception {
        String resp = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        LoginRequestDTO.builder().email(email).password(password).build())))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resp).get("token").asText();
    }

    private Notification createInvite(NotificationStatus status) {
        User target = userRepository.findById(professionalUserId).orElseThrow();
        ProfessionalProfile profile = professionalProfileRepository.findById(professionalProfileId).orElseThrow();
        return notificationRepository.saveAndFlush(Notification.builder()
                .type(NotificationType.CLINIC_INVITE)
                .title("Convite para clínica")
                .message("Convite para " + clinic.getName())
                .targetUser(target)
                .clinic(clinic)
                .professionalProfile(profile)
                .status(status)
                .build());
    }

    // ─── GET /notifications/me ────────────────────────────────────────────────

    @Test
    void testGetMyNotificationsReturnsList() throws Exception {
        createInvite(NotificationStatus.PENDING);

        mockMvc.perform(get("/notifications/me")
                .header("Authorization", "Bearer " + professionalToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].type", equalTo("CLINIC_INVITE")))
                .andExpect(jsonPath("$[0].status", equalTo("PENDING")));
    }

    @Test
    void testGetMyNotificationsEmptyWhenNone() throws Exception {
        mockMvc.perform(get("/notifications/me")
                .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void testGetMyNotificationsRequiresAuth() throws Exception {
        mockMvc.perform(get("/notifications/me"))
                .andExpect(status().isUnauthorized());
    }

    // ─── GET /notifications/me/unread-count ───────────────────────────────────

    @Test
    void testUnreadCountReturnsPendingCount() throws Exception {
        createInvite(NotificationStatus.PENDING);

        mockMvc.perform(get("/notifications/me/unread-count")
                .header("Authorization", "Bearer " + professionalToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count", equalTo(1)));
    }

    @Test
    void testUnreadCountZeroWhenNoPending() throws Exception {
        createInvite(NotificationStatus.READ);

        mockMvc.perform(get("/notifications/me/unread-count")
                .header("Authorization", "Bearer " + professionalToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count", equalTo(0)));
    }

    // ─── PUT /notifications/{id}/read ─────────────────────────────────────────

    @Test
    void testMarkAsReadSuccess() throws Exception {
        Notification n = createInvite(NotificationStatus.PENDING);

        mockMvc.perform(put("/notifications/" + n.getId() + "/read")
                .header("Authorization", "Bearer " + professionalToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", equalTo("READ")));
    }

    @Test
    void testMarkAsReadWrongUserFails() throws Exception {
        Notification n = createInvite(NotificationStatus.PENDING);

        mockMvc.perform(put("/notifications/" + n.getId() + "/read")
                .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testMarkAsReadAlreadyReadNoOp() throws Exception {
        Notification n = createInvite(NotificationStatus.READ);

        mockMvc.perform(put("/notifications/" + n.getId() + "/read")
                .header("Authorization", "Bearer " + professionalToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", equalTo("READ")));
    }

    // ─── PUT /notifications/read-all ──────────────────────────────────────────

    @Test
    void testMarkAllAsReadSuccess() throws Exception {
        createInvite(NotificationStatus.PENDING);

        mockMvc.perform(put("/notifications/read-all")
                .header("Authorization", "Bearer " + professionalToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void testMarkAllAsReadWithNoPendingSucceeds() throws Exception {
        mockMvc.perform(put("/notifications/read-all")
                .header("Authorization", "Bearer " + professionalToken))
                .andExpect(status().isNoContent());
    }

    // ─── PUT /notifications/{id}/accept ───────────────────────────────────────

    @Test
    void testAcceptInviteSuccess() throws Exception {
        Notification n = createInvite(NotificationStatus.PENDING);

        mockMvc.perform(put("/notifications/" + n.getId() + "/accept")
                .header("Authorization", "Bearer " + professionalToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", equalTo("ACCEPTED")));
    }

    @Test
    void testAcceptAlreadyRespondedInviteFails() throws Exception {
        Notification n = createInvite(NotificationStatus.ACCEPTED);

        mockMvc.perform(put("/notifications/" + n.getId() + "/accept")
                .header("Authorization", "Bearer " + professionalToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testAcceptInviteWrongUserFails() throws Exception {
        Notification n = createInvite(NotificationStatus.PENDING);

        mockMvc.perform(put("/notifications/" + n.getId() + "/accept")
                .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isBadRequest());
    }

    // ─── PUT /notifications/{id}/decline ──────────────────────────────────────

    @Test
    void testDeclineInviteSuccess() throws Exception {
        Notification n = createInvite(NotificationStatus.PENDING);

        mockMvc.perform(put("/notifications/" + n.getId() + "/decline")
                .header("Authorization", "Bearer " + professionalToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", equalTo("DECLINED")));
    }

    @Test
    void testDeclineAlreadyRespondedInviteFails() throws Exception {
        Notification n = createInvite(NotificationStatus.DECLINED);

        mockMvc.perform(put("/notifications/" + n.getId() + "/decline")
                .header("Authorization", "Bearer " + professionalToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testDeclineInviteWrongUserFails() throws Exception {
        Notification n = createInvite(NotificationStatus.PENDING);

        mockMvc.perform(put("/notifications/" + n.getId() + "/decline")
                .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isBadRequest());
    }

    // ─── POST /clinics/{clinicId}/invites/{professionalProfileId} ─────────────

    @Test
    void testSendClinicInviteSuccess() throws Exception {
        User ownerUser = userRepository.findById(ownerUserId).orElseThrow();
        ownerUser.setRole(UserRole.PROFESSIONAL);
        userRepository.saveAndFlush(ownerUser);
        String freshOwnerToken = loginToken("notif.owner@test.com", "password1");

        mockMvc.perform(post("/clinics/" + clinic.getId() + "/invites/" + professionalProfileId)
                .header("Authorization", "Bearer " + freshOwnerToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void testSendClinicInviteDuplicateFails() throws Exception {
        User ownerUser = userRepository.findById(ownerUserId).orElseThrow();
        ownerUser.setRole(UserRole.PROFESSIONAL);
        userRepository.saveAndFlush(ownerUser);
        String freshOwnerToken = loginToken("notif.owner@test.com", "password1");

        mockMvc.perform(post("/clinics/" + clinic.getId() + "/invites/" + professionalProfileId)
                .header("Authorization", "Bearer " + freshOwnerToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/clinics/" + clinic.getId() + "/invites/" + professionalProfileId)
                .header("Authorization", "Bearer " + freshOwnerToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testSendClinicInviteNonOwnerFails() throws Exception {
        mockMvc.perform(post("/clinics/" + clinic.getId() + "/invites/" + professionalProfileId)
                .header("Authorization", "Bearer " + professionalToken))
                .andExpect(status().isBadRequest());
    }
}
