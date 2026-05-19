package com.example.consulta.api.controller;

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
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = DemoApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = {"spring.profiles.active=test"})
@Transactional
class PatientProfileControllerIntegrationTest {

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
    private String doctorToken;
    private String doctorUserId;
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

        // Register and set up doctor
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

        doctorUserId = objectMapper.readTree(doctorRegResponse).get("id").asText();

        User doctorUser = userRepository.findById(doctorUserId).orElseThrow();
        doctorUser.setRole(UserRole.DOCTOR);
        userRepository.saveAndFlush(doctorUser);

        DoctorProfile profile = DoctorProfile.builder()
                .user(doctorUser)
                .specialty("Cardiologia")
                .licenseNumber("CRM-SP-99999")
                .build();
        doctorProfileId = doctorProfileRepository.saveAndFlush(profile).getId();

        String doctorLoginResponse = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(LoginRequestDTO.builder()
                        .email("doctor@example.com")
                        .password("doctor123")
                        .build())))
                .andReturn().getResponse().getContentAsString();

        doctorToken = objectMapper.readTree(doctorLoginResponse).get("token").asText();
    }

    @Test
    void testGetMyProfile() throws Exception {
        mockMvc.perform(get("/patients/me")
                .header("Authorization", "Bearer " + patientToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId", notNullValue()))
                .andExpect(jsonPath("$.name", equalTo("Patient User")))
                .andExpect(jsonPath("$.email", equalTo("patient@example.com")));
    }

    @Test
    void testGetPatientProfileById() throws Exception {
        mockMvc.perform(get("/patients/" + patientUserId)
                .header("Authorization", "Bearer " + patientToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId", equalTo(patientUserId)));
    }

    @Test
    void testUpdateMyProfile() throws Exception {
        Map<String, Object> updates = new HashMap<>();
        updates.put("occupation", "Software Engineer");

        mockMvc.perform(put("/patients/me")
                .header("Authorization", "Bearer " + patientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.occupation", equalTo("Software Engineer")));
    }

    @Test
    void testGetPatientMedicalRecordsNotFound() throws Exception {
        // Patient has no medical records initially — expect 404
        mockMvc.perform(get("/patients/" + patientUserId + "/medical-records")
                .header("Authorization", "Bearer " + patientToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdatePatientMedicalRecords() throws Exception {
        Map<String, Object> updates = new HashMap<>();
        updates.put("allergies", "Penicillin");
        updates.put("currentMedication", "Ibuprofen");
        updates.put("privacyConsent", true);

        mockMvc.perform(put("/patients/" + patientUserId + "/medical-records")
                .header("Authorization", "Bearer " + patientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.allergies", equalTo("Penicillin")))
                .andExpect(jsonPath("$.currentMedication", equalTo("Ibuprofen")))
                .andExpect(jsonPath("$.privacyConsent", equalTo(true)));
    }

    @Test
    void testGetDoctorPatientsEmptyInitially() throws Exception {
        mockMvc.perform(get("/patients/doctor/" + doctorUserId)
                .header("Authorization", "Bearer " + doctorToken)
                .param("page", "0")
                .param("size", "20")
                .param("search", "")
                .param("sort", "recent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", instanceOf(java.util.ArrayList.class)))
                .andExpect(jsonPath("$.totalElements", equalTo(0)));
    }

    @Test
    void testGetDoctorPatientsAfterAppointment() throws Exception {
        // Patient schedules an appointment with the doctor
        CreateAppointmentDTO dto = CreateAppointmentDTO.builder()
                .doctorId(doctorProfileId)
                .scheduledAt(LocalDateTime.now().plusDays(5))
                .reason("Consulta de rotina")
                .build();

        mockMvc.perform(post("/appointments")
                .header("Authorization", "Bearer " + patientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/patients/doctor/" + doctorUserId)
                .header("Authorization", "Bearer " + doctorToken)
                .param("page", "0")
                .param("size", "20")
                .param("search", "")
                .param("sort", "recent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", equalTo(1)))
                .andExpect(jsonPath("$.content[0].name", equalTo("Patient User")))
                .andExpect(jsonPath("$.content[0].totalAppointments", equalTo(1)));
    }

    @Test
    void testGetDoctorPatientsRequiresAuth() throws Exception {
        mockMvc.perform(get("/patients/doctor/" + doctorUserId)
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isUnauthorized());
    }
}
