package com.example.consulta.api.controller;

import com.example.consulta.api.dto.appointment.CancelAppointmentDTO;
import com.example.consulta.api.dto.appointment.CreateAppointmentDTO;
import com.example.consulta.api.dto.auth.LoginRequestDTO;
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
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = DemoApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = {"spring.profiles.active=test"})
@Transactional
class AppointmentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DoctorProfileRepository doctorProfileRepository;

    private String patientToken;
    private String patientUserId;
    private String adminToken;
    private String doctorProfileId;

    @BeforeEach
    void setUp() throws Exception {
        CreateUserDTO patientDTO = CreateUserDTO.builder()
                .name("Patient User")
                .email("patient@example.com")
                .password("patient123")
                .cpf("12345678901")
                .phone("11999999999")
                .birthDate(LocalDate.of(1990, 1, 1))
                .gender(Gender.MALE)
                .build();

        String patientResponse = mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patientDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        patientUserId = objectMapper.readTree(patientResponse).get("id").asText();

        LoginRequestDTO loginDTO = LoginRequestDTO.builder()
                .email("patient@example.com")
                .password("patient123")
                .build();

        String loginResponse = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        patientToken = objectMapper.readTree(loginResponse).get("token").asText();

        // Register and promote a doctor user
        CreateUserDTO doctorDTO = CreateUserDTO.builder()
                .name("Doctor User")
                .email("doctor@example.com")
                .password("doctor123")
                .cpf("98765432100")
                .phone("11888888888")
                .birthDate(LocalDate.of(1975, 5, 20))
                .gender(Gender.MALE)
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
        DoctorProfile savedProfile = doctorProfileRepository.saveAndFlush(profile);
        doctorProfileId = savedProfile.getId();

        String adminLoginResponse = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(LoginRequestDTO.builder()
                        .email("doctor@example.com")
                        .password("doctor123")
                        .build())))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        adminToken = objectMapper.readTree(adminLoginResponse).get("token").asText();
    }

    @Test
    void testGetAppointmentByIdNotFound() throws Exception {
        mockMvc.perform(get("/appointments/non-existent-id")
                .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetPatientAppointmentsReturnsEmptyPage() throws Exception {
        // patientUserId is the user ID; the endpoint /patient/{userId} resolves via PatientProfile
        mockMvc.perform(get("/appointments/patient/" + patientUserId)
                .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", instanceOf(java.util.ArrayList.class)))
                .andExpect(jsonPath("$.totalElements", equalTo(0)));
    }

    @Test
    void testScheduleAppointment() throws Exception {
        CreateAppointmentDTO dto = CreateAppointmentDTO.builder()
                .doctorId(doctorProfileId)
                .scheduledAt(LocalDateTime.now().plusDays(7))
                .reason("Consulta de rotina")
                .build();

        mockMvc.perform(post("/appointments")
                .header("Authorization", "Bearer " + patientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.status", equalTo("PENDING")))
                .andExpect(jsonPath("$.reason", equalTo("Consulta de rotina")))
                .andExpect(jsonPath("$.doctorId", equalTo(doctorProfileId)));
    }

    @Test
    void testScheduleAndConfirmAppointment() throws Exception {
        CreateAppointmentDTO dto = CreateAppointmentDTO.builder()
                .doctorId(doctorProfileId)
                .scheduledAt(LocalDateTime.now().plusDays(10))
                .reason("Check-up anual")
                .build();

        String createResponse = mockMvc.perform(post("/appointments")
                .header("Authorization", "Bearer " + patientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String appointmentId = objectMapper.readTree(createResponse).get("id").asText();

        mockMvc.perform(put("/appointments/" + appointmentId + "/confirm")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", equalTo("CONFIRMED")));
    }

    @Test
    void testScheduleAndCancelAppointment() throws Exception {
        CreateAppointmentDTO dto = CreateAppointmentDTO.builder()
                .doctorId(doctorProfileId)
                .scheduledAt(LocalDateTime.now().plusDays(5))
                .reason("Consulta de retorno")
                .build();

        String createResponse = mockMvc.perform(post("/appointments")
                .header("Authorization", "Bearer " + patientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String appointmentId = objectMapper.readTree(createResponse).get("id").asText();

        CancelAppointmentDTO cancelDTO = CancelAppointmentDTO.builder()
                .cancellationReason("Paciente não pode comparecer")
                .build();

        mockMvc.perform(put("/appointments/" + appointmentId + "/cancel")
                .header("Authorization", "Bearer " + patientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cancelDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", equalTo("CANCELED")))
                .andExpect(jsonPath("$.cancellationReason", equalTo("Paciente não pode comparecer")));
    }

    @Test
    void testScheduleDuplicateAppointmentFails() throws Exception {
        // withNano(0) avoids H2 nanosecond truncation causing equality check to miss
        LocalDateTime scheduledAt = LocalDateTime.now().withNano(0).plusDays(14);

        CreateAppointmentDTO dto = CreateAppointmentDTO.builder()
                .doctorId(doctorProfileId)
                .scheduledAt(scheduledAt)
                .reason("Primeira consulta")
                .build();

        mockMvc.perform(post("/appointments")
                .header("Authorization", "Bearer " + patientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/appointments")
                .header("Authorization", "Bearer " + patientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }
}
