package com.consultafacil.api.controller;

import com.consultafacil.api.dto.appointment.CreateAppointmentDTO;
import com.consultafacil.api.dto.auth.LoginRequestDTO;
import com.consultafacil.api.dto.user.CreateUserDTO;
import com.consultafacil.domain.entity.Appointment;
import com.consultafacil.domain.entity.ProfessionalProfile;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.AppointmentPaymentStatus;
import com.consultafacil.domain.enums.Gender;
import com.consultafacil.domain.enums.UserRole;
import com.consultafacil.domain.repository.AppointmentRepository;
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
import java.time.LocalDateTime;
import java.util.Map;
import com.consultafacil.domain.enums.Specialty;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.consultafacil.domain.enums.Specialty;

@SpringBootTest(classes = ConsultaFacilApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = {"spring.profiles.active=test"})
@Transactional
class AppointmentPaymentIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private ProfessionalProfileRepository professionalProfileRepository;
    @Autowired private AppointmentRepository appointmentRepository;

    private String patientToken;
    private String professionalToken;
    private String professionalProfileId;
    private String appointmentId;

    @BeforeEach
    void setUp() throws Exception {
        // Patient
        String patientReg = mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(CreateUserDTO.builder()
                        .name("Pay Patient")
                        .email("pay.patient@test.com")
                        .password("paypass12")
                        .cpf("88800000000")
                        .phone("11900000020")
                        .birthDate(LocalDate.of(1990, 1, 1))
                        .gender(Gender.FEMALE)
                        .build())))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String patientLogin = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(LoginRequestDTO.builder()
                        .email("pay.patient@test.com")
                        .password("paypass12")
                        .build())))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        patientToken = objectMapper.readTree(patientLogin).get("token").asText();

        // Professional
        String doctorReg = mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(CreateUserDTO.builder()
                        .name("Pay Doctor")
                        .email("pay.doctor@test.com")
                        .password("paydoc12")
                        .cpf("88800000001")
                        .phone("11900000021")
                        .birthDate(LocalDate.of(1980, 6, 15))
                        .gender(Gender.MALE)
                        .build())))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String doctorUserId = objectMapper.readTree(doctorReg).get("id").asText();

        User doctorUser = userRepository.findById(doctorUserId).orElseThrow();
        doctorUser.setRole(UserRole.PROFESSIONAL);
        userRepository.saveAndFlush(doctorUser);

        ProfessionalProfile profile = ProfessionalProfile.builder()
                .user(doctorUser)
                .specialty(Specialty.CARDIOLOGIA)
                .licenseNumber("CRM-SP-88800")
                .build();
        professionalProfileId = professionalProfileRepository.saveAndFlush(profile).getId();

        String doctorLogin = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(LoginRequestDTO.builder()
                        .email("pay.doctor@test.com")
                        .password("paydoc12")
                        .build())))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        professionalToken = objectMapper.readTree(doctorLogin).get("token").asText();

        // Appointment
        String apptResponse = mockMvc.perform(post("/appointments")
                .header("Authorization", "Bearer " + patientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(CreateAppointmentDTO.builder()
                        .professionalId(professionalProfileId)
                        .scheduledAt(LocalDateTime.now().plusDays(5).withHour(10).withMinute(0).withSecond(0).withNano(0))
                        .reason("Consulta paga")
                        .build())))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        appointmentId = objectMapper.readTree(apptResponse).get("id").asText();
    }

    @Test
    void createPayment_withoutAuth_shouldReturn401() throws Exception {
        mockMvc.perform(post("/appointments/" + appointmentId + "/payment"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createPayment_asProfessional_shouldReturn403() throws Exception {
        mockMvc.perform(post("/appointments/" + appointmentId + "/payment")
                .header("Authorization", "Bearer " + professionalToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void createPayment_nonExistentAppointment_shouldReturn404() throws Exception {
        mockMvc.perform(post("/appointments/nonexistent-id/payment")
                .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void createPayment_alreadyPaid_shouldReturn400() throws Exception {
        Appointment appt = appointmentRepository.findById(appointmentId).orElseThrow();
        appt.setPaymentStatus(AppointmentPaymentStatus.PAID);
        appointmentRepository.saveAndFlush(appt);

        mockMvc.perform(post("/appointments/" + appointmentId + "/payment")
                .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    void webhook_shouldReturn200Regardless() throws Exception {
        Map<String, Object> webhookBody = Map.of(
                "type", "payment",
                "data", Map.of("id", "999999999")
        );

        mockMvc.perform(post("/payments/webhook")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(webhookBody)))
                .andExpect(status().isOk());
    }

    @Test
    void webhook_unknownType_shouldReturn200() throws Exception {
        Map<String, Object> webhookBody = Map.of("type", "merchant_order");

        mockMvc.perform(post("/payments/webhook")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(webhookBody)))
                .andExpect(status().isOk());
    }

    @Test
    void webhook_paymentTypeWithNoId_shouldReturn200() throws Exception {
        Map<String, Object> webhookBody = Map.of("type", "payment");

        mockMvc.perform(post("/payments/webhook")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(webhookBody)))
                .andExpect(status().isOk());
    }
}
