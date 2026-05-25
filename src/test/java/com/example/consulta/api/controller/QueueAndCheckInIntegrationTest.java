package com.example.consulta.api.controller;

import com.example.consulta.api.dto.auth.LoginRequestDTO;
import com.example.consulta.api.dto.user.CreateUserDTO;
import com.example.consulta.domain.entity.Appointment;
import com.example.consulta.domain.entity.PatientProfile;
import com.example.consulta.domain.entity.ProfessionalProfile;
import com.example.consulta.domain.entity.User;
import com.example.consulta.domain.enums.AppointmentModality;
import com.example.consulta.domain.enums.AppointmentStatus;
import com.example.consulta.domain.enums.Gender;
import com.example.consulta.domain.enums.UserRole;
import com.example.consulta.domain.repository.AppointmentRepository;
import com.example.consulta.domain.repository.PatientProfileRepository;
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
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = DemoApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = {"spring.profiles.active=test"})
@Transactional
class QueueAndCheckInIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private ProfessionalProfileRepository professionalProfileRepository;
    @Autowired private PatientProfileRepository patientProfileRepository;
    @Autowired private AppointmentRepository appointmentRepository;

    private String patientToken;
    private String professionalToken;
    private String patientUserId;
    private String professionalUserId;
    private String professionalProfileId;
    private String patientProfileId;

    @BeforeEach
    void setUp() throws Exception {
        // Register patient
        String patientResp = mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(CreateUserDTO.builder()
                        .name("Queue Patient")
                        .email("queue.patient@test.com")
                        .password("password1")
                        .cpf("11122233344")
                        .phone("11911111111")
                        .birthDate(LocalDate.of(1990, 1, 1))
                        .gender(Gender.FEMALE)
                        .build())))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        patientUserId = objectMapper.readTree(patientResp).get("id").asText();

        patientToken = loginToken("queue.patient@test.com", "password1");

        PatientProfile patientProfile = patientProfileRepository.findByUserId(patientUserId).orElseThrow();
        patientProfileId = patientProfile.getId();

        // Register professional
        String profResp = mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(CreateUserDTO.builder()
                        .name("Queue Doctor")
                        .email("queue.doctor@test.com")
                        .password("password1")
                        .cpf("55566677788")
                        .phone("11922222222")
                        .birthDate(LocalDate.of(1975, 6, 15))
                        .gender(Gender.MALE)
                        .build())))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        professionalUserId = objectMapper.readTree(profResp).get("id").asText();

        User profUser = userRepository.findById(professionalUserId).orElseThrow();
        profUser.setRole(UserRole.PROFESSIONAL);
        userRepository.saveAndFlush(profUser);

        ProfessionalProfile profile = ProfessionalProfile.builder()
                .user(profUser).specialty("Neurologia").licenseNumber("CRM-RJ-99999").build();
        ProfessionalProfile saved = professionalProfileRepository.saveAndFlush(profile);
        professionalProfileId = saved.getId();

        professionalToken = loginToken("queue.doctor@test.com", "password1");
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

    private Appointment createAppointment(AppointmentStatus status) {
        PatientProfile patient = patientProfileRepository.findById(patientProfileId).orElseThrow();
        ProfessionalProfile professional = professionalProfileRepository.findById(professionalProfileId).orElseThrow();
        Appointment appt = Appointment.builder()
                .patient(patient)
                .professional(professional)
                .scheduledAt(LocalDateTime.now())
                .status(status)
                .modality(AppointmentModality.IN_PERSON)
                .reason("Dor de cabeça")
                .build();
        return appointmentRepository.saveAndFlush(appt);
    }

    // ─── GenerateCheckInTokenService ──────────────────────────────────────────

    @Test
    void testGetCheckInTokenSuccess() throws Exception {
        Appointment appt = createAppointment(AppointmentStatus.CONFIRMED);

        mockMvc.perform(get("/appointments/" + appt.getId() + "/checkin-token")
                .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", not(emptyString())))
                .andExpect(jsonPath("$.appointmentId", equalTo(appt.getId())));
    }

    @Test
    void testGetCheckInTokenForCanceledAppointmentFails() throws Exception {
        Appointment appt = createAppointment(AppointmentStatus.CANCELED);

        mockMvc.perform(get("/appointments/" + appt.getId() + "/checkin-token")
                .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetCheckInTokenWrongPatientFails() throws Exception {
        Appointment appt = createAppointment(AppointmentStatus.CONFIRMED);

        // professionalToken has PROFESSIONAL role — endpoint requires PATIENT
        mockMvc.perform(get("/appointments/" + appt.getId() + "/checkin-token")
                .header("Authorization", "Bearer " + professionalToken))
                .andExpect(status().isForbidden());
    }

    // ─── CheckInByQrService ───────────────────────────────────────────────────

    @Test
    void testCheckInByQrSuccess() throws Exception {
        Appointment appt = createAppointment(AppointmentStatus.CONFIRMED);

        // Get token first
        String tokenResp = mockMvc.perform(get("/appointments/" + appt.getId() + "/checkin-token")
                .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String qrToken = objectMapper.readTree(tokenResp).get("token").asText();

        // Use token to check in (requires PROFESSIONAL/RECEPTIONIST/ADMIN role)
        mockMvc.perform(post("/appointments/checkin")
                .header("Authorization", "Bearer " + professionalToken)
                .param("token", qrToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", equalTo("CHECKED_IN")))
                .andExpect(jsonPath("$.checkedInAt", notNullValue()));
    }

    @Test
    void testCheckInByQrWithInvalidTokenFails() throws Exception {
        mockMvc.perform(post("/appointments/checkin")
                .header("Authorization", "Bearer " + professionalToken)
                .param("token", "invalid.jwt.token"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCheckInByQrAlreadyCheckedInFails() throws Exception {
        Appointment appt = createAppointment(AppointmentStatus.CONFIRMED);

        String tokenResp = mockMvc.perform(get("/appointments/" + appt.getId() + "/checkin-token")
                .header("Authorization", "Bearer " + patientToken))
                .andReturn().getResponse().getContentAsString();
        String qrToken = objectMapper.readTree(tokenResp).get("token").asText();

        // First check-in succeeds
        mockMvc.perform(post("/appointments/checkin")
                .header("Authorization", "Bearer " + professionalToken)
                .param("token", qrToken))
                .andExpect(status().isOk());

        // Second check-in fails (status now CHECKED_IN, not CONFIRMED/PENDING)
        mockMvc.perform(post("/appointments/checkin")
                .header("Authorization", "Bearer " + professionalToken)
                .param("token", qrToken))
                .andExpect(status().isBadRequest());
    }

    // ─── CallNextPatientService ───────────────────────────────────────────────

    @Test
    void testCallNextPatientSuccess() throws Exception {
        Appointment appt = createAppointment(AppointmentStatus.CHECKED_IN);

        mockMvc.perform(put("/appointments/" + appt.getId() + "/call")
                .header("Authorization", "Bearer " + professionalToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", equalTo("IN_PROGRESS")))
                .andExpect(jsonPath("$.calledAt", notNullValue()));
    }

    @Test
    void testCallNextPatientNotCheckedInFails() throws Exception {
        Appointment appt = createAppointment(AppointmentStatus.CONFIRMED);

        mockMvc.perform(put("/appointments/" + appt.getId() + "/call")
                .header("Authorization", "Bearer " + professionalToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCallNextPatientWrongProfessionalFails() throws Exception {
        Appointment appt = createAppointment(AppointmentStatus.CHECKED_IN);

        // Patient token has PATIENT role — endpoint requires PROFESSIONAL/ADMIN
        mockMvc.perform(put("/appointments/" + appt.getId() + "/call")
                .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isForbidden());
    }

    // ─── GetQueueService ──────────────────────────────────────────────────────

    @Test
    void testGetQueueReturnsTodaysCheckedInAppointments() throws Exception {
        createAppointment(AppointmentStatus.CHECKED_IN);
        createAppointment(AppointmentStatus.IN_PROGRESS);
        createAppointment(AppointmentStatus.CONFIRMED); // should NOT appear

        mockMvc.perform(get("/appointments/queue")
                .header("Authorization", "Bearer " + professionalToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].status",
                        containsInAnyOrder("CHECKED_IN", "IN_PROGRESS")));
    }

    @Test
    void testGetQueueEmptyWhenNoneCheckedIn() throws Exception {
        createAppointment(AppointmentStatus.CONFIRMED);

        mockMvc.perform(get("/appointments/queue")
                .header("Authorization", "Bearer " + professionalToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
