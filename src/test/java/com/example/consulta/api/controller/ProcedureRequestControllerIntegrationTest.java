package com.example.consulta.api.controller;

import com.example.consulta.api.dto.auth.LoginRequestDTO;
import com.example.consulta.api.dto.user.CreateUserDTO;
import com.example.consulta.domain.entity.PatientProfile;
import com.example.consulta.domain.entity.ProfessionalProfile;
import com.example.consulta.domain.entity.ProfessionalService;
import com.example.consulta.domain.entity.User;
import com.example.consulta.domain.enums.Gender;
import com.example.consulta.domain.enums.UserRole;
import com.example.consulta.domain.repository.PatientProfileRepository;
import com.example.consulta.domain.repository.ProfessionalProfileRepository;
import com.example.consulta.domain.repository.ProfessionalServiceRepository;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = DemoApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = {"spring.profiles.active=test"})
@Transactional
class ProcedureRequestControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private ProfessionalProfileRepository professionalProfileRepository;
    @Autowired private ProfessionalServiceRepository professionalServiceRepository;
    @Autowired private PatientProfileRepository patientProfileRepository;

    private String patientToken;
    private String professionalToken;
    private String patientProfileId;
    private String professionalProfileId;
    private String requiresConsultationServiceId;
    private String directBookingServiceId;

    @BeforeEach
    void setUp() throws Exception {
        // Register patient
        String patientResp = mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(CreateUserDTO.builder()
                        .name("PR Patient")
                        .email("pr.patient@test.com")
                        .password("pass1234")
                        .cpf("20000000001")
                        .phone("11900000003")
                        .birthDate(LocalDate.of(1992, 5, 20))
                        .gender(Gender.FEMALE)
                        .build())))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String patientUserId = objectMapper.readTree(patientResp).get("id").asText();
        patientToken = loginToken("pr.patient@test.com", "pass1234");
        PatientProfile patientProfile = patientProfileRepository.findByUserId(patientUserId).orElseThrow();
        patientProfileId = patientProfile.getId();

        // Register professional
        String docResp = mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(CreateUserDTO.builder()
                        .name("PR Doctor")
                        .email("pr.doctor@test.com")
                        .password("doc12345")
                        .cpf("20000000002")
                        .phone("11900000004")
                        .birthDate(LocalDate.of(1978, 8, 15))
                        .gender(Gender.MALE)
                        .build())))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String professionalUserId = objectMapper.readTree(docResp).get("id").asText();

        User docUser = userRepository.findById(professionalUserId).orElseThrow();
        docUser.setRole(UserRole.PROFESSIONAL);
        userRepository.saveAndFlush(docUser);

        ProfessionalProfile profile = ProfessionalProfile.builder()
                .user(docUser)
                .specialty("Cirurgia Plástica")
                .licenseNumber("CRM-SP-20002")
                .build();
        ProfessionalProfile savedProfile = professionalProfileRepository.saveAndFlush(profile);
        professionalProfileId = savedProfile.getId();
        professionalToken = loginToken("pr.doctor@test.com", "doc12345");

        // Create services directly via repo (setup data)
        ProfessionalService svcRequires = ProfessionalService.builder()
                .professional(savedProfile)
                .name("Rinoplastia")
                .price(new BigDecimal("5000.00"))
                .durationMinutes(120)
                .requiresConsultation(true)
                .build();
        requiresConsultationServiceId = professionalServiceRepository.saveAndFlush(svcRequires).getId();

        ProfessionalService svcDirect = ProfessionalService.builder()
                .professional(savedProfile)
                .name("Limpeza de Pele")
                .price(new BigDecimal("150.00"))
                .durationMinutes(60)
                .requiresConsultation(false)
                .build();
        directBookingServiceId = professionalServiceRepository.saveAndFlush(svcDirect).getId();
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

    @Test
    void createProcedureRequest_shouldReturn201() throws Exception {
        mockMvc.perform(post("/procedure-requests")
                .header("Authorization", "Bearer " + professionalToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "serviceId", requiresConsultationServiceId,
                        "patientId", patientProfileId,
                        "notes", "Paciente aprovado para rinoplastia"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.serviceName", equalTo("Rinoplastia")))
                .andExpect(jsonPath("$.status", equalTo("PENDING")))
                .andExpect(jsonPath("$.patientId", equalTo(patientProfileId)));
    }

    @Test
    void createProcedureRequest_byPatient_shouldReturn403() throws Exception {
        mockMvc.perform(post("/procedure-requests")
                .header("Authorization", "Bearer " + patientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "serviceId", requiresConsultationServiceId,
                        "patientId", patientProfileId))))
                .andExpect(status().isForbidden());
    }

    @Test
    void createProcedureRequest_serviceNotRequiringConsultation_shouldReturn400() throws Exception {
        mockMvc.perform(post("/procedure-requests")
                .header("Authorization", "Bearer " + professionalToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "serviceId", directBookingServiceId,
                        "patientId", patientProfileId))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getMine_asPatient_shouldReturnOwnRequests() throws Exception {
        // Create a request first
        mockMvc.perform(post("/procedure-requests")
                .header("Authorization", "Bearer " + professionalToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "serviceId", requiresConsultationServiceId,
                        "patientId", patientProfileId))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/procedure-requests/mine")
                .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].patientId", equalTo(patientProfileId)));
    }

    @Test
    void getMine_asProfessional_shouldReturnOwnRequests() throws Exception {
        mockMvc.perform(post("/procedure-requests")
                .header("Authorization", "Bearer " + professionalToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "serviceId", requiresConsultationServiceId,
                        "patientId", patientProfileId))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/procedure-requests/mine")
                .header("Authorization", "Bearer " + professionalToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].professionalId", equalTo(professionalProfileId)));
    }

    @Test
    void scheduleProcedureRequest_shouldReturn200AndStatusScheduled() throws Exception {
        String createResp = mockMvc.perform(post("/procedure-requests")
                .header("Authorization", "Bearer " + professionalToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "serviceId", requiresConsultationServiceId,
                        "patientId", patientProfileId))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String requestId = objectMapper.readTree(createResp).get("id").asText();

        mockMvc.perform(post("/procedure-requests/" + requestId + "/schedule")
                .header("Authorization", "Bearer " + patientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "scheduledAt", LocalDateTime.now().plusDays(7).withHour(14).withMinute(0).withSecond(0).withNano(0).toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", equalTo("SCHEDULED")))
                .andExpect(jsonPath("$.appointmentId", notNullValue()));
    }

    @Test
    void cancelProcedureRequest_byPatient_shouldReturn200() throws Exception {
        String createResp = mockMvc.perform(post("/procedure-requests")
                .header("Authorization", "Bearer " + professionalToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "serviceId", requiresConsultationServiceId,
                        "patientId", patientProfileId))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String requestId = objectMapper.readTree(createResp).get("id").asText();

        mockMvc.perform(put("/procedure-requests/" + requestId + "/cancel")
                .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", equalTo("CANCELED")));
    }

    @Test
    void cancelProcedureRequest_byProfessional_shouldReturn200() throws Exception {
        String createResp = mockMvc.perform(post("/procedure-requests")
                .header("Authorization", "Bearer " + professionalToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "serviceId", requiresConsultationServiceId,
                        "patientId", patientProfileId))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String requestId = objectMapper.readTree(createResp).get("id").asText();

        mockMvc.perform(put("/procedure-requests/" + requestId + "/cancel")
                .header("Authorization", "Bearer " + professionalToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", equalTo("CANCELED")));
    }

    @Test
    void scheduleProcedureRequest_alreadyScheduled_shouldReturn400() throws Exception {
        String createResp = mockMvc.perform(post("/procedure-requests")
                .header("Authorization", "Bearer " + professionalToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "serviceId", requiresConsultationServiceId,
                        "patientId", patientProfileId))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String requestId = objectMapper.readTree(createResp).get("id").asText();

        String scheduleBody = objectMapper.writeValueAsString(Map.of(
                "scheduledAt", LocalDateTime.now().plusDays(7).withHour(10).withMinute(0).withSecond(0).withNano(0).toString()));

        mockMvc.perform(post("/procedure-requests/" + requestId + "/schedule")
                .header("Authorization", "Bearer " + patientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(scheduleBody))
                .andExpect(status().isOk());

        // Second schedule attempt fails
        mockMvc.perform(post("/procedure-requests/" + requestId + "/schedule")
                .header("Authorization", "Bearer " + patientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(scheduleBody))
                .andExpect(status().isBadRequest());
    }
}
